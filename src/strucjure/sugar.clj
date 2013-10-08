(ns strucjure.sugar
  (:refer-clojure :exclude [with-meta * or and])
  (:require [plumbing.core :refer [fnk for-map aconcat]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern :refer [->Rest ->Seqable ->Any ->Is ->Guard ->Name ->Or ->And ->Repeated ->WithMeta ->Node ->Edge ->Graph]]
            [strucjure.graph :as graph]
            ;;[strucjure.debug :as debug]
            [strucjure.view :as view]))

;; TODO wrapping parser/rest in [] and calling first (elem) is ugly

(defn zero-or-more [pattern] (->Repeated 0 Long/MAX_VALUE pattern))
(defn one-or-more [pattern] (->Repeated 1 Long/MAX_VALUE pattern))
(defn zero-or-one [pattern] (->Repeated 0 1 pattern))

(def sugared
  (letfn [(bindable [pattern] (->WithMeta pattern (->Or [{:tag (->Edge 'binding)} (->Any)])))]
    (graph/with-named-nodes
      (graph/with-named-edges
        {'pattern (bindable (->Or [(->Edge 'unquote) (->Edge 'seq) (->Edge 'vec) (->Edge 'map) (->Edge 'any) (->Edge 'default)]))
         'unquote (list `unquote (->Name 'unquoted (->Any)))
         'seq (list (->Rest (->Edge 'elems)))
         'vec (vector (->Rest (->Edge 'elems)))
         'elems (zero-or-more (->Rest (->Edge 'elem)))
         'elem (->Or [(->Edge 'parser) (->Edge 'rest) (list (->Edge 'pattern))])
         'parser (list (bindable (->Name 'prefix (->Or ['* '+ '?]))) (->Rest (->Edge 'elem)))
         'rest (list (bindable (->Name 'prefix '&)) (->Rest (->Edge 'elem)))
         'map (->And [{} (->Name 'elems (->Seqable [(->Rest (zero-or-more [(->Any) (->Edge 'pattern)]))]))]) ;; TODO make s/hashmap for this pattern
         'binding (->Is #(symbol? %))
         'any '_
         'default (->Any)}))))

(def prefixes
  {'* `zero-or-more
   '+ `one-or-more
   '? `zero-or-one})

(def desugar-pattern
  (view/view-with [(view/with-output-at strucjure.pattern.Node
                     {'pattern (fnk [binding pattern] (if binding `(->Name '~binding ~pattern) pattern))
                      'unquote (fnk [unquoted] unquoted)
                      'seq (fnk [seq] `(list ~@seq))
                      'parser (fnk [binding prefix elem]
                                   [(let [parser `(~(prefixes prefix) ~(first elem))]
                                      (if binding `(->Name '~binding ~parser) parser))])
                      'rest (fnk [binding elem]
                                 [`(->Rest ~(if binding `(->Name '~binding ~(first elem)) (first elem)))])
                      'map (fnk [map] (into {} map))
                      'any (fnk [] `(strucjure.pattern/->Any))
                      'default (fnk [default] `'~default)})]
                  (->Graph 'pattern sugared)))

(defmacro pattern [sugar]
  (desugar-pattern sugar))

(defmacro with-nodes [names & body]
  `(let [~@(interleave names (for [name names] `(->Edge '~name)))]
     (do ~@body)))

(defn desugar-graph [names&sugars]
  `(graph/with-named-nodes
     (graph/with-named-edges
       (with-nodes [~@(take-nth 2 names&sugars)]
         ~(for-map [[name sugar] (partition 2 names&sugars)] `'~name `(pattern ~sugar))))))

(defmacro graph [& names&sugars]
  (desugar-graph names&sugars))

(defmacro seqable [& sugars]
  `(->Seqable (pattern ~sugars)))

(defmacro is [f]
  `(->Is ~f))

(defmacro guard [sugar fnk]
  `(->Guard (pattern ~sugar) ~fnk))

(defmacro output [sugar fnk]
  `(->Output (pattern ~sugar) ~fnk))

(defmacro as [& sugars]
  `(->As [~@(for [sugar sugars] `(pattern ~sugar))]))

(defmacro or [& sugars]
  `(->Or [~@(for [sugar sugars] `(pattern ~sugar))]))

(defmacro and [& sugars]
  `(->And [~@(for [sugar sugars] `(pattern ~sugar))]))

(defmacro * [sugar]
  `(->Repeated 0  (pattern ~sugar)))

(defmacro + [sugar]
  `(->Repeated 1 nil (pattern ~sugar)))

(defmacro ? [sugar]
  `(->Repeated nil 1 (pattern ~sugar)))

(defmacro with-meta [sugar meta-sugar]
  `(->WithMeta (pattern ~sugar) (pattern ~meta-sugar)))

(defmacro node [name]
  `(->Edge ~name))

;; TODO change name
(defmacro node-of [graph name]
  `(->Graph ~graph ~name))

(defmacro view [sugar]
  (view/view-with [] (eval (desugar-pattern sugar))))

;; (defmacro trace [sugar]
;;   (view/pattern->view (debug/pattern-with-trace (eval (desugar-pattern sugar))) true true))

(defmacro match [input & sugars&outputs]
  (let [pattern (->Or (vec (for [[sugar output] (partition 2 sugars&outputs)]
                             (let [pattern (eval (desugar-pattern sugar))
                                   [_ bound] (pattern/with-bound pattern)]
                               (->Output pattern (eval `(fnk [~@bound] ~output)))))))]
    `(let [[output# remaining#] (~(view/pattern->view pattern true false) ~input)]
       output#)))
