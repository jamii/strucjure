(ns strucjure)

;; --- TODO ---
;; move view into pattern
;; need to be able to walk patterns for analysis and for sugar
;; need to be able to alter views (store original pattern in meta and have pattern/alter and graph/alter)
;; sugar
;; graph (deepest-error, get-in)
;; tests
;; README

;; --- LATER ---
;; cut http://ialab.cs.tsukuba.ac.jp/~mizusima/publications/paste513-mizushima.pdf
;; gens
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
)

(comment
  (def ot (pattern (1 2)))
  [0 * ~ot] --> [0 ((1 2) (1 2))]
  [0 * & ~ot] --> [0 (1 2 1 2)]
  [0 & * ~ot] --> [0 (1 2) (1 2)]
  [0 & * & ~ot] --> [0 1 2 1 2])
