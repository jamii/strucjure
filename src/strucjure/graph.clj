(ns strucjure.graph
  (:require [plumbing.core :refer [for-map]]
            [strucjure.util :refer [walk-replace walk-collect]]
            [strucjure.view :refer [pattern->view]]))

(defrecord Node [form]
  strucjure.view/View
  (pattern->decision [this input bound]
    (view->decision (.form this) input bound)))

(defn graph->fns [graph]
  (vec (for [[name pattern] graph] (pattern->view name pattern))))

(defn graph->views [graph]
  `(letfn ~(graph->fns graph)
     ~(for-map [name (keys graph)] `'~name name)))

(defn graph->view [name graph]
  `(letfn ~(graph->fns graph)
     ~name))

;; TODO graph sugar is just (let [foo (->View foo)] (pattern ...))

(comment
  (use 'strucjure.pattern 'clojure.pprint)
  (def eg-num
    {'num (->Or ['zero (list 'succ (->Node 'num))])})
  (graph->fns eg-num)
  (pprint (graph->views eg-num))
  (eval (graph->views eg-num))
  (def num ('num (eval (graph->views eg-num))))
  (num 'zero)
  (num 'foo)
  (num ('succ 'zero))
  ((:num eg-num) eg-vars)
  (alter-var-root (:num eg-vars) ((:num eg-num) eg-vars))
  (:num (graph->view eg-num)))
