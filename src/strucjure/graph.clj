(ns strucjure.graph
  (:require [plumbing.core :refer [for-map]]
            [strucjure.view :as view]))

(defn graph->fns [graph]
  (vec (for [[name pattern] graph] (view/pattern->view name pattern))))

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
    {'num (->Or ['zero (list 'succ (->View 'num))])})
  (graph->fns eg-num)
  (graph->views eg-num)
  (eval (graph->views eg-num))
  (def num ('num (eval (graph->views eg-num))))
  (num 'zero)
  (num 'foo)
  (num (list 'succ 'zero))
  (num (list 'succ (list 'succ 'zero)))
  (num (list 'succ (list 'succ 'succ)))
  )
