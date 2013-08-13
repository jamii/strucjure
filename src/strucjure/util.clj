(ns strucjure.util
  (:require [plumbing.core :refer [for-map map-vals]]))

(defmacro let-syms [syms & rest]
  `(let ~(vec (apply concat (for [sym syms] [sym `(gensym ~(str sym))])))
     ~@rest))
