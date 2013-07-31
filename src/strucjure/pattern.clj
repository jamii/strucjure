(ns strucjure.pattern)

(defrecord Bind [symbol])

(defrecord Or [patterns])
(defrecord And [patterns])

(defrecord & [pattern])
