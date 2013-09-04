(ns strucjure.util
  (:refer-clojure :exclude [assert])
  (:require clojure.walk
            [plumbing.core :refer [for-map map-vals]]))

(defmacro assert [bool & msg]
  `(clojure.core/assert ~bool (binding [*print-meta* true] (pr-str ~@msg))))

(defn update
  ([map & keys&funs]
     (if-let [[key fun & keys&funs] keys&funs]
       (recur (assoc map key (fun (get map key))) keys&funs)
       map)))

(defn key->sym [key]
  (symbol (.substring (str key) 1)))

(defn fnk->call [fnk]
  (if-let [[pos-fn keywords] (:plumbing.fnk.impl/positional-info (meta fnk))]
    (cons pos-fn (map key->sym keywords))
    (assert nil (pr-str "Not a fnk:" fnk))))

(defmacro with-syms [syms & body]
  `(let ~(vec (apply concat (for [sym syms] [sym `(gensym ~(str sym))])))
     ~@body))
