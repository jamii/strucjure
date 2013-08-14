(ns strucjure.view
  (:require [strucjure.util :as util]
            [strucjure.pattern :as pattern]))

;; TODO when we come to doing loops consider passing output? so the loop can decide whether or not to

(defn when-nil [form body]
  (if (nil? form)
    body
    `(when (nil? ~form) ~body)))

(defprotocol IView
  (pattern->clj [this input result->body]))

(defn pattern->resultss [pattern]
  (let [results (atom #{})]
    (pattern->clj pattern 'input (fn [output remaining] (swap! results conj [output remaining])))
    @results))

(defn seq->clj [pattern input result->body]
  (if-let [[first-pattern & rest-pattern] pattern]
    (if (instance? strucjure.pattern.& first-pattern)
      (pattern->clj first-pattern input
                    (fn [first-output first-remaining]
                      (seq->clj rest-pattern first-remaining
                                (fn [rest-output rest-remaining]
                                  (result->body `(concat ~first-output ~rest-output) rest-remaining)))))
      (util/let-syms [first-input rest-input]
                     `(when-let [[~first-input & ~rest-input] ~input]
                        ~(pattern->clj first-pattern first-input
                                       (fn [first-output first-remaining]
                                         (when-nil first-remaining
                                                   (seq->clj rest-pattern rest-input
                                                             (fn [rest-output rest-remaining]
                                                               (result->body `(cons ~first-output ~rest-output) rest-remaining)))))))))
    (result->body nil input)))

(extend-protocol IView
  Object
  (pattern->clj [this input result->body]
    `(when (= ~input '~this)
       ~(result->body input nil)))
  clojure.lang.ISeq
  (pattern->clj [this input result->body]
    `(when (seq? ~input)
       ~(seq->clj this input result->body)))
  strucjure.pattern.Bind
  (pattern->clj [this input result->body]
    `(let [~(:symbol this) ~input]
       ~(result->body input nil)))
  strucjure.pattern.Or
  (pattern->clj [this input result->body]
    (let [results (map pattern->results (:patterns this))]
      (if (every? #(= #{[input nil]} %) results)
        `(when (or ~@(for [pattern (:patterns this)]
                       (pattern->clj pattern input (fn [_ _] true))))
           ~(result->body input nil))
        `(or ~@(for [pattern (:patterns this)]
                 (pattern->clj pattern input result->body))))))
  strucjure.pattern.And
  (pattern->clj [this input result->body]
    (let [[first-pattern & rest-pattern] (:patterns this)]
      (if rest-pattern
        (pattern->clj first-pattern input (fn [_ _] (pattern->clj (pattern/->And rest-pattern) input result->body)))
        (pattern->clj first-pattern input result->body))))
  strucjure.pattern.Rest
  (pattern->clj [this input result->body]
    (throw (Exception. (str "Compiling strucjure.pattern.Rest outside of seq: " this))))
  strucjure.pattern.View
  (pattern->clj [this input result->body]
    (util/let-syms [view-output view-remaining]
                   `(when-let [[~view-output ~view-remaining] (~(:form this) ~input)]
                      ~(result->body view-output view-remaining)))))

(defn pattern->view [pattern]
  (util/let-syms [input]
                 `(fn [~input]
                    ~(pattern->clj pattern input
                                   (fn [output remaining]
                                     [output remaining])))))

(comment
  (use 'strucjure.pattern)
  (use 'clojure.stacktrace)
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
