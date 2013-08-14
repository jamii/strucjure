(ns strucjure.pattern
  (:require [strucjure.util :as util]))

;; TODO think more about extension points eg relation between IPrimitivePattern, IPattern, IView, IGen

(defrecord Bind [symbol])

(defrecord Or [patterns])
(defrecord And [patterns])

(defrecord Rest [pattern])

(defrecord View [form])
