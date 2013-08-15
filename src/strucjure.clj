(ns strucjure)

;; --- TODO ---
;; more datatypes
;; sugar
;; graph (trace, deepest-error, get-in, update-in, output-in)
;; tests
;; README

;; --- LATER ---
;; cut
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
)

(comment
  (def ot (pattern (1 2)))
  [0 * ~ot] --> [0 ((1 2) (1 2))]
  [0 * & ~ot] --> [0 (1 2 1 2)]
  [0 & * ~ot] --> [0 (1 2) (1 2)]
  [0 & * & ~ot] --> [0 1 2 1 2])
