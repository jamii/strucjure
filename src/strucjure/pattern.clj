(ns strucjure.pattern
  (:require clojure.set
            [strucjure.util :refer [when-nil let-syms free-syms]]))

;; TODO Record
;; TODO Set? (how would you match subpatterns? maybe only allow bind/with-meta? or only value patterns)
;; TODO Atom/Ref/Agent? (what would the output be?)
;; TODO when gen is added, pattern->clj will be a poor name
;; TODO add a value? method for more efficent handling of patterns like [1 2 3]
;;      something like (if (contains-instance? pattern IView) pattern->clj value->clj)
;; TODO need a general way to indicate that output is unchanged for eg WithMeta
;;      just check (= input output)?
;; TODO let &input and &output in Output, Is, When -- actually, that breaks output optimisations
;; TODO maybe pass bindings like output/remaining and only let them when used in forms
;;      or maybe just use closures and stop trying to imitate them in code - what is the overhead?
;;      ^{::used #{'a 'b}} (fn [a b] ...)
;;      also fixes the Or optimisation - sufficient to check if bindings is same in each case (Or should let-input)
;;      but can't embed closures in the generated code
;;      bound vs free. this is what I'm giving you and this is what I want from you
;;      (with-env usable form)
;;      still hard to quasiquote correctly - what if the underlying pattern is changed? probably best not to change the quoting because then it won't match user assumptions?
;;      could just be explicit about which symbols we expect? not terrible for output-in but a mess for guard
;;      really have two different cases - could have implicit in sugar and explicit in raw
;; TODO need to be careful about reusing input - a (let-sym [input `(meta input)] ...) would be useful here
;;      let-syms -> with-syms, then use let-sym
;; TODO pattern debugger via *pattern->clj*

(defprotocol IPattern
  (pattern->clj [this input used? result->body]))

;; --- REST ---

(defrecord Rest [pattern]
  IPattern
  (pattern->clj [this input used? result->body]
    (throw (Exception. (str "Compiling strucjure.pattern.Rest outside of seq: " this)))))

(defn head->clj [pattern input used? result->body]
  (if (instance? Rest pattern)
    (pattern->clj (:pattern pattern) input used? result->body)
    (let-syms [first-input rest-input]
              `(when-let [[~first-input & ~rest-input] ~input]
                 ~(pattern->clj pattern first-input used?
                                (fn [output remaining]
                                  (when-nil remaining
                                            (result->body output rest-input))))))))

(defn cons->clj [pattern first rest]
  (if (instance? Rest pattern)
    `(concat ~first ~rest)
    `(cons ~first ~rest)))

(defn conj->clj [pattern last rest]
  (if (instance? Rest pattern)
    `(apply conj ~rest ~last)
    `(conj ~rest ~last)))

;; --- VALUE PATTERNS ---

(defn seq->clj* [patterns input used? result->body]
  (if-let [[first-pattern & rest-pattern] (seq patterns)]
    (head->clj first-pattern input used?
               (fn [first-output first-remaining]
                 (seq->clj* rest-pattern first-remaining used?
                            (fn [rest-output rest-remaining]
                              (result->body (cons->clj first-pattern first-output rest-output) rest-remaining)))))
    (result->body nil input)))

(defn seq->clj [patterns input used? result->body]
  (let-syms [seq-input]
            `(let [~seq-input (seq ~input)]
               ~(seq->clj* patterns seq-input used? result->body))))

(defn vec->clj [patterns index input used? result->body]
  (if (< index (count patterns))
    (pattern->clj (nth patterns index) `(nth ~input ~index) used?
                  (fn [index-output index-remaining]
                    (when-nil index-remaining
                              (vec->clj patterns (inc index) input used?
                                        (fn [vec-output vec-remaining]
                                          (result->body (vec (cons index-output vec-output)) vec-remaining))))))
    (result->body [] `(seq (subvec ~input ~index)))))

(defn map->clj [patterns input used? result->body]
  (if-let [[[key value-pattern] & rest-pattern] (seq patterns)]
    (pattern->clj value-pattern `(get ~input ~key) used?
                  (fn [value-output value-remaining]
                    (when-nil value-remaining
                              (map->clj rest-pattern input used?
                                        (fn [rest-output _]
                                          (result->body `(assoc ~rest-output ~key ~value-output) nil))))))
    (result->body input nil)))

(extend-protocol IPattern
  nil
  (pattern->clj [this input used? result->body]
    `(when (nil? ~input)
       ~(result->body nil nil)))
  Object
  (pattern->clj [this input used? result->body]
    `(when (= ~input '~this)
       ~(result->body input nil)))
  clojure.lang.ISeq
  (pattern->clj [this input used? result->body]
    `(when (seq? ~input)
       ~(seq->clj this input used? result->body)))
  clojure.lang.IPersistentVector
  (pattern->clj [this input used? result->body]
    `(when (vector? ~input)
       ~(if (some #(instance? Rest %) this)
          (seq->clj this input used?
                    (fn [output remaining] (result->body `(vec ~output) remaining)))
          `(when (>= (count ~input) ~(count this))
             ~(vec->clj this 0 input used? result->body)))))
  clojure.lang.IPersistentMap
  (pattern->clj [this input used? result->body]
    `(when (instance? clojure.lang.IPersistentMap ~input)
       ~(map->clj this input used? result->body))))

(defrecord Seqable [patterns]
  IPattern
  (pattern->clj [this input used? result->body]
    `(when (instance? clojure.lang.Seqable ~input)
       ~(seq->clj patterns input used? result->body))))

;; --- LOGICAL PATTERNS ---

(defrecord Any []
  IPattern
  (pattern->clj [this input used? result->body]
    (result->body input nil)))

(defrecord Is [form]
  IPattern
  (pattern->clj [this input used? result->body]
    `(when (let [~'&input ~input] ~form)
       ~(result->body input nil))))

(defrecord Guard [pattern form]
  IPattern
  (pattern->clj [this input used? result->body]
    (pattern->clj pattern input
                  (clojure.set/union used? (free-syms form))
                  (fn [output remaining]
                    `(when ~form
                       ~(result->body output remaining))))))

(defrecord Bind [symbol pattern]
  IPattern
  (pattern->clj [this input used? result->body]
    (if (used? symbol)
      (pattern->clj pattern input (conj used? :output)
                    (fn [output remaining]
                      `(let [~symbol ~output]
                         ~(result->body symbol remaining))))
      (pattern->clj pattern input used? result->body))))

(defrecord Output [pattern form]
  IPattern
  (pattern->clj [this input used? result->body]
    (pattern->clj pattern input
                  (clojure.set/union (disj used? :output) (free-syms form))
                  (fn [_ remaining] (result->body form remaining)))))

(defn pattern->results [pattern used?]
  (let [results (atom #{})]
    (pattern->clj pattern 'input used? (fn [output remaining] (swap! results conj [output remaining])))
    @results))

(defrecord Or [patterns]
  IPattern
  (pattern->clj [this input used? result->body]
    (let [results (map #(pattern->results % used?) patterns)]
      (if (every? #(= #{[input nil]} %) results)
        ;; TODO is this safe for binding?
        `(when (or ~@(for [pattern patterns]
                       (pattern->clj pattern input used? (fn [_ _] true))))
           ~(result->body input nil))
        `(or ~@(for [pattern patterns]
                 (pattern->clj pattern input used? result->body)))))))

(defrecord And [patterns]
  IPattern
  (pattern->clj [this input used? result->body]
    (if-let [[first-pattern & rest-pattern] (seq patterns)]
      (if rest-pattern
        (pattern->clj first-pattern input used? (fn [_ _] (pattern->clj (->And rest-pattern) input used? result->body)))
        (pattern->clj first-pattern input used? result->body))
      (throw (Exception. "Empty And")))))

(defrecord ZeroOrMore [pattern]
  IPattern
  (pattern->clj [this input used? result->body]
    (let-syms [loop-output loop-remaining output remaining]
              (let [binding (if (used? :output) [output remaining] [remaining])
                    return (fn [output remaining] (if (used? :output) [output remaining] [remaining]))
                    output-acc (when (used? :output) (conj->clj pattern output loop-output))]
                `(when (or (nil? ~input) (seq? ~input))
                   (loop [~loop-output [] ~loop-remaining (seq ~input)]
                     (if-let [~binding (and ~loop-remaining ~(head->clj pattern loop-remaining used? return))]
                       (recur ~output-acc ~remaining)
                       ~(result->body `(seq ~loop-output) loop-remaining))))))))

(defrecord WithMeta [pattern meta-pattern]
  IPattern
  (pattern->clj [this input used? result->body]
    (let-syms [input-meta]
              (pattern->clj pattern input used?
                            (fn [output remaining]
                              (pattern->clj meta-pattern `(meta ~input) used?
                                            (fn [meta-output meta-remaining]
                                              (when-nil meta-remaining
                                                        (result->body `(if (nil? ~meta-output) ~output (with-meta ~output ~meta-output)) remaining)))))))))

(defrecord View [form]
  IPattern
  (pattern->clj [this input used? result->body]
    (let-syms [view-output view-remaining]
              `(when-let [[~view-output ~view-remaining] (~form ~input)]
                 ~(result->body view-output view-remaining)))))

(defn pattern->view [pattern]
  (let-syms [input]
            `(fn [~input]
               ~(pattern->clj pattern input #{:output} (fn [output remaining] [output remaining])))))

(comment
  (use 'strucjure.pattern)
  (use 'clojure.stacktrace)
  (e)
  (pattern->view (->Bind 1 'a))
  (pattern->view (->Output (->Bind 1 'a) '(+ a 1)))
  (pattern->view (list 1 2))
  ((eval (pattern->view (list 1 2))) (list 1 2))
  ((eval (pattern->view (list 1 2))) (list 1))
  ((eval (pattern->view (list 1 2))) (list 1 2 3))
  ((eval (pattern->view (list 1 2))) (list 1 3))
  ((eval (pattern->view (list 1 2))) [1 2])
  ((eval (pattern->view (list 1 2))) 1)
  (let [a (eval (pattern->view 1))] (pattern->view (list (->View a) 2)))
  (let [a (eval (pattern->view 1))] ((eval (pattern->view (list (->View a) 2))) (list 1 2 3)))
  ((eval (pattern->view (list))) (list 1 2))
  (pattern->clj (->ZeroOrMore 1) 'input #{} (fn [output remaining] [remaining]))
  (pattern->clj (->ZeroOrMore 1) 'input #{:output} (fn [output remaining] [output remaining]))
  ((eval (pattern->view (->ZeroOrMore 1))) (list))
  ((eval (pattern->view (->ZeroOrMore 1))) (list 2))
  ((eval (pattern->view (->ZeroOrMore 1))) (list 1 1))
  ((eval (pattern->view (->ZeroOrMore 1))) (list 1 1 2))
  ((eval (pattern->view (->ZeroOrMore (->Or [1 2])))) (list 1 2 1 2))
  (pattern->view (->ZeroOrMore 1))
  (pattern->view (->Output (->ZeroOrMore 1) ''ones))
  (pattern->view (->Output (->Bind (->ZeroOrMore 1) 'a) 'a))
  (let [[out rem] ((eval (pattern->view (->WithMeta (->Any) {:foo true}))) ^:foo [])]
    (meta out))
  ((eval (pattern->view [1 2])) [1])
  ((eval (pattern->view [1 2])) [1 2])
  ((eval (pattern->view [1 2])) [1 2 3])
  ((eval (pattern->view [1 2])) [1 3])
  (pattern->view [1 2 (->Bind (->Any) 'a)])
  (pattern->view (->Output [1 2 (->Bind (->Any) 'a)] 'a))
  (pattern->view [1 2 (->Rest (->Any))])
  (pattern->view {1 2 3 (->Bind (->Any) 'a)})
  (pattern->view (->Output {1 2 3 (->Bind (->Any) 'a)} 'a))
  ((eval (pattern->view (list (->Rest (->Bind 'elems (->ZeroOrMore (->Rest (->Any)))))))) (list 1 2 3))
  ((eval (pattern->view (->Seqable [(->Rest (->ZeroOrMore [(->Any) (->Any)]))]))) '{:foo 1 :bar (& * 3)})
  ((eval (pattern->view (->And [{} (->Bind 'elems (->Seqable [(->Rest (->ZeroOrMore [(->Any) (->Any)]))]))]))) '{:foo 1 :bar (& * 3)})
  ((eval (pattern->view [(->Any) (->Any)])) (first (seq '{:foo 1 :bar (& * 3)})))
  (eval (pattern->view (->Output (list (->WithMeta (->Bind 'prefix (->Or ['*]))) (->Rest (->View 'inc))) 'prefix)))
  )
