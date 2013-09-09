(ns strucjure.graph
  (:require [clojure.set :refer [union]]
            [plumbing.core :refer [fnk for-map map-vals aconcat]]
            [strucjure.pattern :as pattern]))

;; TODO get-in with-deepest-error
;; TODO call stack may become a problem
;; TODO allow parts of the graph to take args eg bindable in sugar

(defn dependencies [pattern]
  (if (instance? strucjure.pattern.Node pattern)
    #{(:name pattern)}
    (union (map dependencies (pattern/subpatterns pattern)))))

(defn output-in [graph & names&fnks]
  (apply assoc graph
         (aconcat (for [[name fnk] (partition 2 names&fnks)]
                    [name (pattern/->Output (graph name) fnk)]))))

(defn named-node [pattern]
  (if (instance? strucjure.pattern.Node pattern)
    (pattern/->Name (:name pattern) pattern)
    pattern))

(defn with-named-inner-nodes [graph]
  (with-meta (map-vals #(pattern/postwalk % named-node)) (meta graph)))

(defn with-named-outer-nodes [graph]
  (with-meta
    (for-map [[name pattern] graph] name (pattern/->Name name pattern))
    (meta graph)))
