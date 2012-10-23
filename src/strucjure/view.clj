(ns strucjure.view
  (:use [slingshot.slingshot :only [try+ throw+]])
  (:require [strucjure.pattern :as pattern]))

(defprotocol View
  "A view takes an input and either fails or consumes some/all of the input and returns an output."
  (run* [this input]
    "Run the view with the given input. Return [remaining-input output] on success or nil on failure."))

(defrecord Raw [f]
  View
  (run* [this input]
    (let [output (f input)]
      (assert (or (nil? output)
                  (and (sequential? output)
                       (= 2 (count output)))))
      output)))

(defrecord NoMatch [view input])
(defrecord PartialMatch [view input remaining output])

(defn run [view input]
  (if-let [[remaining output] (run* view input)]
    (if (nil? remaining)
      output
      (throw+ (PartialMatch. view input remaining output)))
    (throw+ (NoMatch. view input))))

(defrecord Import [view-fun pattern]
  strucjure.pattern.AST
  (with-scope [this scope]
    (pattern/pass-scope (fn [pattern] `(->Import (fn [] ~view-fun) ~pattern)) pattern scope))
  strucjure.pattern.Pattern
  (run* [this input bindings]
    (when-let [[remaining output] (run* (view-fun) input)]
      (when (nil? remaining)
        (pattern/run* pattern output bindings)))))

(defrecord Match [pattern result-fun]
  strucjure.view.View
  (run* [this input]
    (if-let [[remaining bindings] (pattern/run pattern input)]
      [remaining (result-fun input bindings)])))

(defrecord Not [view]
  View
  (run* [this input]
    (if-let [result (run* view input)]
      nil
      [nil nil])))

(defrecord Or [views]
  View
  (run* [this input]
    (loop [views views]
      (when-let [[view & views] views]
        (if-let [result (run* view input)]
          result
          (recur views))))))

(defrecord And [views]
  View
  (run* [this input]
    (let [[view views] views]
      (loop [view view
             views views]
        (when-let [result (run* view input)]
          (if-let [[view & views] views]
            (recur view views)
            result))))))

(defrecord ZeroOrMore [view]
  View
  (run* [this input]
    (when (or
           (nil? input)
           (instance? clojure.lang.Seqable input))
      (loop [elems (seq input)
             outputs nil]
        (if-let [[elem elems] elems]
          (if-let [[remaining output] (run* view elem)]
            (if (nil? remaining)
              (recur elems (cons output outputs))
              [elems (reverse outputs)])
            [elems (reverse outputs)])
          [elems (reverse outputs)])))))

(defrecord ZeroOrMorePrefix [view]
  View
  (run* [this input]
    (loop [input input
           outputs nil]
      (if-let [[remaining output] (run* view input)]
        (let [outputs (cons output outputs)]
          (if (nil? remaining)
            [nil (reverse outputs)]
            (recur remaining outputs)))
        [input (reverse outputs)]))))

(def zero-or-more ->ZeroOrMore)
(def zero-or-more-prefix ->ZeroOrMorePrefix)
