(ns strucjure)

;; --- STACK ---
;; could we remove core and be even more compact?
;; define subpatterns / with-subpatterns for all patterns
;; define named, output?, remaining?, used with defaults that use fmap
;; use existing cores as output fns

;; --- TODO ---
;; need to figure out how to refer graphs in sugar -- maybe view/trace should not use sugar at all
;; tests (string in regression, readme, bootstrap, generative)
;; README - http://hugoduncan.org/post/evaluate_clojure_in_emacs_markdown_buffers/ or similar
;; use wolfes trick for closures. for lexically scoped parts, just add dependency in fnk and don't check it in Output

;; --- ERRORS ---
;; use (on-pass output remaining scope) (on-fail input reason state)
;; (Pass. output remaining) (Fail. input reason)
;; first error? deepest error?
;;   if all same depth as Or, report the Or

;; --- OPTIMISATIONS ---
;; check if output unchanged in patterns - maybe rethink how output/bindings are passed
;; should Output check output?

;; --- LATER ---
;; extensible, multi-pass compiler - stop trying to fit everything into pattern->clj
;; consider separating output from patterns - makes closures much easier (what about guard? and is?)
;; error reporting - deepest-error in graph, maybe first-error in pattern
;; for prewalks can just build ast by attaching name and bindings in metadata
;; clumsiness in trace-pattern comes from using CPS for ->And in the compiler
;;   could use CPS in graph compiler too - would help with trampolining too
;;   wait until we have benchmarks though
;; use tuple or deftype for result?
;; need to be able to alter views (store original pattern in meta and have pattern/alter and graph/alter)
;; cut by returning delay - can trampoline to the nearest try - needs work inside Or/ZeroOrMore
;; gens
;; type hinting
;; string patterns, ~(chain "foo" "/" "bar")
;; binary patterns
;; reimplement core

;; --- LESSONS ---
;; really need proper mutable vars for compiler target
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
