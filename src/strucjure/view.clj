(ns strucjure.view
  (:require clojure.set
            [strucjure.util :as util]
            [strucjure.pattern :as pattern]))

;; TODO when gen is added, pattern->clj will be a poor name
;; TODO add a value? method for more efficent handling of patterns like [1 2 3]
;;      something like (if (contains? instance? IView) pattern->clj value->clj)
;; TODO need a general way to indicate that output is unchanged for eg WithMeta
;;      just check (= input output)?
;; TODO let &input and &output in Output

(defn when-nil [form body]
  (if (nil? form)
    body
    `(when (nil? ~form) ~body)))

(defprotocol IView
  (pattern->clj [this input used? result->body]))

(defn pattern->results [pattern used?]
  (let [results (atom #{})]
    (pattern->clj pattern 'input used? (fn [output remaining] (swap! results conj [output remaining])))
    @results))

(defn head->clj [pattern input used? result->body]
  (if (instance? strucjure.pattern.Rest pattern)
    (pattern->clj (:pattern pattern) input used? result->body)
    (util/let-syms [first-input rest-input]
                   `(when-let [[~first-input & ~rest-input] ~input]
                      ~(pattern->clj pattern first-input used?
                                     (fn [output remaining]
                                       (when-nil remaining
                                                 (result->body output rest-input))))))))

(defn cons->clj [pattern output rest]
  (if (instance? strucjure.pattern.Rest pattern)
    `(concat ~output ~rest)
    `(cons ~output ~rest)))

(defn seq->clj* [patterns input used? result->body]
  (if-let [[first-pattern & rest-pattern] (seq patterns)]
    (head->clj first-pattern input used?
               (fn [first-output first-remaining]
                 (seq->clj* rest-pattern first-remaining used?
                           (fn [rest-output rest-remaining]
                             (result->body (cons->clj first-pattern first-output rest-output) rest-remaining)))))
    (result->body nil input)))

(defn seq->clj [patterns input used? result->body]
   (util/let-syms [seq-input]
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

(extend-protocol IView
  Object
  (pattern->clj [this input used? result->body]
    `(when (= ~input '~this)
       ~(result->body input nil)))
  strucjure.pattern.Any
  (pattern->clj [this input used? result->body]
    (result->body input nil))
  strucjure.pattern.Is
  (pattern->clj [this input used? result->body]
    `(when (let [~'% ~input] ~(:form this))
       ~(result->body input nil)))
  strucjure.pattern.Guard
  (pattern->clj [this input used? result->body]
    (pattern->clj (:pattern this) input
                  (clojure.set/union used? (util/free-syms (:form this)))
                  (fn [output remaining]
                    `(when ~(:form this)
                       ~(result->body output remaining)))))
  strucjure.pattern.Seqable
  (pattern->clj [this input used? result->body]
    `(when (instance? clojure.lang.Seqable ~input)
       ~(seq->clj this input used? result->body)))
  clojure.lang.ISeq
  (pattern->clj [this input used? result->body]
    `(when (seq? ~input)
       ~(seq->clj this input used? result->body)))
  clojure.lang.IPersistentVector
  (pattern->clj [this input used? result->body]
    `(when (vector? ~input)
       ~(if (some #(instance? strucjure.pattern.Rest %) this)
          (seq->clj this input used? result->body)
          (vec->clj this 0 input used? result->body))))
  clojure.lang.IPersistentMap
  (pattern->clj [this input used? result->body]
    `(when (instance? clojure.lang.IPersistentMap ~input)
       ~(map->clj this input used? result->body)))
  strucjure.pattern.WithMeta
  (pattern->clj [this input used? result->body]
    (pattern->clj (:pattern this) input used?
                  (fn [output remaining]
                    (pattern->clj (:meta-pattern this) `(meta ~input) used?
                                  (fn [meta-output meta-remaining]
                                    (when-nil meta-remaining
                                              (result->body `(with-meta ~output ~meta-output) remaining)))))))
  strucjure.pattern.Bind
  (pattern->clj [this input used? result->body]
    (if (used? (:symbol this))
      (pattern->clj (:pattern this) input (conj used? :output)
                    (fn [output remaining]
                      `(let [~(:symbol this) ~output]
                         ~(result->body (:symbol this) remaining))))
      (pattern->clj (:pattern this) input used? result->body)))
  strucjure.pattern.Output
  (pattern->clj [this input used? result->body]
    (pattern->clj (:pattern this) input
                  (clojure.set/union (disj used? :output) (util/free-syms (:form this)))
                  (fn [_ remaining] (result->body (:form this) remaining))))
  strucjure.pattern.Or
  (pattern->clj [this input used? result->body]
    (let [results (map #(pattern->results % used?) (:patterns this))]
      (if (every? #(= #{[input nil]} %) results)
        `(when (or ~@(for [pattern (:patterns this)]
                       (pattern->clj pattern input used? (fn [_ _] true))))
           ~(result->body input nil))
        `(or ~@(for [pattern (:patterns this)]
                 (pattern->clj pattern input used? result->body))))))
  strucjure.pattern.And
  (pattern->clj [this input used? result->body]
    (if-let [[first-pattern & rest-pattern] (:patterns this)]
      (if rest-pattern
        (pattern->clj first-pattern input used? (fn [_ _] (pattern->clj (pattern/->And rest-pattern) input used? result->body)))
        (pattern->clj first-pattern input used? result->body))
      (throw (Exception. "Empty strucjure.pattern.And"))))
  strucjure.pattern.Rest
  (pattern->clj [this input used? result->body]
    (throw (Exception. (str "Compiling strucjure.pattern.Rest outside of seq: " this))))
  strucjure.pattern.ZeroOrMore
  (pattern->clj [this input used? result->body]
    (util/let-syms [loop-output loop-remaining output remaining]
                   (let [binding (if (used? :output) [output remaining] [remaining])
                         return (fn [output remaining] (if (used? :output) [output remaining] [remaining]))
                         output-acc (when (used? :output) (cons->clj (:pattern this) output loop-output))]
                     `(when (seq? ~input)
                        (loop [~loop-output nil ~loop-remaining (seq ~input)]
                          (if-let [~binding ~(head->clj (:pattern this) loop-remaining used? return)]
                            (recur ~output-acc ~remaining)
                            ~(result->body `(reverse ~loop-output) loop-remaining)))))))
  strucjure.pattern.View
  (pattern->clj [this input used? result->body]
    (util/let-syms [view-output view-remaining]
                   `(when-let [[~view-output ~view-remaining] (~(:form this) ~input)]
                      ~(result->body view-output view-remaining)))))

(defn pattern->view [pattern]
  (util/let-syms [input]
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
