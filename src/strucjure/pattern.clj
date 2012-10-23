(ns strucjure.pattern
  (:require [strucjure.util :as util]))

(defprotocol AST
  "An Abstract Syntax Tree for a pattern"
  (with-scope [this scope]
    "Compile the pattern ast into a pattern. Return [pattern new-scope]."))

 (defprotocol Pattern
  "A pattern takes an input and either fails or consumes some/all of the input and returns a set of bindings"
  (run* [this input bindings]
    "Run the pattern with the given input and bindings. Return [remaining-input new-bindings] on success or nil on failure."))

(defn run [pattern input]
  (run* pattern input {}))

(defn pass-scope [constructor pattern scope]
  (let [[new-pattern new-scope] (with-scope pattern)]
    [(constructor new-pattern) new-scope]))

(defn chain-scope [constructor patterns scope]
  (let [chained-scope (atom scope)
        with-chained-scope (fn [pattern]
                             (let [[new-pattern new-scope] (with-scope pattern scope)]
                               (compare-and-set! chained-scope @chained-scope new-scope)
                               new-pattern))]
    [(constructor (map with-chained-scope patterns)) @chained-scope]))

(defrecord Literal [value]
  AST
  (with-scope [this scope]
    this)
  Pattern
  (run* [this input bindings]
    (if (= input value)
      [nil bindings]
      nil)))

(defrecord Guard* [src]
  AST
  (with-scope [this scope]
    (->Guard (eval (util/src-with-scope src scope)))))

(defrecord Guard [fun]
  Pattern
  (run* [this input bindings]
    (if (fun input bindings)
      [nil bindings]
      nil)))

(defrecord Bind* [symbol]
  AST
  (with-scope [this scope]
    (if (contains? scope symbol)
      ;; if already bound, test for equality
      (with-scope (->Guard `(= ~symbol ~util/input-sym)) scope)
      ;; otherwise bind symbol
      (->Bind symbol))))

(defrecord Bind [symbol]
  Pattern
  (run* [this input bindings]
    [nil (assoc bindings symbol input)]))

(defrecord Head [pattern]
  AST
  (with-scope [this scope]
    (pass-scope ->Head pattern scope))
  Pattern
  (run* [this input bindings]
    (when-let [[head & tail] input]
      (when-let [[remaining _ :as result] (run* pattern input bindings)]
        (when (nil? remaining)
          remaining)))))

(defrecord Map [keys&patterns]
  AST
  (with-scope [this scope]
    (chain-scope
     #(->Map (zip-map (map first keys&patterns) %))
     (map second keys&patterns)
     scope))
  Pattern
  (run* [this input bindings]
    (when (associative? input)
      (loop [keys&patterns keys&patterns
             bindings bindings]
        (if-let [[[key pattern] & keys&patterns] keys&patterns]
          (let [value (get input key ::not-found)]
            (when (not (= ::not-found value))
              (when-let [[remaining & new-bindings] (run* pattern value bindings)]
                (when (nil? remaining)
                  (recur keys&patterns new-bindings)))))
          [nil bindings])))))

(defrecord Record [class-name patterns]
  AST
  (with-scope [this scope]
    (chain-scope #(->Constructor constructor) patterns scope))
  Pattern
  (run* [this input bindings]
    (when (instance? class-name input)
      (loop [patterns patterns
             args (vals input)
             bindings bindings]
        (if-let [[pattern & patterns] patterns]
          (when-let [[arg & args] args]
            (when-let [[remaining & new-bindings] (run* pattern input-head bindings)]
              (when (nil? remaining)
                (recur patterns args new-bindings))))
          [nil bindings])))))

(defrecord Total [pattern]
  AST
  (with-scope [this scope]
    (pass-scope ->Total pattern scope))
  Pattern
  (run* [this input bindings]
    (when-let [[remaining _ :as result] (run* pattern scope bindings)]
      (when (nil? remaining)
        result))))

(defrecord Not [pattern]
  AST
  (with-scope [this scope]
    (pass-scope ->Not pattern scope))
  Pattern
  (run* [this input bindings]
    (when-let [result (run* pattern scope bindings)]
      nil
      [nil bindings])))

(defrecord Or [patterns]
  AST
  (with-scope [this scope]
    (let [new-patterns&new-scopes (map #(with-scope % scope) patterns)
          new-patterns (map first new-patterns&new-scopes)
          new-scopes (map second new-patterns&new-scopes)
          new-scope (first new-scopes)]
      (assert (every? #(= new-scope %) new-scopes) "All sub-patterns of an 'or' pattern must have the same bindings")
      [(->Or new-patterns) new-scope]))
  Pattern
  (run* [this input bindings]
    (loop [patterns patterns]
      (when-let [[pattern & patterns-rest] patterns]
        (if-let [result (run* pattern input bindings)]
          result
          (recur patterns-rest))))))

(defrecord And [patterns]
  AST
  (with-scope [this scope]
    (chain-scope ->And patterns scope))
  Pattern
  (run* [this input bindings]
    (let [[pattern & patterns-rest] patterns]
      (loop [pattern pattern
             patterns patterns
             bindings bindings]
        (when-let [[remaining new-bindings :as result] (run* pattern input bindings)]
          (if-let [[pattern & patterns] patterns]
            (recur pattern patterns new-bindings)
            result))))))

(defrecord Chain [patterns]
  AST
  (with-scope [this scope]
    (chain-scope ->Or patterns scope))
  Pattern
  (run* [this input bindings]
    (let [[pattern & patterns-rest] patterns]
      (loop [pattern pattern
             patterns patterns
             input input
             bindings bindings]
        (when-let [[remaining new-bindings :as result] (run* pattern input bindings)]
          (if-let [[pattern & patterns] patterns]
            (recur pattern patterns remaining new-bindings)
            result))))))

(defrecord Seq [pattern]
  AST
  (with-scope [this scope]
    (pass-scope ->Seq pattern scope))
  Pattern
  (run* [this input bindings]
    (when (or
           (nil? input)
           (instance? clojure.lang.Seqable input))
      (run* pattern (seq input) bindings))))

(defn or [& patterns] (->Or patterns))
(defn and [& patterns] (->And patterns))
(defn chain [& patterns] (->Chain patterns))
(defn prefix [& patterns] (->Seq (chain-pattern patterns)))
(defn seqable [& patterns] (->Total (prefix-pattern patterns)))
