(ns strucjure.graph
  (:require [clojure.set :refer [union]]
            [plumbing.core :refer [fnk for-map map-vals aconcat]]
            [strucjure.pattern :as pattern]))

;; TODO call stack may become a problem

(defn dependencies [pattern]
  (if (instance? strucjure.pattern.Edge pattern)
    #{(:name pattern)}
    (union (map dependencies (pattern/subpatterns pattern)))))

(defn named-edge [pattern]
  (if (instance? strucjure.pattern.Edge pattern)
    (pattern/->Name (:name pattern) pattern)
    pattern))

(defn with-named-edges [graph]
  (with-meta
    (map-vals #(pattern/postwalk % named-edge) graph)
    (meta graph)))

(defn with-named-nodes [graph]
  (with-meta
    (for-map [[name pattern] graph] name (pattern/->Name name pattern))
    (meta graph)))
