(ns strucjure.util
  (:require clojure.walk
            [plumbing.core :refer [for-map map-vals]]))

(defn when-nil [form body]
  (if (nil? form)
    body
    `(when (nil? ~form) ~body)))

(defmacro let-syms [syms & rest]
  `(let ~(vec (apply concat (for [sym syms] [sym `(gensym ~(str sym))])))
     ~@rest))
