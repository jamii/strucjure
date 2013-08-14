(ns strucjure.pattern
  (:require [strucjure.util :as util]))

;; TODO think more about extension points eg relation between IPrimitivePattern, IPattern, IView, IGen
;; TODO _, When, Is, Vector, Map, Set, Record
;; TODO in sugar need to macroexpand any user form in place

(defrecord Bind [pattern symbol])
(defrecord Output [pattern form])

(defrecord Or [patterns])
(defrecord And [patterns])

(defrecord Rest [pattern])
(defrecord ZeroOrMore [pattern])

(defrecord View [form])
