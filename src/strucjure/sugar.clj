(ns strucjure.sugar
  (:refer-clojure :exclude [with-meta * or and])
  (:require [plumbing.core :refer [fnk for-map aconcat]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern :refer [->Rest ->Seqable ->Any ->Is ->Guard ->Bind ->Output ->Or ->And ->ZeroOrMore ->WithMeta ->View]]
            [strucjure.debug :as debug]
            [strucjure.graph :as graph]))

;; TODO wrapping parser/rest in [] and calling first (elem) is ugly

(def sugared
  (letfn [(view [sym] (->Bind sym (->View sym)))
          (bindable [pattern] (->WithMeta pattern (->Or [{:tag (->Or [(view 'binding) (->Bind 'binding nil)])} (->Bind 'binding nil)])))]
    {'pattern (bindable (->Or [(view 'unquote) (view 'seq) (view 'vec) (view 'map) (view 'any) (view 'default)]))
     'unquote (list `unquote (->Bind 'unquoted (->Any)))
     'seq (list (->Rest (view 'elems)))
     'vec (vector (->Rest (view 'elems)))
     'elems (->ZeroOrMore (->Rest (view 'elem)))
     'elem (->Or [(view 'parser) (view 'rest) (list (view 'pattern))])
     'parser (list (bindable (->Bind 'prefix (->Or ['*]))) (->Rest (view 'elem))) ;; TODO + ?
     'rest (list (bindable (->Bind 'prefix '&)) (->Rest (view 'elem)))
     'map (->And [{} (->Bind 'elems (->Seqable [(->Rest (->ZeroOrMore [(->Any) (view 'pattern)]))]))]) ;; TODO make s/hashmap for this pattern
     'binding (->Is #(symbol? %))
     'any '_
     'default (->Any)}))

(def prefixes
  {'* 'strucjure.pattern/->ZeroOrMore})

(def desugared
  (graph/output-in (graph/with-named-nodes sugared)
                   'pattern (fnk [binding pattern] (if binding `(->Bind '~binding ~pattern) pattern))
                   'unquote (fnk [unquoted] unquoted)
                   'seq (fnk [seq] `(list ~@seq))
                   'parser (fnk [binding prefix elem]
                                [(let [parser `(~(prefixes prefix) ~(first elem))]
                                   (if binding `(->Bind '~binding ~parser) parser))])
                   'rest (fnk [binding elem]
                              [`(->Rest ~(if binding `(->Bind '~binding ~(first elem)) (first elem)))])
                   'map (fnk [map] (into {} map))
                   'any (fnk [] `(strucjure.pattern/->Any))
                   'default (fnk [default] `'~default)))

;; TODO error reporting here
;; TODO dont eval each time
(defn desugar-pattern [sugar]
  (let [desugar-view (eval (graph/graph->view 'pattern desugared))]
    (if-let [[output remaining] (desugar-view sugar)]
      (if (nil? remaining)
        output
        (throw (Exception. "Not a pattern")))
      (throw (Exception. "Not a pattern")))))

(defmacro pattern [sugar]
  (desugar-pattern sugar))

(defn desugar-graph [name->sugar]
  `(let [~@(aconcat (for [name (keys name->sugar)] [name `(->Bind '~name (->View '~name))]))]
     (strucjure.graph/with-named-nodes
       ~(for-map [[name sugar] name->sugar] `'~name `(pattern ~sugar)))))

(defmacro graph [& names&sugars]
  (desugar-graph (for-map [[name sugar] (partition 2 names&sugars)] name sugar)))

(defmacro seqable [& sugars]
  `(->Seqable (pattern ~sugars)))

(defmacro is [& f]
  `(->Is #(~f)))

(defmacro guard [sugar fnk]
  `(->Guard (pattern ~sugar) ~fnk))

(defmacro output [sugar fnk]
  `(->Output (pattern ~sugar) ~fnk))

(defmacro or [& sugars]
  `(->Or [~@(for [sugar sugars] `(pattern ~sugar))]))

(defmacro and [& sugars]
  `(->And [~@(for [sugar sugars] `(pattern ~sugar))]))

(defmacro & [sugar]
  `(->Rest (pattern ~sugar)))

(defmacro * [sugar]
  `(->ZeroOrMore (pattern ~sugar)))

(defmacro with-meta [sugar meta-sugar]
  `(->WithMeta (pattern ~sugar) (pattern ~meta-sugar)))

(defmacro view
  ([sugar]
     (pattern/pattern->view (eval (desugar-pattern sugar))))
  ([name sugar]
     (graph/graph->view (eval (desugar-graph sugar))))) ;; TODO do we want to allow graph splicing?

(defmacro trace
  ([sugar]
     (pattern/pattern->view (debug/pattern-with-print-trace (eval (desugar-pattern sugar)))))
  ([name sugar]
     (graph/graph->view (debug/graph-with-print-trace (eval (desugar-graph sugar))))))
