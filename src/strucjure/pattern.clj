(ns strucjure.pattern
  (:require [strucjure.common :as common :refer [->Or ->And get-stubs set-stubs]]
            [clojure.set :refer [union]]))

;; --- TODO ---
;; split out common
;; split out parsers
;; move to strucjure.pattern
;; protocols, more datatypes
;; ~fn ~var ~(refer (pattern x y z))

;; --- SCRATCH ---
;; symbol, !var, ?nullable
;; ~pattern
;; ~(s/or x y) ~(s/is x) ~(s/when (= x 1)) ~(s/+ x)
;; NOT p/or, p/and, v/or, v/and
;; ~fn ~var -- call compiled?
;; ~(recur x y)?
;; don't need context-sensitive if we can build our own views using s/fail s/succeed
;; ~(view pattern output) ~(call view pattern-for-output) pattern->view, view->pattern
;; use match as compiler
;; later - run over generated code and remove (if _ true false) etc
;; tree is easy, use chunk for dag, use fns for cycles

(defprotocol Pattern
  (pattern->tree [this input bound]))

(defrecord Succeed [remaining bound])
(defrecord Fail [])

;; TODO use stubs and efficient local vars instead of atoms

(defrecord Bind [symbol])

(defn get-binding [symbol]
  `(deref ~symbol))

(defn set-binding [symbol value]
  `(reset! ~symbol ~value))

(defn with-bindings [symbols form]
  `(let [~@(apply concat (for [symbol symbols] [symbol `(atom nil)]))]
     ~form))

(defn bush->bound [bush]
  "The set of symbols that are bound if the bush succeeds"
  (let [stubs (get-stubs bush #{Succeed Fail})
        succeeds (stubs Succeed)]
    (assert (= 1 (count (set (map :bound succeeds)))) "All success paths must bind the same set of symbols")
    (:bound (first succeeds))))

(defn bush->tree [bush]
  "Reduce the number of success and failure paths to at most one each to avoid exponential branching"
  (let [stubs (get-stubs bush #{Succeed Fail})
        succeeds (stubs Succeed)
        fails (stubs Fail)]
    (if (and (<= (count succeeds) 1) (<= (count fails) 1))
      bush ;; already a tree
      `(if ~(set-stubs bush
                              {Succeed (fn [_] true)
                               Fail (fn [_] false)})
         ~(->Succeed ~(:remaining (first succeeds)) (bush->bound bush))
         ~(->Fail)))))

(defn and->bush [sub-patterns input bound]
  (let [[sub-pattern & sub-patterns] sub-patterns]
    (if sub-patterns
      (set-stubs (pattern->tree sub-pattern input bound)
                        {Succeed (fn [{:keys [bound]}] (and->bush sub-patterns input bound))
                         Fail identity})
      (pattern->tree sub-pattern input bound))))

(defn or->bush [sub-patterns input bound]
  (let [[sub-pattern & sub-patterns] sub-patterns]
    (if sub-patterns
      (set-stubs (pattern->tree sub-pattern input bound)
                        {Succeed identity
                         Fail (fn [_] (or->bush sub-patterns input bound))})
      (pattern->tree sub-pattern input bound))))

;; TODO seq patterns are now broken, need a separate protocol to handle remaining
(defrecord Rest [])
(defn seq->bush [sub-patterns input bound]
  (if-let [[sub-pattern & sub-patterns] sub-patterns]
    (if (instance? Rest sub-pattern)
      (set-stubs (pattern->tree (:pattern sub-pattern) input bound)
                        {Succeed (fn [{:keys [remaining bound]}] (seq->bush sub-patterns remaining bound))
                         Fail identity})
      (let [first-sym (gensym "first")
            rest-sym (gensym "rest")]
        `(if-let [[~first-sym ~rest-sym] ~input]
           ~(set-stubs (pattern->tree sub-pattern first-sym bound)
                              {Succeed (fn [{:keys [remaining bound]}] (seq->bush sub-patterns rest-sym bound))
                               Fail identity})
           ~(->Succeed input bound))))))

(extend-protocol Pattern
  Object
  (pattern->tree [this input bound]
    `(if (= ~input ~this)
       ~(->Succeed nil bound)
       ~(->Fail)))
  Bind
  (pattern->tree [this input bound]
    (let [symbol (:symbol this)]
      (if (bound symbol)
        `(if (= ~(get-binding symbol) ~input)
           ~(->Succeed nil bound)
           ~(->Fail))
        `(do ~(set-binding symbol input)
             ~(->Succeed nil (conj bound symbol))))))
  strucjure.common.And
  (pattern->tree [this input bound]
    (bush->tree (and->bush (:patterns this) input bound)))
  strucjure.common.Or
  (pattern->tree [this input bound]
    (bush->tree (or->bush (:patterns this) input bound)))
  clojure.lang.ISeq
  (pattern->tree [this input bound]
    (bush->tree (seq->bush this input bound))))

(defn pattern->tree-with-locals [pattern input bound]
  (let [tree (pattern->tree pattern input bound)]
    (with-bindings (bush->bound tree) tree)))

(defn pattern->matches? [pattern]
  (let [input-sym (gensym "input")]
    `(fn [~input-sym]
       ~(set-stubs (pattern->tree-with-locals pattern input-sym #{})
                          {Succeed (fn [{:keys [remaining bound]}]
                                     [remaining (into {} (for [symbol bound] [`(quote ~symbol) `(deref ~symbol)]))])
                           Fail (fn [_]
                                  nil)}))))

;; (pattern->tree-with-locals (->Bind 'a) 'input #{})
;; (pattern->tree-with-locals (->Bind 'a) 'input #{'a})
;; (and->bush [(->Bind 'a) (->Bind 'b)] 'input #{})
;; (meta (->And [(->Bind 'a) (->Bind 'b)]))
;; (command? 'and (->And [(->Bind 'a) (->Bind 'b)]))
;; (pattern->tree (->And [(->Bind 'a) 1]) 'input #{})
;; (pattern->tree (->And [(->Bind 'a) 1 2]) 'input #{})
;; (pattern->tree (->And [(->Bind 'a) (->Bind 'b)]) 'input #{})
;; (pattern->tree-with-locals (->And [(->Bind 'a) (->Bind 'b)]) 'input #{})
;; (pattern->matches? (->And [(->Bind 'a) (->Bind 'b)]))
;; ((eval (pattern->matches? (->And [(->Bind 'a) (->Bind 'b)]))) 1)
;; ((eval (pattern->matches? (->And [1 (->Bind 'b)]))) 1)
;; ((eval (pattern->matches? (->And [1 (->Bind 'b)]))) 2)
