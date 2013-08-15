(ns strucjure.view
  (:require clojure.set
            [strucjure.util :as util]
            [strucjure.pattern :as pattern]))

;; TODO add a value? method for more efficent handling of patterns like [1 2 3]

(defn when-nil [form body]
  (if (nil? form)
    body
    `(when (nil? ~form) ~body)))

(defn let-output [sym value body]
  (if sym
    `(let [~sym ~value] ~body)
    ~body))

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
  clojure.lang.ISeq
  (pattern->clj [this input used? result->body]
    (util/let-syms [seq-input]
                   `(when (seq? ~input)
                      (let [~seq-input (seq ~input)]
                        ~(pattern->clj (pattern/->Seq this) seq-input used? result->body)))))
  strucjure.pattern.Seq
  (pattern->clj [this input used? result->body]
    (if-let [[first-pattern & rest-pattern] (seq (:patterns this))]
      (head->clj first-pattern input used?
                 (fn [first-output first-remaining]
                   (pattern->clj (pattern/->Seq rest-pattern) first-remaining used?
                                 (fn [rest-output rest-remaining]
                                   (result->body (cons->clj first-pattern first-output rest-output) rest-remaining)))))
      (result->body nil input)))
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
  )
