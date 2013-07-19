(ns strucjure
  (:use clojure.test)
  (:require [clojure.set :refer [union]]))

;; PEG parser / pattern matcher
;; (originally based on matchure)

;; --- THIS ---
;; pass output like remaining
;; figure out where to put nil checks for remaining
;; compiler
;; sugar (raw/sour)
;; splicing
;; clean up syntax and semantics, especially import (use ~?)
;; proper locals (with-local-vars?)
;; sanity check input

;; --- NEXT ---
;; use graph for large parsers
;; useful error messages
;; interactive debugger

;; --- LATER ---
;; extended syntax
;; type hinting
;; string parsing

;; --- TODO ---
;; use graph for extensibility
;; use deepest match for error reporting
;; symbol, !var, ?nullable
;; write cond handlers out as fns so can attach to actual grammar later?
;; need to be able to splice in parsers? or just quote? this is the reason data-structures are better. using :or would be a good start
;; have pattern* do the work and pattern just syntax-quotes? prewalk resolve and splice. there is a syntax-quote library somewhere...
;; splicing allows syntactic extension - can use splice for import if parser knows how to call fns? not context-aware and need to think about output
;;   only bind output?
;; can almost use splicing for guards but not context aware. can at least splice in a fun...
;; biggest change is representation as plain data-structure. important to note...
;; graph compiler can trampoline, inline etc
;; strucjure cannot be compiled context-free... may have to do more interesting traversal - reduce?
;; later - run over generated code and remove (if _ true false) etc
;; remaining/output form a stack machine. true-case/false-case implement that machine on the actual lexical stack
;; ->Command
;; bool->clj is like save-stack, restore-stack
;; tree is easy, use chunk for dag, use funs for cycles
;; pattern->tree can be a multi-method on type
;; output =>
;; input <=
;; remaining hack is really ugly. put in bindings?
;; in the case (?a ?b) + how do we disamiguate?
;;   possibly (+ ?a ?b) vs (+ [?a ?b]) vs (+ & [?a ?b])

(defn command? [name pattern]
  (and (= ::command (type pattern)) (= name (first pattern))))

(defrecord Succeed [remaining bound])
(defrecord Fail [])

;;; stolen from potemkin
(defn- walk
  "Like `clojure.walk/walk`, but preserves metadata."
  [inner outer form]
  (let [x (cond
            (instance? clojure.lang.IRecord form) (outer (inner form))
            (list? form) (outer (apply list (map inner form)))
            (instance? clojure.lang.IMapEntry form) (outer (vec (map inner form)))
            (seq? form) (outer (doall (map inner form)))
            (coll? form) (outer (into (empty form) (map inner form)))
            :else (outer form))]
    (if (instance? clojure.lang.IObj x)
      (with-meta x (meta form))
      x)))

(defn set-placeholders [form succeed-fn fail-fn]
  (cond
   (instance? Succeed form) (succeed-fn (:remaining form) (:bound form))
   (instance? Fail form) (fail-fn)
   :else (walk #(set-placeholders % succeed-fn fail-fn) identity form)))

(defn get-placeholders [form]
  (let [succeeds (atom [])
        fails (atom [])]
    (set-placeholders form #(swap! succeeds conj (->Succeed %1 %2)) #(swap! fails conj (->Fail)))
    [@succeeds @fails]))

(defn bush->bound [bush]
  "The set of symbols that are bound if the bush succeeds"
  (let [[succeeds fails] (get-placeholders bush) ]
    (assert (= 1 (count (set (map :bound succeeds)))) "All success paths must bind the same set of symbols")
    (:bound (first succeeds))))

(defonce remaining-sym
  (gensym "remaining"))

(defn bush->tree [bush]
  "Reduce the number of success and failure paths to at most one each to avoid exponential branching"
  (let [[succeeds fails] (get-placeholders bush)]
    (cond
     ;; already a tree
     (and (<= (count succeeds) 1) (<= (count fails) 1))
     bush

     ;; every success path returns the same
     (<= (count (set succeeds)) 1)
     `(if ~(set-placeholders bush (fn [_ _] true) (fn [] false))
        ~(->Succeed ~(:remaining (first succeeds)) (bush->bound bush))
        ~(->Fail))

     ;; different returns from different success path
     :else
     `(if ~(set-placeholders bush (fn [remaining _] `(do (reset! ~remaining-sym ~remaining) true)) (fn [] false))
        ~(->Succeed `(deref ~remaining-sym) (bush->bound bush))
        ~(->Fail)))))

(defn and->bush [sub-patterns input bound]
  (let [[sub-pattern & sub-patterns] sub-patterns]
    (if sub-patterns
      (set-placeholders (pattern->tree sub-pattern input bound)
                        (fn [_ bound] (and->bush sub-patterns input bound))
                        ->Fail)
      (pattern->tree sub-pattern input bound))))

(defn or->bush [sub-patterns input bound]
  (let [[sub-pattern & sub-patterns] sub-patterns]
    (if sub-patterns
      (set-placeholders (pattern->tree sub-pattern input bound)
                        ->Succeed
                        (fn [] (or->bush sub-patterns input bound)))
      (pattern->tree sub-pattern input bound))))

(defn seq->bush [sub-patterns input bound]
  (if-let [[sub-pattern & sub-patterns] sub-patterns]
    (if (command? '& sub-pattern)
      (set-placeholders (pattern->tree (second sub-pattern) input bound)
                        (fn [remaining bound] (seq->bush sub-patterns remaining bound)
                        ->Fail))
      (let [first-sym (gensym "first")
            rest-sym (gensym "rest")]
        `(if-let [[~first-sym ~rest-sym] ~input]
           ~(set-placeholders (pattern->tree sub-pattern first-sym bound)
                              (fn [remaining bound] (seq->bush sub-patterns rest-sym bound))
                              ->Fail)
           ~(->Succeed input bound))))))

(defn pattern->tree [pattern input bound]
  "Compile a pattern into a decision-tree"
  (binding [*print-meta* true] (println pattern input bound))
  (cond
   (= ::binding (type pattern))
   (if (bound pattern)
     `(if (= (deref ~pattern) ~input)
        ~(->Succeed nil bound)
        ~(->Fail))
     `(do (reset! ~pattern ~input)
          ~(->Succeed nil (conj bound pattern))))

   (command? 'or pattern)
   (bush->tree (or->bush (rest pattern) input bound))

   (command? 'and pattern)
   (bush->tree (and->bush (rest pattern) input bound))

   (seq? pattern)
   (bush->tree (seq->bush (rest pattern) input bound))

   :else
   `(if (= ~input ~pattern)
      ~(->Succeed nil bound)
      ~(->Fail))))

(defn pattern->wrapped-tree [pattern input bound]
  (let [tree (pattern->tree pattern input bound)]
    `(let [~remaining-sym (atom nil)
           ~@(apply concat (for [symbol (bush->bound tree)] [symbol `(atom nil)]))]
       ~tree)))

(defn pattern->matches? [pattern]
  (let [input-sym (gensym "input")]
    `(fn [~input-sym]
       ~(set-placeholders (pattern->wrapped-tree pattern input-sym #{})
                          (fn [remaining bound]
                            [remaining (into {} (for [symbol bound] [`(quote ~symbol) `(deref ~symbol)]))])
                          (fn []
                            nil)))))

(defn with-type [type obj]
  (with-meta obj (merge (meta obj) {:type type})))

(defn- ? [sym]
  (with-type ::binding sym))

(defn- $ [& args]
  (with-type ::command (apply list args)))

;; (pattern->wrapped-tree (? 'a) 'input #{})
;; (pattern->wrapped-tree (? 'a) 'input #{'a})
;; (and->bush [(? 'a) (? 'b)] 'input #{})
;; (meta ($ 'and (? 'a) (? 'b)))
;; (command? 'and ($ 'and (? 'a) (? 'b)))
;; (pattern->tree ($ 'and (? 'a) 1) 'input #{})
;; (pattern->tree ($ 'and (? 'a) 1 2) 'input #{})
;; (pattern->tree ($ 'and (? 'a) (? 'b)) 'input #{})
;; (pattern->wrapped-tree ($ 'and (? 'a) (? 'b)) 'input #{})
;; (pattern->matches? ($ 'and (? 'a) (? 'b)))
;; ((eval (pattern->matches? ($ 'and (? 'a) (? 'b)))) 1)
;; ((eval (pattern->matches? ($ 'and 1 (? 'b)))) 1)
;; ((eval (pattern->matches? ($ 'and 1 (? 'b)))) 2)
