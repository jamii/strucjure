(ns strucjure.flow
  (:require [clojure.set :refer [union]]
            [strucjure.util :as util]))

(defprotocol IFlow
  (restrict [this syms] "Remove bindings which are not transitively depended on by syms and are not needed by a test")
  (flow->clj [this body] "A clj form which returns body on success or nil/false on failure"))

(extend-protocol IFlow
  nil ;; makes it easy to nil out nested flows
  (restrict [this syms]
    this)
  (flow->clj [this body]
    body))

(defn when-flow [form body]
  (cond
   (= true body) form
   (= false body) `(not ~form)
   :else `(when ~form ~body)))

(defrecord And [flows]
  IFlow
  (restrict [this syms]
    (->And (reverse (map #(restrict % syms) (reverse flows)))))
  (flow->clj [this body]
    (if-let [[flow & flows] flows]
      (flow->clj flow (flow->clj (->And flows) body))
      body)))

(defrecord LetOr [syms flows]
  IFlow
  (restrict [this syms]
    ;; LetOr branches cannot share bindings so no need to run restrict in parallel
    (->LetOr (filter @syms syms) (map #(restrict % syms) flows)))
  (flow->clj [this body] ;; TODO can optimise if no Let/LetOr in flows
    (if (empty? syms)
      (when-flow `(or ~@(for [flow flows] (flow->clj flow true)))
                 body)
      `(when-let [~(vec syms) (or ~@(for [flow flows] (flow->clj flow (vec syms))))]
         ~body))))

(defrecord Let [binding value]
  IFlow
  (restrict [this syms]
    (when (some @syms (util/syms binding))
      (swap! syms union (util/syms value))
      this))
  (flow->clj [this body]
    `(let [~binding ~value]
       ~body)))

(defrecord Constant [sym value]
  IFlow
  (restrict [this syms]
    (swap! syms conj sym)
    this)
  (flow->clj [this body]
    (if (and (not (symbol? sym)) (= sym value))
      body
      (when-flow `(= ~sym ~value) body))))

(defrecord Test [form]
  IFlow
  (restrict [this syms]
    (swap! syms union (util/syms form))
    this)
  (flow->clj [this body]
    (when-flow form body)))

(defn flow->restrict->clj [flow body]
  (flow->clj (restrict flow (atom (util/syms body))) body))

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
  (flow->restrict->clj a '[remaining output])
  (flow->restrict->clj a '[remaining])
  (flow->restrict->clj a true)
)
