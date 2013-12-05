(ns strucjure.sugar
  (:refer-clojure :exclude [with-meta * + or and])
  (:require [plumbing.core :refer [fnk for-map aconcat]]
            [strucjure.pattern :as pattern :refer [->Rest ->Any ->Is ->Guard ->Name ->Or ->And ->Repeated ->WithMeta ->Output ->Let ->Refer ->Total]]
            [strucjure.view :as view]))

(def _ (->Any))
(defmacro is [f] `(->Is '~f))
(defmacro guard [pattern code] `(->Guard ~pattern '~code))
(def name ->Name)
(defn * [pattern] (->Repeated 0 Long/MAX_VALUE pattern))
(defn + [pattern] (->Repeated 1 Long/MAX_VALUE pattern))
(defn ? [pattern] (->Repeated 1 1 pattern))
(def with-meta ->WithMeta)
(defn or [& patterns] (->Or (vec patterns)))
(defn and [& patterns] (->And (vec patterns)))
(def & ->Rest)
(defn &* [pattern] (& (* pattern)))
(defn &+ [pattern] (& (+ pattern)))
(defn &? [pattern] (& (? pattern)))
(defn &*& [pattern] (& (* (& pattern))))
(defn &+& [pattern] (& (+ (& pattern))))
(defn &?& [pattern] (& (? (& pattern))))
(defn *& [pattern] (* (& pattern)))
(defn +& [pattern] (+ (& pattern)))
(defn ?& [pattern] (? (& pattern)))

(defmacro output [pattern code] `(->Output ~pattern '~code))

(defn- with-names [form]
  (clojure.walk/prewalk
   (fn [form]
     (if-let [name (:tag (meta form))]
       `(->Name '~name ~(vary-meta form dissoc :tag))
       form))
   form))

(def overrides #{'_ 'is 'guard 'name '* '+ '? 'with-meta 'or 'and 'seqable '& '&* '&+ '&? '&*& '&+& '&?&})

(defn- with-overrides [form]
  (clojure.walk/prewalk
   (fn [form]
     (if (contains? overrides form)
       (symbol (str "strucjure.sugar/" form))
       form))
   form))

(defmacro pattern [sugar]
  (with-overrides (with-names sugar)))

(defmacro case [& patterns&outputs]
  (cond
   (= 1 (count patterns&outputs)) `(->Total (pattern ~(first patterns&outputs)))
   (even? (count patterns&outputs)) `(->Or [~@(for [[pattern output] (partition 2 patterns&outputs)]
                                                `(->Output (->Total (pattern ~pattern)) '~output))])))

(defmacro letp [names&patterns & patterns&outputs]
  `(let [~@(aconcat
            (for [[name pattern] (partition 2 names&patterns)]
              [name `(->Name '~name (->Refer '~name))]))]
     (->Let ~(for-map [[name pattern] (partition 2 names&patterns)] `'~name `(pattern ~pattern))
            (case ~@patterns&outputs))))

(defmacro match [input & patterns&outputs]
  (let [pattern (eval `(case ~@patterns&outputs))]
    `(let [~view/input ~input] ~(view/view-top pattern))))

(comment
  (set! *warn-on-reflection* true)

  (= (list 1 2 3) (list 1 2 3))

  (match (list 1 2 3)
         (list 1 2 3))

  (match (list 1 2)
         (list 1) :fail
         2 :fail
         (list 1 3) :fail)

  (match 1 ^x _ x)

  (match [1 2 3]
         [1 ^y _ 3] y)

  (match {:a 1 :b 2}
         {:c 1} :fail
         {:a 1 :b 2} :ok)

  (match [1 2]
         (and ^z _ [1 ^y _]) [z y])

  (match [1 2 3]
         (guard [1 ^z _ ^y _] (= z y)) :fail
         (guard [1 ^z _ ^y _] (= (inc z) y)) [z y])

  (match [1 2 3]
         [1 (& ^z _)] z)

  (match [1 2 3]
         [1 ^z (& _)] z)

  (match [1 2 3 4 5]
         ^y [1 ^z (& [_ _]) ^w (& [_ _])] [y z w])

  (match [1 2]
         ^z (* (is integer?)) z)

  (match [1 2 1 2 1 2]
         ^z (*& [1 2]) z)

  (match [1 2 1 2 1 2 3]
         (*& [1 2]) :fail
         _ :ok)

  (match [1 2 1 2 1 2 3]
         ^w [^y (&*& [1 2]) ^z (& _)] [w y z])

  (match [1 1 1]
         (+ 1) :ok)

  (match []
         (* 1) :ok)

  (match [1]
         (? 1) :ok)

  (match [1 2 3]
         (* (is integer?)))

  (match [1 2 3]
         (letp [i (is integer?)]
               (* i)))


  (doall (for [[a b] (partition 2 (range 10))]
           (clojure.core/+ a b)))

  (match (range 10)
         (*& (output [^a _ ^b _] (list (clojure.core/+ a b)))))

  (letfn [(sum-pairs
           ([] nil)
           ([a b & rest] (cons (clojure.core/+ a b) (apply sum-pairs rest))))]
    (apply sum-pairs (range 10)))

  (letfn [(sum-pairs [pairs]
                     (if-let [[a b & rest] pairs]
                       (cons (clojure.core/+ a b) (sum-pairs rest))
                       nil))]
    (sum-pairs (range 10)))


  )