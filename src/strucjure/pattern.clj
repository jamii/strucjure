(ns strucjure.pattern
  (:refer-clojure :exclude [assert])
  (:require [clojure.set :refer [union difference]]
            [plumbing.core :refer [for-map aconcat map-vals fnk]]
            [strucjure.util :refer [with-syms assert extend-protocol-by-fn fnk->args try-vary-meta try-with-meta]])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]))

;; TODO Records
;; TODO think carefully about seq vs list

(defprotocol Pattern
  (subpatterns [this] "A list of subpatterns of this pattern (just children, not descendants)")
  (with-subpatterns [this subpatterns] "Replace the subpatterns of this pattern, preserving metadata (if the number of subpatterns is wrong the behaviour is unspecified)")
  (used [this] "Which names are used by this pattern (not subpatterns)")
  (bound [this] "Which names are bound by this pattern (not subpatterns))"))

;; patterns
(defrecord Any [])
(defrecord Is [f])
(defrecord Guard [pattern fnk])
(defrecord Name [name pattern])
(defrecord Repeated [min-count max-count pattern])
(defrecord WithMeta [pattern meta-pattern])
(defrecord Or [patterns])
(defrecord And [patterns])
(defrecord Seqable [patterns])
(defrecord NodeOf [graph name])

;; pseudo-patterns
(defrecord Rest [pattern])
(defrecord Node [name])

(extend-protocol-by-fn
 Pattern

 (fn subpatterns [this]
   [nil Object Any Is Node NodeOf] nil
   [ISeq IPersistentVector] this
   [IPersistentMap] (vals this)
   [Rest Guard Name Repeated] [(:pattern this)]
   [WithMeta] [(:pattern this) (:meta-pattern this)]
   [Or And Seqable] (:patterns this))

 (fn with-subpatterns [this subpatterns]
   [nil Object Any Is Node NodeOf] this
   [ISeq] (apply list subpatterns)
   [IPersistentVector] (vec subpatterns)
   [IPersistentMap] (zipmap (keys this) subpatterns)
   [Rest Guard Name Repeated] (assoc this :pattern (first subpatterns))
   [WithMeta] (assoc this :pattern (first subpatterns) :meta-pattern (second subpatterns))
   [Or And Seqable] (assoc this :patterns subpatterns))

 (fn used [this]
   [nil Object ISeq IPersistentVector IPersistentMap Any Is Rest Name Repeated WithMeta Or And Seqable Node NodeOf] #{}
   [Guard] (set (fnk->args (:fnk this))))

 (fn bound [this]
   [nil Object ISeq IPersistentVector IPersistentMap Any Is Rest Guard Repeated WithMeta Or And Seqable Node NodeOf] #{}
   [Name] #{(:name this)}))

(defn fmap [pattern f]
  (try-with-meta (with-subpatterns pattern (map f (subpatterns pattern))) (meta pattern)))

(defn prewalk [pattern f]
  (fmap (f pattern) #(prewalk % f)))

(defn postwalk [pattern f]
  (f (fmap pattern #(postwalk % f))))

(defn with-bound [pattern]
  (let [subpatterns&bound-below (map with-bound (subpatterns pattern))
        bound-here (apply union (bound pattern) (map second subpatterns&bound-below))
        pattern (with-subpatterns pattern (map first subpatterns&bound-below))
        pattern (try-vary-meta pattern assoc :bound-here bound-here)]
    [pattern bound-here]))

(defn with-used [pattern used-above]
  (let [used-here (union (used pattern) used-above)
        subpatterns (map #(with-used % used-here) (subpatterns pattern))
        pattern (with-subpatterns pattern subpatterns)
        pattern (try-vary-meta pattern assoc :used-here used-here)]
    pattern))

(defn check-used-not-bound [pattern]
  (prewalk pattern (fn [pattern]
                      (let [used-not-bound (difference (used pattern) (:bound-here (meta pattern)))]
                        (assert (empty? used-not-bound) "Names" used-not-bound "are used but not bound in" pattern)))))
