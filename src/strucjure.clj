(ns strucjure)

;; --- TODO ---
;; need to figure out how to refer graphs in sugar -- maybe view/trace should not use sugar at all
;; tests (string in regression, readme, bootstrap, generative)
;; error reporting - deepest-error in graph, maybe first-error in pattern
;; README - http://hugoduncan.org/post/evaluate_clojure_in_emacs_markdown_buffers/ or similar
;; use wolfes trick for closures. for lexically scoped parts, just add dependency in fnk and don't check it in Output

;; --- ERRORS ---
;; maybe need to rethink [o r] vs nil
;; need to return failure messages
;; can they be tracked imperatively?
;; later on, cut and commit will require modifying (when-let [[o r] ...] ...)
;; proposal
;;   use Success and Fail
;;   if-result, when-result, check
;;   rename state->scope, output?->control. track :output, :trace, :depth
;;   pass error? to decide whether or not to bother constructing a real Fail
;;     we always have to insert depth, at least
;;   allow graph to choose heuristic for tracking failures
;;  what if we pass in a mutable var to use for error tracking
;;    it needs depth and result
;;    could walk and wrap every Or? and every when? nope, gonna have to be hardcoded
;;  for now stick with the current scheme and focus on tracing
;; pattern trace could also benefit from static depth

;; --- OPTIMISATIONS ---
;; check if output unchanged in patterns - maybe rethink how output/bindings are passed
;; should Output check output?

;; --- LATER ---
;; clumsiness in trace-pattern comes from using CPS for ->And in the compiler
;;   could use CPS in graph compiler too - would help with trampolining too
;;   wait until we have benchmarks though
;; use tuple or deftype for result?
;; need to be able to alter views (store original pattern in meta and have pattern/alter and graph/alter)
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
;; write down thinking to avoid going in circles

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
