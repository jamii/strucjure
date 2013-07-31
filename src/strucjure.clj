(ns strucjure)

;; --- TODO ---
;; pattern
;; parser
;; common records
;; view [output/Fail remaining] (provides cut)
;; sugar (raw/sour) (splicing)
;; tests
;; benchmark + optimise (proper locals?)

;; --- LATER ---
;; graph (extensible, memo, trampoline, inline etc)
;; useful error messages (deepest match?)
;; interactive debugger
;; reversible patterns
;; type hinting
;; string patterns
;; binary patterns

;; --- LESSONS ---
;; representation as plain data-structure, minimal syntax
;; splice as data macro - wish clojure did this, julia got it right
;; placeholders instead of passing closures
;; separate pattern, parser, view, graph
