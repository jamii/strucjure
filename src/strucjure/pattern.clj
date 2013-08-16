(ns strucjure.pattern
  (:require [strucjure.util :as util]))

;; TODO Record
;; TODO Set? (how would you match subpatterns? maybe only allow bind/with-meta? or only value patterns)
;; TODO Atom/Ref/Agent? (what would the output be?)

(defrecord Any [])
(defrecord Is [form])
(defrecord Guard [pattern form])

;; ISeq, IVector, IPersistentMap
(defrecord Seqable [patterns])
(defrecord WithMeta [pattern meta-pattern])

(defrecord Bind [pattern symbol])
(defrecord Output [pattern form])

(defrecord Or [patterns])
(defrecord And [patterns])

(defrecord Rest [pattern])
(defrecord ZeroOrMore [pattern])

(defrecord View [form])
