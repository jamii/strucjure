(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [clojure.walk :refer [prewalk postwalk-replace]]
            [plumbing.core :refer [aconcat for-map]]
            [strucjure.util :refer [with-syms assert fnk->pos-fn fnk->args extend-protocol-by-fn try-with-meta]]
            [strucjure.pattern :as pattern]
            [proteus :refer [let-mutable]])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]
           [strucjure.pattern Any Is Rest Guard Name Repeated WithMeta Or And Refer Where Output]
           [strucjure.view Failure]))

;; TODO
;; parsing Rest Repeated seq->view
;; recursive Refer Where

;; INTERFACE

(defprotocol View
  (view [this]))

(def input (gensym "input"))

(defmacro let-input [value body]
  `(let [~input ~value] ~body))

;; FAILURE

(def failure
  (Failure. ""))

(defmacro on-fail [t f]
  `(try ~t
        (catch Failure exc#
          ~f)))

(defmacro trap-failure [body]
  `(try ~body
        (catch Exception exc#
          (if (instance? Failure exc#)
            (throw (Exception. (str exc#)))
            (throw exc#)))))

(defmacro check [pred]
  `(if-not ~pred (throw failure)))

;; STRUCTURAL PATTERNS

(defn seq->view [pattern]
  (if-let [[first-pattern & next-pattern] pattern]
    `(cons
      (let-input (first ~input) ~(view first-pattern))
      (let-input (next ~input) ~(seq->view next-pattern)))
    `(check (nil? ~input))))

(defn or->view [patterns]
  (assert (not (empty? patterns)) "OR patterns must not be empty")
  (let [[first-pattern & next-pattern] patterns]
    (if next-pattern
      `(on-fail ~(view first-pattern)
                ~(or->view next-pattern))
      (view first-pattern))))

(extend-protocol-by-fn
 View
 (fn view [{:keys [pattern patterns meta-pattern name code f min-count max-count] :as this}]
   [nil Object]
   `(let [literal# '~this]
      (check (= literal# ~input))
      literal#)

   [ISeq]
   `(do (check (seq? ~input))
      ~(seq->view this))

   [IPersistentVector]
   `(do (check (vector? ~input))
      (let-input (seq ~input) ~(seq->view (seq this))))

   [IPersistentMap]
   `(do (check (map? ~input))
      ~(for-map [[key pattern] this]
                key
                `(let-input (get ~input ~key) ~(view pattern))))

   [Any]
   input

   [Is]
   `(do (check (~f ~input))
      input)

   [Guard]
   `(let [output# ~(view pattern)]
      (check ~code)
      output#)

   [Name]
   `(let [output# ~(view pattern)]
      (set! ~name output#)
      output#)

   [Output]
   `(do ~(view pattern)
      (trap-failure ~code))

   [Or]
   (or->view patterns)

   [And]
   (do (assert (not (empty? patterns)) "AND patterns must not be empty")
     `(do ~@(for [pattern patterns]
               (view pattern))))

   [WithMeta]
   `(try-with-meta ~(view pattern)
                   (let-input (meta ~input) ~(view meta-pattern)))))

;; WRAPPER

(defn with-locals [bound code]
  `(let-mutable [~@(interleave bound (repeat nil))]
     ~code))

(defn view-with-locals [pattern]
  (let [[pattern bound] (pattern/with-bound pattern)]
    (with-locals bound (view pattern))))

;; BENCHMARKS

(comment
  (use 'criterium.core)

  (def pat (range 10000 10100))
  (def test (range 10000 10100))
  (def test2 (range 10000 10100))

  (def pat (Output. (list 1 (Name. 'x 2) 3) 'x))
  (def test (list 1 2 3))
  (def test2 (list 1 2 3))

  (def direct (eval `(fn [~input] ~(view-direct pat))))

  (= test test2)
  (direct test)
  )
