(ns strucjure.pattern
  (:use [slingshot.slingshot :only [try+ throw+]])
  (:require [strucjure.util :as util]))

(defprotocol AST
  "An Abstract Syntax Tree for a pattern"
  (scope [this]
    "Return the set of symbols which this pattern will bind on success.")
  (with-scope [this scope]
    "Given a surrounding scope, return code which evals to the correct pattern."))

(defprotocol Pattern
  "A pattern takes an input and either fails or consumes some/all of the input and returns a set of bindings"
  (run* [this input bindings opts]
    "Run the pattern with the given input and bindings. Return [remaining-input new-bindings] on success or nil on failure."))

(defn run [pattern input bindings opts]
  (run* pattern input bindings opts))

(defn with-chained-scope [patterns init-scope]
  (let [scopes (map scope patterns)
        chained-scopes (butlast (reductions clojure.set/union init-scope scopes))]
    (vec (map with-scope patterns chained-scopes))))

(defrecord Literal [value]
  AST
  (scope [this]
    #{})
  (with-scope [this scope]
    `(->Literal ~value))
  Pattern
  (run* [this input bindings opts]
    (if (= input value)
      [nil bindings]
      nil)))

(defrecord Ignore []
  AST
  (scope [this]
    #{})
  (with-scope [this scope]
    `(->Ignore))
  Pattern
  (run* [this input bindings opts]
    [nil bindings]))

(defrecord Guard [fun]
  AST
  (scope [this]
    #{})
  (with-scope [this scope]
    `(->Guard ~(util/src-with-scope fun scope)))
  Pattern
  (run* [this input bindings opts]
    (if (fun input bindings)
      [nil bindings]
      nil)))

(defrecord Bind [symbol]
  AST
  (scope [this]
    #{symbol})
  (with-scope [this scope]
    (if (contains? scope symbol)
      ;; if already bound, test for equality
      (with-scope (->Guard `(= ~symbol ~util/input-sym)) scope)
      ;; otherwise bind symbol
      `(->Bind '~symbol)))
  Pattern
  (run* [this input bindings opts]
    [nil (assoc bindings symbol input)]))

(defrecord Head [pattern]
  AST
  (scope [this]
    (scope pattern))
  (with-scope [this scope]
    `(->Head ~(with-scope pattern scope)))
  Pattern
  (run* [this input bindings opts]
    (when-let [[head & tail] input]
      (when-let [[remaining new-bindings :as result] (run pattern head bindings opts)]
        (when (nil? remaining)
          [tail new-bindings])))))

(defrecord Map [keys&patterns]
  AST
  (scope [this]
    (apply clojure.set/union (map scope (map second keys&patterns))))
  (with-scope [this scope]
    (let [keys (map first keys&patterns)
          patterns (map second keys&patterns)
          with-scoped-patterns (with-chained-scope patterns scope)]
      `(->Map ~(vec (map vector keys with-scoped-patterns)))))
  Pattern
  (run* [this input bindings opts]
    (when (associative? input)
      (loop [keys&patterns keys&patterns
             bindings bindings]
        (if-let [[[key pattern] & keys&patterns] keys&patterns]
          (let [value (get input key ::not-found)]
            (when (not (= ::not-found value))
              (when-let [[remaining new-bindings] (run pattern value bindings opts)]
                (when (nil? remaining)
                  (recur keys&patterns new-bindings)))))
          [nil bindings])))))

(defrecord Record [class-name patterns]
  AST
  (scope [this]
    (apply clojure.set/union (map scope patterns)))
  (with-scope [this scope]
    `(->Record ~class-name ~(with-chained-scope patterns scope)))
  Pattern
  (run* [this input bindings opts]
    (when (instance? class-name input)
      (loop [patterns patterns
             args (vals input)
             bindings bindings]
        (if-let [[pattern & patterns] patterns]
          (when-let [[arg & args] args]
            (when-let [[remaining new-bindings] (run pattern arg bindings opts)]
              (when (nil? remaining)
                (recur patterns args new-bindings))))
          [nil bindings])))))

(defrecord Regex [regex]
  AST
  (scope [this]
    #{})
  (with-scope [this scope]
    `(->Regex ~regex))
  Pattern
  (run* [this input bindings opts]
    (when-let [_ (re-find regex input)]
      [nil bindings])))

(defrecord Total [pattern]
  AST
  (scope [this]
    (scope pattern))
  (with-scope [this scope]
    `(->Total ~(with-scope pattern scope)))
  Pattern
  (run* [this input bindings opts]
    (when-let [[remaining _ :as result] (run pattern input bindings opts)]
      (when (nil? remaining)
        result))))

(defrecord Not [pattern]
  AST
  (scope [this]
    #{}) ;; new lexical scope :(
  (with-scope [this scope]
    `(->Not ~(with-scope pattern scope)))
  Pattern
  (run* [this input bindings opts]
    (if-let [result (run pattern input bindings opts)]
      nil
      [nil bindings])))

(defrecord Or [patterns]
  AST
  (scope [this]
    (let [scopes (map scope patterns)]
      (assert (apply = scopes) "All sub-patterns of an 'or' pattern must have the same bindings")
      (first scopes)))
  (with-scope [this scope]
    `(->Or ~(vec (map #(with-scope % scope) patterns))))
  Pattern
  (run* [this input bindings opts]
    (loop [patterns patterns]
      (when-let [[pattern & patterns-rest] patterns]
        (if-let [result (run pattern input bindings opts)]
          result
          (recur patterns-rest))))))

(defrecord And [patterns]
  AST
  (scope [this]
    (apply clojure.set/union (map scope patterns)))
  (with-scope [this scope]
    `(->And ~(with-chained-scope patterns scope)))
  Pattern
  (run* [this input bindings opts]
    (if-let [[pattern & patterns-rest] (seq patterns)]
      (loop [pattern pattern
             patterns patterns
             bindings bindings]
        (when-let [[remaining new-bindings :as result] (run pattern input bindings opts)]
          (if-let [[pattern & patterns] patterns]
            (recur pattern patterns new-bindings)
            result)))
      [input bindings])))

(defrecord Chain [patterns]
  AST
  (scope [this]
    (apply clojure.set/union (map scope patterns)))
  (with-scope [this scope]
    `(->Chain ~(with-chained-scope patterns scope)))
  Pattern
  (run* [this input bindings opts]
    (if-let [[pattern & patterns] (seq patterns)]
      (loop [pattern pattern
             patterns patterns
             input input
             bindings bindings]
        (when-let [[remaining new-bindings :as result] (run pattern input bindings opts)]
          (if-let [[pattern & patterns] patterns]
            (recur pattern patterns remaining new-bindings)
            result)))
      [input bindings])))

(defrecord Seq [pattern]
  AST
  (scope [this]
    (scope pattern))
  (with-scope [this scope]
    `(->Seq ~(with-scope pattern scope)))
  Pattern
  (run* [this input bindings opts]
    (when (or
           (nil? input)
           (instance? clojure.lang.Seqable input))
      (run pattern (seq input) bindings opts))))

(defn prefix [& patterns] (->Seq (->Chain patterns)))
(defn seqable [& patterns] (->Total (apply prefix patterns)))

(defrecord Named [name pattern]
  Pattern
  (run* [this input bindings opts]
    (let [input ((get opts :pre-view util/null-pre-view) name input)]
      (when-let [[remaining new-bindings] (run pattern input bindings opts)]
        (let [new-bindings ((get opts :post-view util/null-post-view) name new-bindings)]
          [remaining new-bindings])))))
