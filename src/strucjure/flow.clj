(ns strucjure.flow
  (:require [clojure.set :refer [intersection union]]
            [clojure.walk :refer [walk]]
            [plumbing.core :refer [for-map]]
            [strucjure.util :as util]))

;; TODO could probably simplify some of this by using the clj analyser to track bindings

;; the clj forms in these records must not cause side-effects
(defrecord And [flows])
(defrecord Or [flows])
(defrecord Let [binding value])
(defrecord Equal [left right])
(defrecord Test [form])

(defn constant? [form]
  (let [constant (atom true)]
    (clojure.walk/prewalk
     (fn [form]
       (if (list? form)
         (when (not= 'quote (first form))
           (reset! constant false))
         form))
     form)
    @constant))

(defn binds [flow]
  (condp = (type flow)
    And (apply union (map binds (:flows flow)))
    Or (apply intersection (map binds (:flows flow)))
    Let (util/syms (:binding flow)) ;; TODO this is a bit inaccurate
    #{}))

(defn lets [flow]
  (condp = (type flow)
    And (apply union (map lets (:flows flow)))
    Or (apply union (map lets (:flows flow)))
    Let #{flow}
    #{}))

(defn remove-flows [flow flows]
  (when-not (contains? flows flow)
    (condp = (type flow)
      And (->And (map #(remove-flows % flows) (:flows flow)))
      Or (->Or (map #(remove-flows % flows) (:flows flow)))
      flow)))

(defn replace-syms [flow sym->value]
  (let [replace #(clojure.walk/prewalk-replace sym->value %)]
    (condp = (type flow)
      And (->And (map #(replace-syms % sym->value) (:flows flow)))
      Or (->Or (map #(replace-syms % sym->value) (:flows flow)))
      Let (->Let (:binding flow) (replace (:value flow)))
      Equal (->Equal (replace (:left flow)) (replace (:right flow)))
      Test (->Test (replace (:form flow)))
      nil nil)))

(defn propagate-constants* [[flow body]]
  (let [constant-lets (filter #(and (symbol? (:binding %)) (constant? (:value %))) (lets flow))
        sym->value (for-map [constant-let constant-lets] (:binding constant-let) (:value constant-let))]
    [(-> flow
         (remove-flows (set constant-lets))
         (replace-syms sym->value))
     (clojure.walk/prewalk-replace sym->value body)]))

(defn propagate-constants [flow body]
  (loop [val [flow body]]
    (let [new-val (propagate-constants* val)]
      (if (= val new-val)
        new-val
        (recur new-val)))))

(defn preeval-equals [flow]
  (condp = (type flow)
    And (->And (map preeval-equals (:flows flow)))
    Or (->Or (map preeval-equals (:flows flow)))
    Equal (when-not (and (constant? (:left flow))
                         (constant? (:right flow))
                         (= (:left flow) (:right flow)))
            flow)
    flow))

(defn collapse-when* [form]
  (cond
   (and (seq? form) (= `when (nth form 0)) (= true (nth form 2))) (nth form 1)
   (and (seq? form) (= `when (nth form 0)) (= true (nth form 1))) (nth form 2)
   :else form))

(defn collapse-when [form]
  (clojure.walk/postwalk collapse-when* form))

(defn flow->clj* [flow body]
  (condp = (type flow)
    nil body

    And (if-let [[sub-flow & sub-flows] (:flows flow)]
          (flow->clj* sub-flow (flow->clj* (->And sub-flows) body))
          body)

    ;; TODO test this against the naive version once we have some realistic patterns
    Or (let [sub-flows-syms (for [sub-flow (:flows flow)]
                              ;; these are the syms which the body can observe from the flow
                              (intersection (binds sub-flow) (util/syms body)))
             flow-syms (first sub-flows-syms)]
         ;; this should already have been checked in strucjure.pattern, but best be sure
         (assert (every? #(= flow-syms %) sub-flows-syms) "All flows in an Or must bind the same set of symbols")
         (if (empty? flow-syms)
           ;; this is the easy case
           `(when (or ~@(for [sub-flow (:flows flow)] (flow->clj* sub-flow true)))
              ~body)
           ;; otherise have to export some symbols from the (or ...)
           `(when-let [~(vec flow-syms) (or ~@(for [sub-flow (:flows flow)] (flow->clj* sub-flow (vec flow-syms))))]
              ~body)))

    Let (if (some (util/syms (:binding flow)) (util/syms body)) ;; if flow binding is used anywhere
          `(let [~(:binding flow) ~(:value flow)] ~body)
          body)

    Equal `(when (= ~(:left flow) ~(:right flow)) ~body)

    Test `(when ~(:form flow) ~body)))

(defn flow->clj [flow body]
  (let [[flow body] (propagate-constants flow body)
        flow (preeval-equals flow)
        clj (flow->clj* flow body)
        clj (collapse-when clj)]
    clj))

(comment
  (use 'clojure.stacktrace)
  ;; (pattern (1 2))
  (constant? '{:a 1 :b [2 3 '(for [x y] x)]})
  (constant? '{:a 1 :b [2 3 (+ 4 5)]})
  (def a (->And [(->Test '(seq? input))
                 (->And [(->Test '(not (nil? input)))
                         (->Let '[first_1 rest_1] 'input)
                         (->Equal 'first_1 1)
                         (->Let 'output_1 'first_1)
                         (->Let 'remaining_1 'rest_1)])
                 (->And [(->Test '(not (nil? remaining_1)))
                         (->Let '[first_2 rest_2] 'rest_1)
                         (->Equal 'first_2 2)
                         (->Let 'output_2 'first_2)
                         (->Let 'remaining_2 'rest_2)])
                 (->Let 'remaining 'remaining_2)
                 (->Let 'output '(list output_1 output_2))]))
  (flow->clj a '[remaining output])
  (flow->clj a '[remaining])
  (flow->clj a true)
  (flow->clj (->Or [(->Equal 'input 1) (->Equal 'input 2)]) true)
  (flow->clj (->Or [(->And [(->Let 'output 'input) (->Equal 'input 1)])
                    (->And [(->Let 'output 'input) (->Equal 'input 2)])])
             ['output])
)
