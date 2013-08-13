(ns strucjure.pattern
  (:require [strucjure.util :as util]))

(defrecord Bind [symbol])

(defrecord Or [patterns])
(defrecord And [patterns])

(defrecord & [pattern])

(defrecord View [form])
