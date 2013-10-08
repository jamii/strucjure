(ns strucjure)

;; --- TODO ---
;; README - http://hugoduncan.org/post/evaluate_clojure_in_emacs_markdown_buffers/ or similar
;; tests (string in regression, readme, bootstrap, generative)

;; --- LATER ---
;; cut by returning delay - can trampoline to the nearest try - needs work inside Or/ZeroOrMore
;; gens
;; types
;; string patterns, ~(chain "foo" "/" "bar")
;; binary patterns

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
;; benchmark as soon as possible
;; without a specific goal ends up heavily over-engineered

;; --- MOTIVATION ---
;; types vs data
;; correct-by-construction as opposed to analysing ad-hoc code
;; use :refer-all as example
