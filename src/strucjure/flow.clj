(ns strucjure.flow
  (:require [clojure.set :refer [intersection union]]
            [strucjure.util :as util]))

(defn when->clj [form body]
  (cond
   (= true body) form
   (= false body) false
   :else `(when ~form ~body)))

(defprotocol IFlow
  (binds [this]
    "The set of symbols bound by this flow")
  (flow->clj [this body]
    "A clj form which returns body on success or nil/false on failure."))

(extend-protocol IFlow
  nil ;; makes it easy to nil out nested flows
  (binds [this]
    #{})
  (flow->clj [this body]
    body))

(defrecord And [flows]
  IFlow
  (binds [this]
    (apply union (map binds flows)))
  (flow->clj [this body]
    (if-let [[flow & flows] flows]
      (flow->clj flow (flow->clj (->And flows) body))
      body)))

;; TODO test this against the naive version once we have some realistic patterns
(defrecord Or [flows]
  IFlow
  (binds [this]
    (apply intersection (map binds flows)))
  (flow->clj [this body]
    (let [flows-syms (for [flow flows]
                       ;; these are the syms which the body can observe from the flow
                       (intersection (binds flow) (util/syms body)))
          syms (first flows-syms)]
      ;; this should already have been checked in strucjure.pattern, but best be sure
      (assert (every? #(= syms %) flows-syms) "All flows in an Or must export the same set of symbols")
      (if (empty? syms)
        ;; this is the easy case
        (when->clj `(or ~@(for [flow flows] (flow->clj flow true)))
                   body)
        ;; otherise have to export some symbols from the (or ...)
        `(when-let [~(vec syms) (or ~@(for [flow flows] (flow->clj flow (vec syms))))]
           ~body)))))

(defrecord Let [binding value] ;; value must not cause side-effects
  IFlow
  (binds [this]
    (util/syms binding))
  (flow->clj [this body]
    (if (some (util/syms binding) (util/syms body)) ;; if this binding is used anywhere
      `(let [~binding ~value] ~body)
      body)))

(defrecord Constant [sym value]
  IFlow
  (binds [this]
    #{})
  (flow->clj [this body]
    (if (and (not (symbol? sym)) (= sym value))
      body
      (when->clj `(= ~sym ~value) body))))

(defrecord Test [form]
  IFlow
  (binds [this]
    #{})
  (flow->clj [this body]
    (when->clj form body)))

(comment
  (use 'clojure.stacktrace)
  ;; (pattern (1 2))
  (def a (->And [(->Test '(seq? input))
                 (->And [(->Test '(not (nil? input)))
                         (->Let '[first_1 rest_1] 'input)
                         (->Constant 'first_1 1)
                         (->Let 'output_1 'first_1)
                         (->Let 'remaining_1 'rest_1)])
                 (->And [(->Test '(not (nil? remaining_1)))
                         (->Let '[first_2 rest_2] 'rest_1)
                         (->Constant 'first_2 2)
                         (->Let 'output_2 'first_2)
                         (->Let 'remaining_2 'rest_2)])
                 (->Let 'remaining 'remaining_2)
                 (->Let 'output '(list output_1 output_2))]))
  (flow->clj a '[output remaining])
  (let [syms (atom #{'output 'remaining})] [(restrict a syms) @syms])
  (= a (restrict a (atom #{'output 'remaining})))
  (restrict a (atom #{'remaining}))
  (flow->clj a '[remaining output])
  (flow->clj a '[remaining])
  (flow->clj a true)
  (flow->clj (->Or [(->Constant 'input 1) (->Constant 'input 2)]) true)
  (flow->clj (->Or [(->And [(->Let 'output 'input) (->Constant 'input 1)])
                    (->And [(->Let 'output 'input) (->Constant 'input 2)])])
             ['output])
)
