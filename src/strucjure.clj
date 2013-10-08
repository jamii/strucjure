(ns strucjure (:require [strucjure.pattern :as p]
                       [strucjure.graph :as g]
                       [strucjure.sugar :as s]
                       [strucjure.view :as v]
                       [plumbing.core :refer [fnk]]))

;; --- TODO ---
;; finish ns example
;; README - http://hugoduncan.org/post/evaluate_clojure_in_emacs_markdown_buffers/ or similar
;; tests (string in regression, readme, bootstrap, generative)
;; optimisations in view
;; error reporting - deepest-error in graph, maybe first-error in pattern
;; Input pattern for prewalks?
;; license?

;; --- SKETCH ---
;; if not output? return input instead (and then look for Output in children)
;; pseudo-patterns should use callback in view compiler
;; could import graphs using nested names eg :require:libspec
;; the problem with ns-grammer is not a bug - its a semantics problem...
;;   could be fixed in this case with (or option libspec)
;;   but is confusing and broken in general
;;   at minimum need to be able to push context down into nodes

;; --- LATER ---
;; for prewalks can just build ast by attaching name and bindings in metadata
;; need to be able to alter views (store original pattern in meta and have pattern/alter and graph/alter)
;; cut by returning delay - can trampoline to the nearest try - needs work inside Or/ZeroOrMore
;; gens
;; types
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
;; benchmark as soon as possible

;; --- MOTIVATION ---
;; types vs data
;; correct-by-construction as opposed to analysing ad-hoc code
;; use :refer-all as example

(comment
  (def ot (pattern (1 2)))
  [0 * ~ot] --> [0 ((1 2) (1 2))]
  [0 * & ~ot] --> [0 (1 2 1 2)]
  [0 & * ~ot] --> [0 (1 2) (1 2)]
  [0 & * & ~ot] --> [0 1 2 1 2])

;; http://www.mercury.csse.unimelb.edu.au/information/papers/packrat.pdf

;; 'This paper argues that (a) packrat parsers can be trivially implemented using a combination
;; of deﬁnite clause grammar rules and memoing, and that (b) packrat
;; parsing may actually be signiﬁcantly less eﬃcient than plain recursive
;; descent with backtracking, but (c) memoing the recognizers of just one or
;; two nonterminals, selected in accordance with Amdahl’s law, can some-
;; times yield speedups. We present experimental evidence to support these
;; claims.'

(def ns-grammar
  (s/graph
   ns (ns ^name ~symbol & ? ~docstring & ? ~attr-map & * ~reference)
   docstring ~(s/is string?)
   attr-map ~(s/is map?)
   reference ~(s/or ~require ~import)
   require (:require & * ~libspec)
   libspec ~(s/or ~symbol
                  [^prefix ~symbol & * ~libspec]
                  [~symbol & * & ~option])
   option ~(s/or (:as ~symbol)
                 (:refer ~(s/or :all [& * ~symbol]))
                 (:reload)
                 (:reload-all)
                 (:verbose))
   import (:import & * [~symbol & * ~symbol])
   symbol ~(s/is symbol?)))

(def ns-validate
  (v/with-layers [v/with-depth v/with-deepest-failure]
    (v/*view* (s/node-of 'ns ns-grammar))))

(strucjure/ns-validate '(ns foo (:require [bar :refer :all])))

(strucjure/ns-validate '(ns foo (:require [bar :refer-all])))
