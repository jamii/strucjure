(ns strucjure.pattern
  (:require clojure.set
            [strucjure.util :refer [when-nil let-syms free-syms]]))

;; TODO Record
;; TODO Set? (how would you match subpatterns? maybe only allow bind/with-meta? or only value patterns)
;; TODO Atom/Ref/Agent? (what would the output be?)
;; TODO when gen is added, pattern->clj will be a poor name
;; TODO add a value? method for more efficent handling of patterns like [1 2 3]
;;      something like (if (contains? instance? IView) pattern->clj value->clj)
;; TODO need a general way to indicate that output is unchanged for eg WithMeta
;;      just check (= input output)?
;; TODO let &input and &output in Output, Is, When

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

(defn cons->clj [pattern output rest]
  (if (instance? Rest pattern)
    `(concat ~output ~rest)
    `(cons ~output ~rest)))

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

(defn map->clj [patterns input used? result->body]
  (if-let [[[key value-pattern] & rest-pattern] (seq patterns)]
    (pattern->clj value-pattern `(get ~input ~key) used?
                  (fn [value-output value-remaining]
                    (when-nil value-remaining
                              (map->clj rest-pattern input used?
                                        (fn [rest-output _]
                                          (result->body (assoc rest-output key value-output) nil))))))
    (result->body {} input)))

(defn vec->clj [patterns index input used? result->body]
  (if (< index (count patterns))
    (pattern->clj (nth patterns index) `(nth ~input ~index) used?
                  (fn [index-output index-remaining]
                    (when-nil index-remaining
                              (vec->clj patterns (inc index) input used?
                                        (fn [vec-output vec-remaining]
                                          (result->body (vec (cons index-output vec-output)) vec-remaining))))))
    (result->body [] input)))

(extend-protocol IPattern
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
          (seq->clj this input used? result->body)
          (vec->clj this 0 input used? result->body))))
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
    `(when (let [~'% ~input] ~form)
       ~(result->body input nil))))

(defrecord Guard [pattern form]
  IPattern
  (pattern->clj [this input used? result->body]
    (pattern->clj pattern input
                  (clojure.set/union used? (free-syms form))
                  (fn [output remaining]
                    `(when ~form
                       ~(result->body output remaining))))))

(defrecord Bind [pattern symbol]
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
        `(when (or ~@(for [pattern patterns]
                       (pattern->clj pattern input used? (fn [_ _] true))))
           ~(result->body input nil))
        `(or ~@(for [pattern patterns]
                 (pattern->clj pattern input used? result->body)))))))

(defrecord And [patterns]
  IPattern
  (pattern->clj [this input used? result->body]
    (if-let [[first-pattern & rest-pattern] patterns]
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
                    output-acc (when (used? :output) (cons->clj pattern output loop-output))]
                `(when (seq? ~input)
                   (loop [~loop-output nil ~loop-remaining (seq ~input)]
                     (if-let [~binding ~(head->clj pattern loop-remaining used? return)]
                       (recur ~output-acc ~remaining)
                       ~(result->body `(reverse ~loop-output) loop-remaining))))))))

(defrecord WithMeta [pattern meta-pattern]
  IPattern
  (pattern->clj [this input used? result->body]
    (pattern->clj pattern input used?
                  (fn [output remaining]
                    (pattern->clj meta-pattern `(meta ~input) used?
                                  (fn [meta-output meta-remaining]
                                    (when-nil meta-remaining
                                              (result->body `(with-meta ~output ~meta-output) remaining))))))))

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
  (pattern->view (->ZeroOrMore 1))
  (pattern->view (->Output (->ZeroOrMore 1) ''ones))
  (pattern->view (->Output (->Bind (->ZeroOrMore 1) 'a) 'a))
  (let [[out rem] ((eval (pattern->view (->WithMeta (->Any) {:foo true}))) ^:foo [])]
    (meta out))
  (pattern->view [1 2 (->Bind (->Any) 'a)])
  (pattern->view (->Output [1 2 (->Bind (->Any) 'a)] 'a))
  (pattern->view [1 2 (->Rest (->Any))])
  (pattern->view {1 2 3 (->Bind (->Any) 'a)})
  (pattern->view (->Output {1 2 3 (->Bind (->Any) 'a)} 'a))
  )
