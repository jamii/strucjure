(ns strucjure.pattern
  (:require [strucjure.util :as util]))

(defprotocol AST
  "An Abstract Syntax Tree for a pattern"
  (with-scope [this scope]
    "Compile the pattern ast into a pattern. Return [pattern-code new-scope]."))

 (defprotocol Pattern
  "A pattern takes an input and either fails or consumes some/all of the input and returns a set of bindings"
  (run* [this input bindings]
    "Run the pattern with the given input and bindings. Return [remaining-input new-bindings] on success or nil on failure."))

(defn run [pattern input]
  (run* pattern input {}))

(defn pass-scope [constructor pattern scope]
  (let [[new-pattern new-scope] (with-scope pattern scope)]
   [(constructor new-pattern) new-scope]))

(defn chain-scope [constructor patterns scope]
  (let [chained-scope (atom scope)
        with-chained-scope (fn [pattern]
                             (let [[new-pattern new-scope] (with-scope pattern @chained-scope)]
                               (compare-and-set! chained-scope @chained-scope new-scope)
                               new-pattern))
        new-pattern (constructor (vec (map with-chained-scope patterns)))]
    [new-pattern @chained-scope]))

(defrecord Literal [value]
  AST
  (with-scope [this scope]
    [`(->Literal ~value) scope])
  Pattern
  (run* [this input bindings]
    (if (= input value)
      [nil bindings]
      nil)))

(defrecord Ignore []
  AST
  (with-scope [this scope]
    [`(->Ignore) scope])
  Pattern
  (run* [this input bindings]
    [nil bindings]))

(defrecord Guard [fun]
  AST
  (with-scope [this scope]
    [`(->Guard ~(util/src-with-scope fun scope)) scope])
  Pattern
  (run* [this input bindings]
    (if (fun input bindings)
      [nil bindings]
      nil)))

(defrecord Bind [symbol]
  AST
  (with-scope [this scope]
    (if (contains? scope symbol)
      ;; if already bound, test for equality
      (with-scope (->Guard `(= ~symbol ~util/input-sym)) scope)
      ;; otherwise bind symbol
      [`(->Bind '~symbol) (conj scope symbol)]))
  Pattern
  (run* [this input bindings]
    [nil (assoc bindings symbol input)]))

(defrecord Head [pattern]
  AST
  (with-scope [this scope]
    (pass-scope (fn [pattern] `(->Head ~pattern)) pattern scope))
  Pattern
  (run* [this input bindings]
    (when-let [[head & tail] input]
      (when-let [[remaining new-bindings :as result] (run* pattern head bindings)]
        (when (nil? remaining)
          [tail new-bindings])))))

(defrecord Map [keys&patterns]
  AST
  (with-scope [this scope]
    (chain-scope
     (fn [patterns] `(->Map (zip-map ~(map first keys&patterns) ~patterns)))
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
    (chain-scope (fn [patterns] `(->Record ~class-name ~patterns)) patterns scope))
  Pattern
  (run* [this input bindings]
    (when (instance? class-name input)
      (loop [patterns patterns
             args (vals input)
             bindings bindings]
        (if-let [[pattern & patterns] patterns]
          (when-let [[arg & args] args]
            (when-let [[remaining & new-bindings] (run* pattern arg bindings)]
              (when (nil? remaining)
                (recur patterns args new-bindings))))
          [nil bindings])))))

(defrecord Regex [regex]
  AST
  (with-scope [this scope]
    `(->Regex ~regex))
  Pattern
  (run* [this input bindings]
    (when-let [_ (re-find regex input)]
      [nil bindings])))

(defrecord Total [pattern]
  AST
  (with-scope [this scope]
    (pass-scope (fn [pattern] `(->Total ~pattern)) pattern scope))
  Pattern
  (run* [this input bindings]
    (when-let [[remaining _ :as result] (run* pattern input bindings)]
      (when (nil? remaining)
        result))))

(defrecord Not [pattern]
  AST
  (with-scope [this scope]
    (pass-scope (fn [pattern] `(->Not ~pattern)) pattern scope))
  Pattern
  (run* [this input bindings]
    (if-let [result (run* pattern input bindings)]
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
      [`(->Or ~(vec new-patterns)) new-scope]))
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
    (chain-scope (fn [patterns] `(->And ~patterns)) patterns scope))
  Pattern
  (run* [this input bindings]
    (if-let [[pattern & patterns-rest] (seq patterns)]
      (loop [pattern pattern
             patterns patterns
             bindings bindings]
        (when-let [[remaining new-bindings :as result] (run* pattern input bindings)]
          (if-let [[pattern & patterns] patterns]
            (recur pattern patterns new-bindings)
            result)))
      [input bindings])))

(defrecord Chain [patterns]
  AST
  (with-scope [this scope]
    (chain-scope (fn [patterns] `(->Chain ~patterns)) patterns scope))
  Pattern
  (run* [this input bindings]
    (if-let [[pattern & patterns] (seq patterns)]
      (loop [pattern pattern
             patterns patterns
             input input
             bindings bindings]
        (when-let [[remaining new-bindings :as result] (run* pattern input bindings)]
          (if-let [[pattern & patterns] patterns]
            (recur pattern patterns remaining new-bindings)
            result)))
      [input bindings])))

(defrecord Seq [pattern]
  AST
  (with-scope [this scope]
    (pass-scope (fn [pattern] `(->Seq ~pattern)) pattern scope))
  Pattern
  (run* [this input bindings]
    (when (or
           (nil? input)
           (instance? clojure.lang.Seqable input))
      (run* pattern (seq input) bindings))))

(defn prefix [& patterns] (->Seq (->Chain patterns)))
(defn seqable [& patterns] (->Total (apply prefix patterns)))
