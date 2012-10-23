(ns strucjure.view
  (:require [strucjure.pattern :as pattern]))

(defprotocol View
  "A view takes an input and either fails or consumes some/all of the input and returns an output."
  (run* [this input]
    "Run the view with the given input. Return [remaining-input output] on success or nil on failure."))

(defrecord NoMatch [view input])
(defrecord PartialMatch [view input remaining output])

(defn run [this input]
  (if-let [[remaining output] (run* this input)]
    (if (nil? remaining)
      output
      (throw+ (PartialMatch. view input remaining output)))
    (throw+ (NoMatch. view input))))

(defrecord Import* [view-src pattern]
  strucjure.pattern.AST
  (with-scope [this scope]
    (assert (= nil view-fun))
    (let [view-fun (eval `(fn [] ~view-src))] ; don't give view-src access to the bindings - enforces context-free
      (pass-scope #(->Import view-src view-fun %) pattern scope))))

(defrecord Import [view-src view-fun pattern]
  strucjure.pattern.Pattern
  (run* [this input bindings]
    (when-let [[remaining output] (run* (view-fun) input)]
      (when (nil? remaining)
        (run* pattern output bindings)))))

(defrecord Match [pattern result-fun]
  strucjure.view.View
  (run* [this input]
    (if-let [[remaining bindings] (pattern/run input)]
      [remaining (result-fun bindings)])))

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
            (recur views)
            result))))))

(defrecord ZeroOrMore [view]
  View
  (run* [this input]
    (when (or
           (nil? input)
           (instance? clojure.lang.Seqable input))
      (loop [elems (seq input)
             outputs nil]
        (or (when-let [[elem elems] elems]
              (when-let [[remaining output] (run* view elem)]
                (when (nil? remaining)
                  (recur elems (cons output outputs)))))
            [elems (reverse outputs)])))))

(defrecord ZeroOrMorePrefix [view]
  View
  (run* [this input]
    (loop [input input
           outputs nil]
      (if-let [[remaining output] (run* view input)]
        (if (nil? remaining)
          [nil (reverse outputs)]
          (recur remaining (cons output outputs)))
        [input (reverse outputs)]))))

(def not ->Not)
(defn and [& views] (->And views))
(defn or [& views] (->Or views))
(def zero-or-more ->ZeroOrMore)
(def zero-or-more-prefix ->ZeroOrMorePrefix)
