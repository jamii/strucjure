(ns strucjure.debug
  (:require [plumbing.core :refer [for-map]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern]
            [strucjure.graph :as graph]))

(def ^:dynamic *depth*)

(defn- indent [n]
  (apply str (repeat (* 4 n) \ )))

(defn print-input [name input]
  (println (indent *depth*) "=>" name input)
  (set! *depth* (inc *depth*)))

(defn print-success [name output remaining]
  (set! *depth* (dec *depth*))
  (println (indent *depth*) "<=" name output remaining))

(defn print-failure [name]
  (set! *depth* (dec *depth*))
  (println (indent *depth*) "X" name))

(defn print-trace [pattern name]
  (pattern/->Trace pattern print-input print-success print-failure))

(defn pattern-with-trace [pattern input-fn success-fn failure-fn]
  (pattern/->Binding *depth* 0  (pattern/postwalk pattern #(print-trace % (pr-str %)))))

(defn graph-with-trace [graph input-fn]
  (with-meta
    (for-map [[name pattern] graph] name (print-trace pattern name))
    (meta graph)))
