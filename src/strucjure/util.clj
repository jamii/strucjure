(ns strucjure.util
  (:refer-clojure :exclude [assert])
  (:require [clojure.set :refer [union]]
            [plumbing.core :refer [aconcat]]))

(defmacro assert [bool & msg]
  `(clojure.core/assert ~bool (binding [*print-meta* true] (pr-str ~@msg))))

(defn key->sym [key]
  (symbol (.substring (str key) 1)))

(defn fnk->pos-fn [fnk]
  (if-let [[pos-fn keywords] (:plumbing.fnk.impl/positional-info (meta fnk))]
    pos-fn
    (assert nil "Not a fnk:" fnk)))

(defn fnk->args [fnk]
  (if-let [[pos-fn keywords] (:plumbing.fnk.impl/positional-info (meta fnk))]
    (map key->sym keywords)
    (assert nil "Not a fnk:" fnk)))

(defmacro with-syms [syms & body]
  `(let ~(vec (apply concat (for [sym syms] [sym `(gensym ~(str sym))])))
     ~@body))

(defmacro extend-protocol-by-fn [protocol & fns]
  (let [class->fns (apply merge-with union
                                (for [[fn-symbol fn-name fn-args & classes&bodies] fns
                                      [classes body] (partition 2 classes&bodies)
                                      class classes]
                                  {class #{(list fn-name fn-args body)}}))]
    (print class->fns)
    `(extend-protocol ~protocol
       ~@(aconcat (for [[class fns] class->fns] (cons class fns))))))

(defn try-vary-meta [obj & args]
  (if (instance? clojure.lang.IObj obj)
    (apply vary-meta obj args)
    obj))

(defn try-with-meta [obj meta]
  (if (instance? clojure.lang.IObj obj)
    (with-meta obj meta)
    obj))
