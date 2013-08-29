(ns strucjure.debug
  (:require [plumbing.core :refer [for-map]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern]
            [strucjure.graph :as graph]))

;; TODO the ugliness here is due to not being able to detect failure of an individual pattern
;;      because the compiler is written in CPS with no failure continuation
(defrecord Trace [pattern name input-fn success-fn failure-fn]
  strucjure.pattern.IPattern
  (pattern->clj [this input output? state result->body]
    (with-syms [success?]
      (letfn [(traced-result->body [output remaining state]
                (with-syms [output-sym remaining-sym]
                  `(let [~output-sym ~output
                         ~remaining-sym ~remaining]
                     (reset! ~success? true)
                     (~success-fn '~name ~output-sym ~remaining-sym)
                     ~(result->body output-sym remaining-sym state))))]
        `(let [~success? (atom false)]
           (~input-fn '~name ~input)
           (let [result# ~(pattern/*pattern->clj* pattern input output? state traced-result->body)]
             (when-not @~success? (~failure-fn '~name))
             result#))))))

(defn pattern-with-trace [pattern input-fn success-fn failure-fn]
  (let [traced-pattern (pattern/fmap pattern #(pattern-with-trace % input-fn success-fn failure-fn))
        metad-pattern (if (meta pattern) (with-meta traced-pattern (meta pattern)) traced-pattern)]
    (->Trace metad-pattern (pr-str pattern) input-fn success-fn failure-fn)))

(defn graph-with-trace [graph input-fn success-fn failure-fn]
  (with-meta
    (for-map [[name pattern] graph] name (->Trace pattern name input-fn success-fn failure-fn))
    (meta graph)))

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

(defn pattern-with-print-trace [pattern]
  (-> pattern (pattern-with-trace print-input print-success print-failure) (pattern/with-binding `*depth* 0)))

(defn graph-with-print-trace [graph]
  (-> graph (graph-with-trace print-input print-success print-failure) (graph/with-binding `*depth* 0)))
