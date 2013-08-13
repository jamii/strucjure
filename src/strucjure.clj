(ns strucjure)

;; --- PROBLEMS ---
;; compiler is ugly
;; binding syntax, esp in graph

;; --- TODO ---
;; GetBinding still in output
;; more datatypes
;; ~fn ~var ~(invoke (pattern x y z)) ~(recur x y z)
;; use meta for bindings in sugar (requires (->Bind name rest))
;; have ->Node create &binding
;; sugar (raw/sour) (splicing)
;; =>, <=, &output, &remaining
;; tests
;; benchmark + optimise (proper locals?)

;; --- LATER ---
;; revisit bindings
;;   maybe use binding trick for let as well (LetBinding in bush->tree)
;;   maybe use state machine like core.async
;;   maybe use all mutables and use booleans for decisions
;;   certainly add an intermediate representation
;; graph (extensible, memo, trampoline, inline etc)
;; use fns in graph for now, later use (let [stack [[array state]] (loop [parent-array return-state output-index]))
;; cut
;;   pass erasable state down from try, return result=true/false/(cut result)?
;; useful error messages (deepest match?)
;; interactive debugger
;; reversible patterns
;; type hinting
;; string patterns, ~(chain "foo" "/" "bar"), may have to rethink seq patterns
;; binary patterns
;; reimplement core

;; --- LESSONS ---
;; huge premature optimisation of ->Or - measure twice, cut once
;; should look like constructors as much as possible
;; representation as plain data-structure, minimal syntax
;; compile as late as possible, use stubs for things that might not be needed
;; unquote as data macro cf template haskell and copilot
;; view/pattern interplay allows injecting other kinds of parsers
;; compiler simplified by mutable variables
;; writing use cases helps make decisions
;; writing out expected code help write compiler
;; write the api without macros first

;; --- SCRATCH ---
;; symbol, !var, ?nullable
;; ~pattern
;; ~(s/or x y) ~(s/is x) ~(s/when (= x 1)) ~(s/+ x)
;; ~fn ~var -- call compiled?
;; don't need context-sensitive if we can build our own views using s/fail s/succeed
;; ~(view pattern output) ~(call view pattern-for-output) pattern->view, view->pattern
;; use match as compiler
;; later - run over generated code and remove (if _ true false) etc
;; tree is easy, use chunk for dag, use fns for cycles
;;   in graph, anything with a backwards edge to it becomes a fn, everything else is a raw pattern
;; use meta for bindings?

(comment
  (def ot (pattern (1 2)))
  [0 * ~ot] --> [0 ((1 2) (1 2))]
  [0 * & ~ot] --> [0 (1 2 1 2)]
  [0 & * ~ot] --> [0 (1 2) (1 2)]
  [0 & * & ~ot] --> [0 1 2 1 2])

;; --- OPTIMISER ---
;; (if _ true false) (if _ false true) (when _ true) etc
;; (do nil & rest)

;; --- MOTIVATION ---
;; types vs data
(comment
  (:require [strucjure.raw :as r]
            [strucjure.sugar :as s])

  ;; sugar
  (def ns
    (s/graph
     ns (ns ~symbol & * ~clause)
     symbol ~(s/is symbol? %)
     clause ~(s/or ~refer ~use)
     use (:use + ~libspec)
     require (:require + ~libspec)
     libspec ~(s/or ~prefix + ~lib)
     prefix [~symbol + ~lib]
     lib ~(s/or ~symbol [~symbol & * ~option])
     option ~(s/or ~as ~refer)
     as (:as ~symbol)
     refer (:refer [& + ~symbol])))

  ;; raw
  (def ns
    {'ns (fnk [symbol clause] (list 'ns symbol (r/->& (r/->* clause))))
     ;; ...
     })

  (def pattern-pattern
    (s/graph
     pattern ~(s/or ~unquote ~seq ~vec ~binding ~symbol _)
     unquote (clojure.core/unquote _)
     seq (& * & elem)
     vec [& * & elem]
     elem ~(s/or (~pattern) (~unquote-splicing) ~parser)
     parser ~(s/or ~'* ~'+ ~'? ~'&)
     binding ~(s/or ~nullable-binding ~non-nullable-binding)
     nullable-binding ~(s/is nullable-binding? &input)
     non-nullable-binding ~(s/is non-nullable-binding? &input)
     symbol ~(s/is symbol? &input)))

  (def pattern-pattern
    (s/graph
     pattern ~(s/with-meta {:type ~binding} ~unbound-pattern)
     unbound-pattern ~(s/or ~unquote ~seq ~vec ~symbol _)
     unquote (clojure.core/unquote _)
     seq (& * & elem)
     vec [& * & elem]
     elem ~(s/or (~pattern) (~unquote-splicing) ~parser)
     parser ~(s/or ~'* ~'+ ~'? ~'&)
     binding ~(s/or ~nullable-binding ~non-nullable-binding)
     nullable-binding ~(s/is nullable-binding? &input)
     non-nullable-binding ~(s/is non-nullable-binding? &input)
     symbol ~(s/is symbol? &input)))

  (def desugar-pattern
    (s/update-graph pattern-pattern
                    unquote (s/=> ~% (_ ?body) ~body)
                    parser (s/=> ~% (?action ?sub-pattern) `(~(in-raw action) ~sub-pattern))
                    binding (s/=> ~% (->Bind (nullable? &output) (binding-name &output)))
                    symbol (s/=> ~% `'~&output)))

  ;; still need to think about bindings
)

;; construct , destruct , restruct? better than replode...
;; transform is destruct=>construct
;; lens -> struct
;; flow is not a lens, its explode/implode
