(ns strucjure.view
  (:require [strucjure.util :as util]
            [strucjure.pattern :as pattern]))

;; TODO when we come to doing loops consider passing output? so the loop can decide whether or not to
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

(defn seq->clj [pattern input used? result->body]
  (if-let [[first-pattern & rest-pattern] pattern]
    (if (instance? strucjure.pattern.Rest first-pattern)
      (pattern->clj first-pattern input used?
                    (fn [first-output first-remaining]
                      (seq->clj rest-pattern first-remaining used?
                                (fn [rest-output rest-remaining]
                                  (result->body `(concat ~first-output ~rest-output) rest-remaining)))))
      (util/let-syms [first-input rest-input]
                     `(when-let [[~first-input & ~rest-input] ~input]
                        ~(pattern->clj first-pattern first-input used?
                                       (fn [first-output first-remaining]
                                         (when-nil first-remaining
                                                   (seq->clj rest-pattern rest-input used?
                                                             (fn [rest-output rest-remaining]
                                                               (result->body `(cons ~first-output ~rest-output) rest-remaining)))))))))
    (result->body nil input)))

(extend-protocol IView
  Object
  (pattern->clj [this input used? result->body]
    `(when (= ~input '~this)
       ~(result->body input nil)))
  clojure.lang.ISeq
  (pattern->clj [this input used? result->body]
    `(when (seq? ~input)
       ~(seq->clj this input used? result->body)))
  strucjure.pattern.Bind
  (pattern->clj [this input used? result->body]
    (if (used? (:symbol this))
      `(let [~(:symbol this) ~input]
         ~(result->body input nil))
      (result->body input nil)))
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
    (let [[first-pattern & rest-pattern] (:patterns this)]
      (if rest-pattern
        (pattern->clj first-pattern input used? (fn [_ _] (pattern->clj (pattern/->And rest-pattern) input used? result->body)))
        (pattern->clj first-pattern input used? result->body))))
  strucjure.pattern.Rest
  (pattern->clj [this input used? result->body]
    (throw (Exception. (str "Compiling strucjure.pattern.Rest outside of seq: " this))))
  strucjure.pattern.View
  (pattern->clj [this input used? result->body]
    (util/let-syms [view-output view-remaining]
                   `(when-let [[~view-output ~view-remaining] (~(:form this) ~input)]
                      ~(result->body view-output view-remaining)))))

(defn pattern->view [pattern]
  (util/let-syms [input]
                 `(fn [~input]
                    ~(pattern->clj pattern input #{:output}
                                   (fn [output remaining]
                                     [output remaining])))))

(comment
  (use 'strucjure.pattern)
  (use 'clojure.stacktrace)
  (e)
  (pattern->clj (list (->Bind 'a)) 'input #{} (fn [output remaining] [output remaining]))
  (pattern->clj (list (->Bind 'a)) 'input #{'a} (fn [output remaining] [output remaining]))
  (pattern->view (list (->Bind 'a)))
  (pattern->view (->And [(->Bind 'a) 1]))
  (pattern->view (->And [(->Bind 'a) 1 2]))
  (pattern->view (->And [(->Bind 'a) (->Bind 'b)]))
  ((eval (pattern->view (->And [(->Bind 'a) (->Bind 'b)]))) 1)
  ((eval (pattern->view (->And [1 (->Bind 'b)]))) 1)
  ((eval (pattern->view (->And [1 (->Bind 'b)]))) 2)
  (pattern->view (list 1 2))
  ((eval (pattern->view (list 1 2))) (list 1 2))
  ((eval (pattern->view (list 1 2))) (list 1))
  ((eval (pattern->view (list 1 2))) (list 1 2 3))
  ((eval (pattern->view (list 1 2))) (list 1 3))
  ((eval (pattern->view (list 1 2))) [1 2])
  ((eval (pattern->view (list 1 2))) 1)
  (let [a (eval (pattern->view 1))] (pattern->view (list (->View a) 2)))
  (let [a (eval (pattern->view 1))] ((eval (pattern->view (list (->View a) 2))) (list 1 2 3)))
)
