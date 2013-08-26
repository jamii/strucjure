(ns strucjure.pattern
  (:require [clojure.set :refer [union]]
            [plumbing.core :refer [aconcat map-vals fnk]]
            [strucjure.util :refer [when-nil with-syms fnk->clj]]))

;; TODO Record
;; TODO Set? (how would you match subpatterns? maybe only allow bind/with-meta? or only value patterns)
;; TODO Atom/Ref/Agent? (what would the output be?)
;; TODO when gen is added, *pattern->clj* will be a poor name
;; TODO need a general way to indicate that output is unchanged for eg WithMeta
;;      just check (= input output)?
;; TODO think carefully about seq vs list
;; TODO could return to having implicit equality but would require careful thinking about Guard/Output
;;      state needs to track 'has it been bound before' and 'will it need to be bound again' - not exclusive
;; TODO should View take a fn instead of a form? where do we want to eval it?

(defprotocol IPattern
  (fmap [this f]
    "Apply f to all immediate child patterns. Does not need to keep metadata.")
  (pattern->clj [this input output? state result->body]
    "Compile a pattern into clojure which returns nil on failure or hands control to result->body on success.
     input -- form, input to the pattern
     output? -- bool, whether the output of this pattern is used anywhere
     state -- {symbol :bound/:free}, :bound symbols are in scope already, :free symbols are used somewhere in the body
     result->body -- (fn [output remaining] form), returns the body to be evaluated on success, should be called *exactly* once"))

(defn ^:dynamic *pattern->clj* [this input output? state result->body]
  (if (symbol? input)
    (pattern->clj this input output? state result->body)
    (with-syms [input-sym]
      `(let [~input-sym ~input]
         ~(pattern->clj this input-sym output? state result->body)))))

(defn with-binding [pattern var val]
  (vary-meta pattern clojure.core/update-in [::bindings] #(assoc % var val)))

;; --- REST ---

(defn ->Rest [pattern]
  (vary-meta pattern assoc ::rest true))

(defn rest? [pattern]
  (::rest (meta pattern)))

(defn head->clj [pattern input output? state result->body]
  (if (rest? pattern)
    (*pattern->clj* pattern input output? state result->body)
    `(when ~input
       ~(*pattern->clj* pattern `(first ~input) output? state
                        (fn [output remaining state]
                          (when-nil remaining
                                    (result->body output `(next ~input) state)))))))

(defn cons->clj [pattern first rest]
  (if (rest? pattern)
    `(concat ~first ~rest)
    `(cons ~first ~rest)))

(defn conj->clj [pattern last rest]
  (if (rest? pattern)
    `(apply conj ~rest ~last)
    `(conj ~rest ~last)))

;; --- VALUE PATTERNS ---

(defn seq->clj [patterns input output? state result->body]
  (if-let [[first-pattern & rest-pattern] (seq patterns)]
    (head->clj first-pattern input output? state
               (fn [first-output first-remaining state]
                 (seq->clj rest-pattern first-remaining output? state
                           (fn [rest-output rest-remaining state]
                             (result->body (cons->clj first-pattern first-output rest-output) rest-remaining state)))))
    (result->body nil input state)))

(defn vec->clj [patterns index input output? state result->body]
  (if (< index (count patterns))
    (*pattern->clj* (nth patterns index) `(nth ~input ~index) output? state
                    (fn [index-output index-remaining state]
                      (when-nil index-remaining
                                (vec->clj patterns (inc index) input output? state
                                          (fn [vec-output vec-remaining state]
                                            (result->body (vec (cons index-output vec-output)) vec-remaining state))))))
    (result->body [] `(seq (subvec ~input ~index)) state)))

(defn map->clj [patterns input output? state result->body]
  (if-let [[[key value-pattern] & rest-pattern] (seq patterns)]
    (*pattern->clj* value-pattern `(get ~input ~key) output? state
                    (fn [value-output value-remaining state]
                      (when-nil value-remaining
                                (map->clj rest-pattern input output? state
                                          (fn [rest-output _ state]
                                            (result->body `(assoc ~rest-output ~key ~value-output) nil state))))))
    (result->body input nil state)))

(extend-protocol IPattern
  nil
  (fmap [this f]
    this)
  (pattern->clj [this input output? state result->body]
    `(when (nil? ~input)
       ~(result->body nil nil state)))
  Object
  (fmap [this f]
    this)
  (pattern->clj [this input output? state result->body]
    `(when (= ~input '~this)
       ~(result->body input nil state)))
  clojure.lang.ISeq
  (fmap [this f]
    (map f this))
  (pattern->clj [this input output? state result->body]
    `(when (seq? ~input)
       ~(seq->clj this `(seq ~input) output? state result->body)))
  clojure.lang.IPersistentVector
  (fmap [this f]
    (vec (map f this)))
  (pattern->clj [this input output? state result->body]
    `(when (vector? ~input)
       ~(if (some rest? this)
          (seq->clj this `(seq ~input) output? state
                    (fn [output remaining state] (result->body `(vec ~output) remaining state)))
          `(when (>= (count ~input) ~(count this))
             ~(vec->clj this 0 input output? state result->body)))))
  clojure.lang.IPersistentMap
  (fmap [this f]
    (map-vals f this))
  (pattern->clj [this input output? state result->body]
    `(when (instance? clojure.lang.IPersistentMap ~input)
       ~(map->clj this input output? state result->body))))

(defrecord Seqable [patterns]
  IPattern
  (fmap [this f]
    (->Seqable (map f this)))
  (pattern->clj [this input output? state result->body]
    `(when (instance? clojure.lang.Seqable ~input)
       ~(seq->clj patterns `(seq ~input) output? state result->body))))

;; --- LOGICAL PATTERNS ---

(defrecord Any []
  IPattern
  (fmap [this f]
    this)
  (pattern->clj [this input output? state result->body]
    (result->body input nil state)))

(defrecord Is [f]
  IPattern
  (fmap [this f]
    this)
  (pattern->clj [this input output? state result->body]
    `(when (~f ~input)
       ~(result->body input nil state))))

(defrecord Guard [pattern fnk]
  IPattern
  (fmap [this f]
    (->Guard (f pattern) fnk))
  (pattern->clj [this input output? state result->body]
    (let [[args call] (fnk->clj fnk)]
      (*pattern->clj* pattern input output?
                      (apply assoc state (interleave args (repeat :free)))
                      (fn [output remaining state]
                        (assert (every? #(= :bound (state %)) args)
                                (pr-str "All free variables in the guard must be bound in the enclosed pattern:" this state args))
                        `(when ~call
                           ~(result->body output remaining state)))))))

(defrecord Bind [symbol pattern]
  IPattern
  (fmap [this f]
    (->Bind symbol (f pattern)))
  (pattern->clj [this input output? state result->body]
    (assert (symbol? symbol))
    (if (= :free (state symbol))
      (*pattern->clj* pattern input true
                      (assoc state symbol :bound)
                      (fn [output remaining state]
                        `(let [~symbol ~output]
                           ~(result->body symbol remaining state))))
      (*pattern->clj* pattern input output? state result->body))))

(defrecord Output [pattern fnk]
  IPattern
  (fmap [this f]
    (->Output (f pattern) fnk))
  (pattern->clj [this input output? state result->body]
    (let [[args call] (fnk->clj fnk)]
      (*pattern->clj* (->Bind '&output pattern) input false
                      (reduce #(assoc %1 %2 :free) state args)
                      (fn [_ remaining state]
                        (assert (every? #(= :bound (state %)) args)
                                (pr-str "All free variables in the output must be bound in the enclosed pattern:" this state args))
                        (result->body call remaining state))))))

(defn bound-since [old-state new-state]
  (for [key (keys new-state)
        :when (= :bound (new-state key))
        :when (not= :bound (old-state key))]
    key))

(defrecord Or [patterns]
  IPattern
  (fmap [this f]
    (->Or (map f patterns)))
  (pattern->clj [this input output? state result->body]
    (assert patterns (pr-str "Or cannot be empty: " this))
    (let [states (atom #{})
          branches (doall (for [pattern patterns]
                            (*pattern->clj* pattern input output? state
                                            (fn [output remaining state']
                                              (swap! states conj state')
                                              (apply vector output remaining (bound-since state state'))))))
          ;; TODO will need some kind of merge here if we allow other things in state
          _ (assert (= 1 (count @states)) (pr-str "All patterns in Or must have the same set of bindings: " this @states))
          state' (first @states)]
      (with-syms [output remaining]
        `(when-let [~(apply vector output remaining (bound-since state state')) (or ~@branches)]
           ~(result->body output remaining state'))))))

(defrecord And [patterns]
  IPattern
  (fmap [this f]
    (->And (map f patterns)))
  (pattern->clj [this input output? state result->body]
    (assert patterns (pr-str "And cannot be empty: " this))
    (let [[first-pattern & rest-pattern] (seq patterns)]
      (if rest-pattern
        (*pattern->clj* first-pattern input output? state
                        (fn [_ _ state] (*pattern->clj* (->And rest-pattern) input output? state result->body)))
        (*pattern->clj* first-pattern input output? state result->body)))))

(defrecord ZeroOrMore [pattern]
  IPattern
  (fmap [this f]
    (->ZeroOrMore (f pattern)))
  (pattern->clj [this input output? state result->body]
    (with-syms [loop-output loop-remaining output remaining]
      (let [binding (if output? [output remaining] [remaining])
            output-acc (when output? (conj->clj pattern output loop-output))
            states (atom #{})
            loop-body (head->clj pattern loop-remaining output? state
                                 (fn [output remaining state']
                                   (swap! states conj state')
                                   (if output? [output remaining] [remaining])))
            _ (assert (= 1 (count @states)) (pr-str "result->body should be called exactly once: " pattern))
            state' (first @states)]
        `(when (or (nil? ~input) (seq? ~input))
           (loop [~loop-output [] ~loop-remaining (seq ~input)]
             (if-let [~binding (and ~loop-remaining ~loop-body)]
               (recur ~output-acc ~remaining)
               ~(result->body `(seq ~loop-output) loop-remaining state'))))))))

(defrecord WithMeta [pattern meta-pattern]
  IPattern
  (fmap [this f]
    (->WithMeta (f pattern) (f meta-pattern)))
  (pattern->clj [this input output? state result->body]
    (*pattern->clj* pattern input output? state
                    (fn [output remaining state]
                      (*pattern->clj* meta-pattern `(meta ~input) output? state
                                      (fn [meta-output meta-remaining state]
                                        (when-nil meta-remaining
                                                  (result->body
                                                   `(if (nil? ~meta-output) ~output (with-meta ~output ~meta-output))
                                                   remaining state))))))))

(defrecord View [form]
  IPattern
  (fmap [this f]
    this)
  (pattern->clj [this input output? state result->body]
    (with-syms [view-output view-remaining]
      `(when-let [[~view-output ~view-remaining] (~form ~input)]
         ~(result->body view-output view-remaining state)))))

(defn pattern->view
  ([pattern]
     (pattern->view 'fn pattern))
  ([name pattern]
      (with-syms [input]
        `(~name [~input]
                (binding [~@(aconcat (::bindings (meta pattern)))]
                  ~(*pattern->clj* pattern input true {} (fn [output remaining _] [output remaining])))))))
