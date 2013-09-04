(ns strucjure.pattern
  (:refer-clojure :exclude [assert])
  (:require [clojure.set :refer [union]]
            [plumbing.core :refer [for-map aconcat map-vals fnk]]
            [strucjure.util :refer [with-syms assert]]
            [strucjure.core :as core]))

;; TODO Records
;; TODO need a general way to indicate that output is unchanged for eg WithMeta
;;      just check (= input output)?
;; TODO think carefully about seq vs list
;; TODO could return to having implicit equality but would require careful thinking about Guard/Output
;;      state needs to track 'has it been bound before' and 'will it need to be bound again' - not exclusive
;; TODO Rest and Chain are messed up. ugliness shows in seq->core

(defprotocol IPattern
  (pattern->core [this]))

(defn total [core]
  (if (core/->has? core None)
    (core/->GuardInner core (fnk [&remaining] (nil? &remaining)))
    core))

;; --- REST ---

(defrecord Rest [pattern]
  IPattern
  (pattern->core [this]
    (assert nil "Rest patterns can only be used as direct children of a sequence pattern:" this)))

(defn cons->core [pattern core]
  (core/->GuardOuter identity
                     (with-syms [input first-input rest-input]
                       (core/->Struct input
                                      [[first-input (total (pattern->core pattern)) `(first ~input)]
                                       [rest-input core `(rest ~input)]]
                                      `(cons ~first-input ~rest-input)))))

(defn concat->core [pattern core]
  (with-syms [input first-input rest-input]
    (core/->Struct input
                   [[first-input (pattern->core pattern) input]
                    [rest-input core :remaining]]
                   `(concat ~first-input ~rest-input))))

;; --- VALUE PATTERNS ---

(defn elem->core [pattern core]
  (if (instance? Rest pattern)
    (concat->core (:pattern pattern) core)
    (cons->core pattern core)))

(defn seq->core
  ([] (core/->None))
  ([pattern & patterns] (elem->core pattern (seq->core patterns))))

(defn map->core [key->pattern]
  (with-syms [input]
    (let [syms (for-map [key (keys key->pattern)] key (gensym (name key)))]
      (core/->Struct input
                     (for [[key pattern] key->pattern] [(syms key) (total (pattern->core pattern)) `(get ~input ~(syms key)) :total])
                     `(assoc ~input ~(aconcat syms))))))

(extend-protocol IPattern
  nil
  (pattern->core [this]
    (core/->Constant nil))
  Object
  (pattern->core [this]
    (core/->Constant this))
  clojure.lang.ISeq
  (pattern->core [this]
    (core/->GuardOuter #(or (nil? %) (seq? %)) (apply seq->core this)))
  clojure.lang.IPersistentVector
  (pattern->core [this]
    (core/->GuardOuter vector? (apply seq->core this)))
  clojure.lang.IPersistentMap
  (pattern->core [this]
    (core/->GuardOuter map? (map->core this))))

(defrecord Seqable [patterns]
  IPattern
  (pattern->core [this]
    (core/->GuardOuter #(instance? clojure.lang.Seqable %) (apply seq->core patterns))))

;; --- LOGICAL PATTERNS ---

(defrecord Any []
  IPattern
  (pattern->core [this]
    (core/->All)))

(defrecord Is [f]
  IPattern
  (pattern->core [this]
    (core/->GuardOuter f (core/->All))))

(defrecord Guard [pattern fnk]
  IPattern
  (pattern->core [this]
    (core/->GuardInner (pattern->core pattern) fnk)))

(defrecord Name [symbol pattern]
  IPattern
  (pattern->core [this]
    (core/->Name symbol (pattern->core pattern))))

(defn bool->core
  ([bool] (assert nil "Boolean cannot be empty"))
  ([bool pattern] (pattern->core pattern))
  ([bool pattern & patterns] (bool (pattern->core pattern) (apply bool->core bool patterns))))

(defrecord Or [patterns]
  IPattern
  (pattern->core [this]
    (apply bool->core core/->Or patterns)))

(defrecord And [patterns]
  IPattern
  (pattern->core [this]
    (apply bool->core core/->And patterns)))

(defrecord ZeroOrMore [pattern]
  IPattern
  (pattern->core [this]
    (core/->ZeroOrMore (elem->core pattern (core/->None)))))

(defrecord WithMeta [pattern meta-pattern]
  IPattern
  (pattern->core [this]
    (core/->And (with-syms [input meta-input]
                  (core/->Struct input
                                 [[meta-input (total (pattern->core meta-pattern)) `(meta ~input)]]
                                 `(if (instance? clojure.lang.IObj ~input)
                                    (with-meta ~input ~meta-input)
                                    ~input)))
                (pattern->core pattern))))
