(ns strucjure.debug
  (:require [plumbing.core :refer [for-map]]
            [strucjure.pattern :as pattern]
            [strucjure.graph :as graph]))

(defrecord Trace [pattern name enter-fn exit-fn]
  strucjure.pattern.IPattern
  (pattern->clj [this input output? state result->body]
    `(let [~'_ (~enter-fn '~name ~input)
           result# ~(pattern/*pattern->clj* pattern input output? state result->body)
           ~'_ (~exit-fn '~name result#)]
       result#)))

(defn pattern-with-trace [pattern enter-fn exit-fn]
  (let [traced-pattern (pattern/fmap pattern #(pattern-with-trace % enter-fn exit-fn))
        metad-pattern (if (meta pattern) (with-meta traced-pattern (meta pattern)) traced-pattern)]
    (->Trace metad-pattern (pr-str pattern) enter-fn exit-fn)))

(defn graph-with-trace [graph enter-fn exit-fn]
  (with-meta
    (for-map [[name pattern] graph] name (->Trace pattern name enter-fn exit-fn))
    (meta graph)))

(def ^:dynamic *depth*)

(defn- indent [n]
  (apply str (repeat (* 4 n) \ )))

(defn print-enter [name input]
  (println (indent *depth*) "=>" name input)
  (set! *depth* (inc *depth*))
  input)

(defn print-exit [name result]
  (set! *depth* (dec *depth*))
  (if result
    (println (indent *depth*) "<=" name result)
    (println (indent *depth*) "X" name))
  result)

(defn pattern-with-print-trace [pattern]
  (-> pattern (pattern-with-trace print-enter print-exit) (pattern/with-binding `*depth* 0)))

(defn graph-with-print-trace [graph]
  (-> graph (graph-with-trace print-enter print-exit) (graph/with-binding `*depth* 0)))
