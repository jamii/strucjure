(ns strucjure.sugar
  (:refer-clojure :exclude [with-meta * + or and])
  (:require [plumbing.core :refer [fnk for-map aconcat]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern :refer [->Rest ->Any ->Is ->Guard ->Name ->Or ->And ->Repeated ->WithMeta]]
            [strucjure.view :as view]))

(def _ (->Any))
(defmacro is [f] `(->Is '~f))
(defmacro guard [pattern code] `(->Guard ~pattern '~code))
(def name ->Name)
(defn * [pattern] (->Repeated 0 Long/MAX_VALUE pattern))
(defn + [pattern] (->Repeated 1 Long/MAX_VALUE pattern))
(defn ? [pattern] (->Repeated 0 1 pattern))
(def with-meta ->WithMeta)
(defn or [& patterns] (->Or (vec patterns)))
(defn and [& patterns] (->And (vec patterns)))
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
    `(let [~view/input ~input] ~(view/view-with-locals pattern))))

(comment
  (= [1 2 3] [1 2 3])

  (match [1 2 3]
         (list 1 2 3) :fail
         [1 ^x _ 3] x)

  (match {:a 1 :b 2}
         {:c 1} :fail
         {:a 1 :b 2} :ok)

  (match [1 2]
         (and ^x _ [1 ^y _]) [x y])

  (match [1 2 3]
         (guard [1 ^x _ ^y _] (= x y)) :fail
         (guard [1 ^x _ ^y _] (= (inc x) y)) [x y])
  )