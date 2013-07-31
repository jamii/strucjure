(ns strucjure)

;; --- TODO ---
;; check nil on subpatterns
;; more datatypes
;; (pattern as sugar+common)
;; (view as compiler)
;; pattern->view [output/Fail remaining] (provides cut)
;; ~fn ~var ~(invoke (pattern x y z)) ~(recur x y z)
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
;; various protocols give context sensitive behaviour
;; bush->tree trick made easier to unify whole compiler
;; using Fail allows safe cut

;; --- SCRATCH ---
;; symbol, !var, ?nullable
;; ~pattern
;; ~(s/or x y) ~(s/is x) ~(s/when (= x 1)) ~(s/+ x)
;; NOT p/or, p/and, v/or, v/and
;; ~fn ~var -- call compiled?
;; ~(recur x y)?
;; don't need context-sensitive if we can build our own views using s/fail s/succeed
;; ~(view pattern output) ~(call view pattern-for-output) pattern->view, view->pattern
;; use match as compiler
;; later - run over generated code and remove (if _ true false) etc
;; tree is easy, use chunk for dag, use fns for cycles

;; --- OPTIMISER ---
;; (if _ true false) (if _ false true) (when _ true) etc
;; (do nil & rest)
