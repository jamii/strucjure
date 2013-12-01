(ns strucjure.sugar
  (:refer-clojure :exclude [with-meta * + or and])
  (:require [plumbing.core :refer [fnk for-map aconcat]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern :refer [->Rest ->Seqable ->Any ->Is ->Guard ->Name ->Or ->And ->Repeated ->WithMeta]]
            [strucjure.view :as view]))

(def _ (->Any))
(def is ->Is)
(def guard ->Guard)
(def name ->Name)
(defn * [pattern] (->Repeated 0 Long/MAX_VALUE pattern))
(defn + [pattern] (->Repeated 1 Long/MAX_VALUE pattern))
(defn ? [pattern] (->Repeated 0 1 pattern))
(def with-meta ->WithMeta)
(defn or [& patterns] (->Or (vec patterns)))
(defn and [& patterns] (->And (vec patterns)))
(defn seqable [& patterns] (->Seqable (vec patterns)))
(def & ->Rest)
(defn &* [pattern] (& (* pattern)))
(defn &+ [pattern] (& (+ pattern)))
(defn &? [pattern] (& (? pattern)))
(defn &*& [pattern] (& (* (& pattern))))
(defn &+& [pattern] (& (+ (& pattern))))
(defn &?& [pattern] (& (? (& pattern))))

(defn- with-names [form]
  (clojure.walk/prewalk
   (fn [form]
     (if-let [name (:tag (meta form))]
       `(->Name '~name ~(vary-meta form dissoc :tag))
       form))
   form))

(def overrides #{'_ 'is 'guard 'name '* '+ '? 'with-meta 'or 'and 'seqable '& '&* '&+ '&? '&*& '&+& '&?&})

(defn- with-overrides [form]
  (clojure.walk/prewalk
   (fn [form]
     (if (contains? overrides form)
       (symbol (str "strucjure.sugar/" form))
       form))
   form))

(defmacro pattern [sugar]
  (with-overrides (with-names sugar)))

(defmacro match [input & patterns&outputs]
  (assert (even? (count patterns&outputs)))
  (let [pattern (pattern/->Or
                 (for [[pattern output] (partition 2 patterns&outputs)]
                   (pattern/->Output (eval `(pattern ~pattern)) output)))]
    `(let [~view/input ~input] ~(view/view-direct pattern))))

(comment
  (match (list 1 2 3)
         (list 1 2) :fail
         (list 1 (->Name 'x 2) 3) x)
  )