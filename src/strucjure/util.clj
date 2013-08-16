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

(defn free-syms
  ([form]
     (let [free (atom #{})]
       (free-syms (clojure.walk/macroexpand-all form) free #{})
       @free))
  ([form free bound]
     (cond
      (symbol? form)
      (when-not (bound form) (swap! free conj form))

      (and (seq? form)
           (= 'let* (nth form 0)))
      (let [bound (apply conj bound (take-nth 2 (nth form 1)))]
        (clojure.walk/walk #(free-syms % free bound) identity (nthnext form 2)))

      :else
      (clojure.walk/walk #(free-syms % free bound) identity form))))
