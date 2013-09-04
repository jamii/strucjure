(ns strucjure.core
  (:refer-clojure :exclude [assert])
  (:require [clojure.set :refer [subset? union intersection difference]]
            [strucjure.util :refer [update assert fnk->call]]
            [strucjure.util :refer [assert]]))

;; the narrow waist

(defrecord All [])
(defrecord None [])
(defrecord Name [name core])
(defrecord GuardOuter [fn core])
(defrecord GuardInner [core fnk])
(defrecord Struct [input parts output]) ;; parts is [[name core input/:remaining]]
(defrecord Or [core-a core-b])
(defrecord And [core-a core-b])
(defrecord ZeroOrMore [core])

(defn core? [form]
  (#{All None Name GuardOuter GuardInner Struct Or And ZeroOrMore} (class form)))

(defn subcores [core]
  (assert (core? core) core "is not a core")
  (condp contains? (class core)
    #{All None} []
    #{Struct} (for [[_ subcore _] (:parts core)] subcore)
    #{Or And} [(:core-a core) (:core-b core)]
    [(:core core)]))

(defn with-subcores [core subcores]
  (assert (core? core) core "is not a core")
  (condp contains? (class core)
    #{All None} core
    #{Struct} (assoc core :parts (for [[[name _ input] subcore] (map vector (:parts core) subcores)] [name subcore input]))
    #{Or And} (assoc core :core-a (first subcores) :core-b (second subcores))
    (assoc core :core (first subcores))))

(defn fmap [core f]
  (with-subcores core (map f (subcores core))))

(defn postwalk [core f]
  (f (fmap core f)))

(defn with-bound [core]
  (let [subcores (map with-bound (subcores core))
        bounds (map #(:bound (meta %)) subcores)]
    (if-not (instance? strucjure.core.Or core)
      (let [collisions (apply intersection #{} bounds)]
        (assert (empty? collisions) "Names" collisions "collide in" core)))
    (let [bound (conj (apply union bounds) (:name core))]
      (vary-meta (with-subcores core subcores) assoc :bound bound))))

(defn with-used [core used]
  (let [used (if-let [fnk (:fnk core)]
               (union used (rest (fnk->call fnk)))
               used)
        used? (used (:name core))]
    (vary-meta (fmap core #(with-used % used)) assoc :used used :used? used?)))

(defn get-used [core]
  (apply union (:used (meta core)) (map get-used (subcores core))))

(defn check-unused [core names]
  (let [unused (difference (set names) (:bound (meta core)))]
    (assert (empty? unused) "Names" unused "are overridden but not bound in" core)))

(defn step-check-unbound [core]
  (let [unbound (difference (:used (meta core)) (:bound (meta core)))]
    (assert (empty? unbound) "Names" unbound "are used but not bound in" core)))

(defn has? [core class]
  (or (instance? class core) (some #(has? % class) (subcores core))))
