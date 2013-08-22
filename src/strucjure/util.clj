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

(defn key->sym [key]
  (symbol (.substring (str key) 1)))

;; TODO check for closures
(defn fnk->clj [fnk]
  (if-let [[pos-fn keywords] (:plumbing.fnk.impl/positional-info (meta fnk))]
    (let [args (map key->sym keywords)]
      [args `(~pos-fn ~@args)])
    (throw (Exception. (pr-str "Not a fnk:" fnk)))))
