(ns strucjure.view
  (:use [slingshot.slingshot :only [try+ throw+]])
  (:require [strucjure.pattern :as pattern]
            [strucjure.util :as util]))

(defprotocol View
  "A view takes an input and either fails or consumes some/all of the input and returns an output."
  (run* [this input opts]
    "Run the view with the given input. Return [remaining-input output] on success or nil on failure."))

(extend-protocol View
  strucjure.pattern.Pattern
  (run* [this input opts]
    (pattern/run this input {} opts)))

(defrecord Raw [f]
  View
  (run* [this input opts]
    (let [output (f input opts)]
      (assert (or (nil? output)
                  (and (sequential? output)
                       (= 2 (count output)))))
      output)))

(defn run [view input opts]
  (run* view input opts))

(defrecord NoMatch [view input])
(defrecord PartialMatch [view input remaining output])

(defn run-or-throw
  ([view input]
     (run-or-throw view input {}))
  ([view input opts]
     (if-let [[remaining output] (run view input opts)]
       (if (nil? remaining)
         output
         (throw+ (PartialMatch. view input remaining output)))
       (throw+ (NoMatch. view input)))))

(defrecord Import [view-fun pattern]
  strucjure.pattern.AST
  (with-scope [this scope]
    (pattern/pass-scope (fn [pattern] `(->Import (fn [] ~view-fun) ~pattern)) pattern scope))
  strucjure.pattern.Pattern
  (run* [this input bindings opts]
    (when-let [[remaining output] (run (view-fun) input)]
      (when-let [[remaining* new-bindings] (pattern/run pattern output bindings opts)]
        (when (nil? remaining*)
          [remaining new-bindings])))))

(defrecord Match [pattern result-fun]
  strucjure.view.View
  (run* [this input opts]
    (if-let [[remaining bindings] (pattern/run pattern input {} opts)]
      [remaining (result-fun input bindings)])))

(defrecord Not [view]
  View
  (run* [this input opts]
    (if-let [result (run view input opts)]
      nil
      [nil nil])))

(defrecord Or [views]
  View
  (run* [this input opts]
    (loop [views views]
      (when-let [[view & views] views]
        (if-let [result (run view input opts)]
          result
          (recur views))))))

(defrecord And [views]
  View
  (run* [this input opts]
    (let [[view views] views]
      (loop [view view
             views views]
        (when-let [result (run view input opts)]
          (if-let [[view & views] views]
            (recur view views)
            result))))))

(defrecord ZeroOrMore [view]
  View
  (run* [this input opts]
    (when (or
           (nil? input)
           (instance? clojure.lang.Seqable input))
      (loop [elems (seq input)
             outputs nil]
        (if-let [[elem & elems] elems]
          (if-let [[remaining output] (run view elem opts)]
            (if (nil? remaining)
              (recur elems (cons output outputs))
              [(cons elem elems) (reverse outputs)])
            [(cons elem elems) (reverse outputs)])
          [nil (reverse outputs)])))))

(defrecord ZeroOrMorePrefix [view]
  View
  (run* [this input opts]
    (loop [input input
           outputs nil]
      (if-let [[remaining output] (run view input opts)]
        (let [outputs (cons output outputs)]
          (if (nil? remaining)
            [nil (reverse outputs)]
            (recur remaining outputs)))
        [input (reverse outputs)]))))

(def zero-or-more ->ZeroOrMore)
(def zero-or-more-prefix ->ZeroOrMorePrefix)

(defrecord Named [name view]
  View
  (run* [this input opts]
    (let [input ((get opts :pre-view util/null-pre-view) name input)]
      (when-let [[remaining output] (run view input opts)]
        (let [output ((get opts :post-view util/null-post-view) name output)]
          [remaining output])))))
