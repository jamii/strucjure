(ns strucjure.pattern
  (:refer-clojure :exclude [assert])
  (:require [clojure.set :refer [union difference]]
            [plumbing.core :refer [for-map aconcat map-vals fnk]]
            [strucjure.util :refer [with-syms assert extend-protocol-by-fn fnk->args try-vary-meta]]
            [strucjure.core :as core])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]))

;; TODO Records
;; TODO think carefully about seq vs list

(defprotocol Subpatterns
  (subpatterns [this] "A list of subpatterns of this pattern (just children, not descendants)")
  (with-subpatterns [this subpatterns] "Replace the subpatterns of this pattern, preserving metadata (if the number of subpatterns is wrong the behaviour is unspecified)")
  (used [this] "Which names are used by this pattern (not subpatterns)")
  (bound [this] "Which names are bound by this pattern (not subpatterns))"))

(defrecord Any [])
(defrecord Is [fn])
(defrecord Rest [pattern])
(defrecord Guard [pattern fnk])
(defrecord Output [pattern fnk])
(defrecord Name [symbol pattern])
(defrecord ZeroOrMore [pattern])
(defrecord WithMeta [pattern meta-pattern])
(defrecord Or [patterns])
(defrecord And [patterns])
(defrecord Seqable [patterns])

(extend-protocol-by-fn
 Pattern

 (fn subpatterns [this]
   [nil Object Any Is] nil
   [ISeq IPersistentVector] this
   [IPersistentMap] (vals this)
   [Rest Guard Output Name ZeroOrMore] [(:pattern this)]
   [WithMeta] [(:pattern this) (:meta-pattern this)]
   [Or And Seqable] (:patterns this))

 (fn with-subpatterns [this subpatterns]
   [nil Object Any Is] this
   [ISeq] (seq subpatterns)
   [IPersistentVector] (vec subpatterns)
   [IPersistentMap] (zipmap (keys this) subpatterns)
   [Rest Guard Output Name ZeroOrMore] (assoc this :pattern (first subpatterns))
   [WithMeta] (assoc this :pattern (first subpatterns) :meta-pattern (second subpatterns))
   [Or And Seqable] (assoc this :patterns subpatterns))

 (fn used [this]
   [nil Object ISeq IPersistentVector IPersistentMap Any Is Rest Name ZeroOrMore WithMeta Or And Seqable] #{}
   [Guard Output] (set (fnk->args (:fnk this))))

 (fn bound [this]
   [nil Object ISeq IPersistentVector IPersistentMap Any Is Rest Guard Output ZeroOrMore WithMeta Or And Seqable] #{}
   [Name] #{(:name this)}))

(defn fmap [pattern f]
  (with-subpatterns pattern (map f (subpatterns pattern))))

(defn with-scope [pattern used-above]
  (let [subpatterns&bound-below (map #(with-scope % used-above) (subpatterns pattern))
        used-here (union used-above (used pattern))
        bound-here (apply union (map second subpatterns&bound-below))
        unbound-here (difference bound-here used-here)
        pattern (with-subpatterns pattern (map first subpatterns&bound-below))
        pattern (try-vary-meta pattern assoc :used-here used-here :bound-here bound-here)]
    (assert (empty? unbound-here) "Names" unbound-here "are used but not bound in" pattern)
    [pattern bound-here]))
