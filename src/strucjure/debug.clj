(ns strucjure.debug
  (:require [plumbing.core :refer [for-map]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern]
            [strucjure.graph :as graph]))

(def ^:dynamic *depth* 0)

(defn- indent [n]
  (apply str (repeat (* 4 n) \ )))

(defn print-input [name input]
  (println (indent *depth*) "=>" name input))

(defn print-success [name output remaining]
  (println (indent *depth*) "<=" name output remaining))

(defn print-failure [name failure]
  (println (indent *depth*) "X" name failure))

(defn print-trace [pattern name]
  (if (instance? strucjure.pattern.Rest pattern)
    pattern
    (pattern/->Trace pattern name print-input print-success print-failure)))

(defn pattern-with-trace [pattern]
  (let [traced-pattern (pattern/fmap pattern pattern-with-trace)]
    (if (instance? strucjure.pattern.Rest pattern)
      traced-pattern
      (print-trace traced-pattern (pr-str pattern)))))

;; TODO where do we put the bindings?
(defn graph-with-trace [graph input-fn]
  (with-meta
    (for-map [[name pattern] graph] name (print-trace pattern name))
    (meta graph)))
