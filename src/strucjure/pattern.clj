(ns strucjure.pattern
  (:require [strucjure.util :as util]))

;; TODO Vector, Map, Set, Record

(defrecord Any [])
(defrecord Is [form])
(defrecord Guard [pattern form])

(defrecord Seq [patterns])

(defrecord Bind [pattern symbol])
(defrecord Output [pattern form])

(defrecord Or [patterns])
(defrecord And [patterns])

(defrecord Rest [pattern])
(defrecord ZeroOrMore [pattern])

(defrecord View [form])
