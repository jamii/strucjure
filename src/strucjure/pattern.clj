(ns strucjure.pattern
  (:require [clojure.set :refer [union difference]]
            [plumbing.core :refer [for-map aconcat map-vals]]
            [strucjure.util :refer [extend-protocol-by-fn try-vary-meta try-with-meta]])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap IRecord]))

;; TODO Records

(defprotocol Pattern
  (subpatterns [this] "A list of subpatterns of this pattern (just children, not descendants)")
  (with-subpatterns [this subpatterns] "Replace the subpatterns of this pattern, preserving metadata (if the number of subpatterns is wrong the behaviour is unspecified)")
  (bound [this] "Which names are bound by this pattern (not by subpatterns))"))

;; patterns
(defrecord Any [])
(defrecord Is [f])
(defrecord Guard [pattern code])
(defrecord Name [name pattern])
(defrecord Repeated [min-count max-count pattern])
(defrecord WithMeta [pattern meta-pattern])
(defrecord Or [patterns])
(defrecord And [patterns])

;; recursive patterns
(defrecord Refer [name])
(defrecord Let [refers pattern])

;; pseudo-patterns
(defrecord Rest [pattern])
(defrecord Output [pattern code])
(defrecord Trace [name pattern])

(defn walk [pattern f]
  (with-subpatterns pattern (map f (subpatterns pattern))))

(extend-protocol-by-fn
 Pattern

 (fn subpatterns [this]
   [nil Object Any Is Refer] nil
   [ISeq IPersistentVector] this
   [IPersistentMap IRecord] (vals this)
   [Rest Guard Name Repeated Output Let Trace] [(:pattern this)]
   [WithMeta] [(:pattern this) (:meta-pattern this)]
   [Or And] (:patterns this))

 (fn with-subpatterns [this subpatterns]
   [nil Object Any Is Refer] this
   [ISeq] (apply list subpatterns)
   [IPersistentVector] (vec subpatterns)
   [IPersistentMap IRecord] (reduce (fn [this [key value]] (assoc this key value)) this (map vector (keys this) subpatterns))
   [Rest Guard Name Repeated Output Let Trace] (assoc this :pattern (first subpatterns))
   [WithMeta] (assoc this :pattern (first subpatterns) :meta-pattern (second subpatterns))
   [Or And] (assoc this :patterns subpatterns))

 (fn bound [this]
   [nil Object ISeq IPersistentVector IPersistentMap IRecord Any Is Rest Guard Repeated WithMeta Or And Refer Let Output Trace] #{}
   [Name] #{(:name this)}))

(defn with-bound [pattern]
  (let [subpatterns&bound-below (map with-bound (subpatterns pattern))
        bound-here (apply union (bound pattern) (map second subpatterns&bound-below))
        pattern (with-subpatterns pattern (map first subpatterns&bound-below))
        pattern (try-vary-meta pattern assoc :bound-here bound-here)]
    [pattern bound-here]))
