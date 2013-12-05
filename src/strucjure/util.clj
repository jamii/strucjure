(ns strucjure.util
  (:refer-clojure :exclude [assert])
  (:require [clojure.set :refer [union]]
            [plumbing.core :refer [aconcat]]))

(defmacro extend-protocol-by-fn [protocol & fns]
  (let [class->fns (apply merge-with union
                                (for [[fn-symbol fn-name fn-args & classes&bodies] fns
                                      [classes body] (partition 2 classes&bodies)
                                      class classes]
                                  {class #{(list fn-name fn-args body)}}))]
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
