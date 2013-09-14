(ns strucjure.sugar
  (:refer-clojure :exclude [with-meta * or and])
  (:require [plumbing.core :refer [fnk for-map aconcat]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern :refer [->Rest ->Seqable ->Any ->Is ->Guard ->Name ->Output ->As ->Or ->And ->ZeroOrMore ->WithMeta ->Node ->NodeOf]]
            [strucjure.graph :as graph]
            [strucjure.debug :as debug]
            [strucjure.view :as view]))

;; TODO wrapping parser/rest in [] and calling first (elem) is ugly

(def sugared
  (letfn [(bindable [pattern] (->WithMeta pattern (->Or [{:tag (->Node 'binding)} (->Any)])))]
    {'pattern (bindable (->Or [(->Node 'unquote) (->Node 'seq) (->Node 'vec) (->Node 'map) (->Node 'any) (->Node 'default)]))
     'unquote (list `unquote (->Name 'unquoted (->Any)))
     'seq (list (->Rest (->Node 'elems)))
     'vec (vector (->Rest (->Node 'elems)))
     'elems (->ZeroOrMore (->Rest (->Node 'elem)))
     'elem (->Or [(->Node 'parser) (->Node 'rest) (list (->Node 'pattern))])
     'parser (list (bindable (->Name 'prefix (->Or ['*]))) (->Rest (->Node 'elem))) ;; TODO + ?
     'rest (list (bindable (->Name 'prefix '&)) (->Rest (->Node 'elem)))
     'map (->And [{} (->Name 'elems (->Seqable [(->Rest (->ZeroOrMore [(->Any) (->Node 'pattern)]))]))]) ;; TODO make s/hashmap for this pattern
     'binding (->Is #(symbol? %))
     'any '_
     'default (->Any)}))

(def prefixes
  {'* 'strucjure.pattern/->ZeroOrMore})

(def desugared
  (graph/output-in (graph/with-named-inner-nodes (graph/with-named-outer-nodes sugared))
                   'pattern (fnk [binding pattern] (if binding `(->Name '~binding ~pattern) pattern))
                   'unquote (fnk [unquoted] unquoted)
                   'seq (fnk [seq] `(list ~@seq))
                   'parser (fnk [binding prefix elem]
                                [(let [parser `(~(prefixes prefix) ~(first elem))]
                                   (if binding `(->Name '~binding ~parser) parser))])
                   'rest (fnk [binding elem]
                              [`(->Rest ~(if binding `(->Name '~binding ~(first elem)) (first elem)))])
                   'map (fnk [map] (into {} map))
                   'any (fnk [] `(strucjure.pattern/->Any))
                   'default (fnk [default] `'~default)))

;; TODO error reporting here
;; TODO dont eval each time
(defn desugar-pattern [sugar]
  (let [desugar-view (eval (view/graph->view 'pattern desugared))]
    (let [[output remaining] (desugar-view sugar)]
      (if (nil? remaining)
        output
        (throw (Exception. "Not a pattern"))))))

(defmacro pattern [sugar]
  (desugar-pattern sugar))

(defmacro with-nodes [names & body]
  `(let [~@(interleave names (for [name names] `(->Node '~name)))]
     (do ~@body)))

(defn desugar-graph [names&sugars]
  `(graph/with-named-inner-nodes
     (graph/with-named-outer-nodes
       `(with-nodes [~@(take-nth 2 names&sugars)]
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
  `(->ZeroOrMore (pattern ~sugar)))

(defmacro with-meta [sugar meta-sugar]
  `(->WithMeta (pattern ~sugar) (pattern ~meta-sugar)))

(defmacro node [name]
  `(->Node ~name))

(defmacro node-of [graph name]
  `(->NodeOf ~graph ~name))

(defmacro view [sugar]
  (view/pattern->view (eval (desugar-pattern sugar)) true true))

(defmacro trace [sugar]
  (view/pattern->view (debug/pattern-with-trace (eval (desugar-pattern sugar))) true true))

(defmacro match [input & sugars&outputs]
  (let [pattern (->Or (vec (for [[sugar output] (partition 2 sugars&outputs)]
                              (let [pattern (eval (desugar-pattern sugar))
                                    [_ bound] (pattern/with-bound pattern)]
                                (->Output pattern (eval `(fnk [~@bound] ~output)))))))]
    `(let [[output# remaining#] (~(view/pattern->view pattern true false) ~input)]
       output#)))
