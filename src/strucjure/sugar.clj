(ns strucjure.sugar
  (:refer-clojure :exclude [with-meta * + or and name case])
  (:require [plumbing.core :refer [fnk for-map aconcat]]
            [strucjure.pattern :as pattern :refer [->Rest ->Any ->Is ->Guard ->Name ->Or ->And ->Repeated ->WithMeta ->Output ->Let ->Refer ->Trace]]
            [strucjure.view :as view])
  (:import [strucjure.pattern Let]))

(def _ (->Any))
(defmacro is [f] `(->Is '~f))
(defmacro guard [pattern code] `(->Guard ~pattern '~code))
(def name ->Name)
(defn * [pattern] (->Repeated 0 Long/MAX_VALUE pattern))
(defn + [pattern] (->Repeated 1 Long/MAX_VALUE pattern))
(defn ? [pattern] (->Repeated 1 1 pattern))
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
(defn *& [pattern] (* (& pattern)))
(defn +& [pattern] (+ (& pattern)))
(defn ?& [pattern] (? (& pattern)))

(def not-nil (is #(not (nil? %))))

(defmacro output [pattern code] `(->Output ~pattern '~code))

(defn- with-names [form]
  (clojure.walk/prewalk
   (fn [form]
     (if-let [name (:tag (meta form))]
       `(->Name '~name ~(vary-meta form dissoc :tag))
       form))
   form))

(defmacro pattern [sugar]
  (with-names sugar))

(defmacro case [& patterns&outputs]
  (cond
   (= 1 (count patterns&outputs)) `(pattern ~(first patterns&outputs))
   (even? (count patterns&outputs)) `(->Or [~@(for [[pattern output] (partition 2 patterns&outputs)]
                                                `(->Output (pattern ~pattern) '~output))])))

(defmacro letp [names&patterns & patterns&outputs]
  `(let [~@(aconcat
            (for [[name pattern] (partition 2 names&patterns)]
              [name `(->Name '~name (->Refer '~name))]))]
     (->Let ~(for-map [[name pattern] (partition 2 names&patterns)] `'~name `(pattern ~pattern))
            (case ~@patterns&outputs))))

(defn keys* [& symbols]
  (for-map [symbol symbols]
           (keyword (str symbol))
           (->Name symbol (->Any))))

(defmacro keys [& symbols]
  `(keys* ~@(for [symbol symbols] `'~symbol)))

(defmacro match [input & patterns&outputs]
  (let [pattern (eval `(case ~@patterns&outputs))]
    `(let [~view/input ~input] ~(view/view-top pattern))))

(defn trace-let [pattern]
  (if (instance? Let pattern)
    (assoc (pattern/walk pattern trace-let)
      :refers (for-map [[name pattern] (:refers pattern)] name
                       (->Trace (str name) (trace-let pattern))))
    (pattern/walk pattern trace-let)))

(defn trace-all [pattern]
  (->Trace (pr-str pattern)
           (if (instance? Let pattern)
             (assoc (pattern/walk pattern trace-all)
               :refers (for-map [[name pattern] (:refers pattern)] name (trace-all pattern)))
             (pattern/walk pattern trace-all))))

(defmacro match-with [tracer input & patterns&outputs]
  (let [pattern (eval `(~tracer (case ~@patterns&outputs)))]
    `(let [~view/input ~input
           ~view/depth (proteus.Containers$O. 0)]
       ~(view/view-top pattern))))