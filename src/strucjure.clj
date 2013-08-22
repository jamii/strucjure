(ns strucjure)

;; --- TODO ---
;; need to be careful about reusing input - a (let-sym [input `(meta input)] ...) would be useful here
;;      let-syms -> with-syms, then use let-sym
;; go back to using letfn for graph, along with pre/post
;; error reporting - deepest-error in graph, maybe first-error in pattern
;; debugging - trace-pattern, trace-graph
;; let input in patterns
;; tests (regression, readme, bootstrap, generative)
;; README

;; --- ERRORS ---
;; maybe need to rethink [o r] vs nil
;; need to return failure messages
;; can they be tracked imperatively?
;; later on, cut and commit will require modifying (when-let [[o r] ...] ...)

;; --- OPTIMISTIONS ---
;; check if output unchanged in patterns - maybe rethink how output/bindings are passed

;; --- LATER ---
;; figure out how to roundtrip pre/post closures through eval
;; need to be able to alter views (store original pattern in meta and have pattern/alter and graph/alter)
;; need to be able to walk patterns for eg determining dependencies in graph so can ignore in lenses
;; cut by returning delay - can trampoline to the nearest try - needs work inside Or/ZeroOrMore
;; gens
;; type hinting
;; may need to rethink seq patterns and Rest
;; string patterns, ~(chain "foo" "/" "bar")
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
;; writing out expected code helps write compiler
;; write the api without macros first
;; would be hugely useful to be able to embed arbitrary data structures in code before eval
;; getting binding right is hard. parsec et al solve this by just not doing it

;; --- MOTIVATION ---
;; types vs data
;; use :refer-all as example
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
)

(comment
  (def ot (pattern (1 2)))
  [0 * ~ot] --> [0 ((1 2) (1 2))]
  [0 * & ~ot] --> [0 (1 2 1 2)]
  [0 & * ~ot] --> [0 (1 2) (1 2)]
  [0 & * & ~ot] --> [0 1 2 1 2])
