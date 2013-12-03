(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [clojure.walk :refer [prewalk postwalk-replace]]
            [plumbing.core :refer [aconcat for-map]]
            [strucjure.util :refer [with-syms assert fnk->pos-fn fnk->args extend-protocol-by-fn try-with-meta]]
            [strucjure.pattern :as pattern]
            [proteus :refer [let-mutable]])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]
           [strucjure.pattern Any Is Rest Guard Name Repeated WithMeta Or And Refer Let Output]
           [strucjure.view Failure]))

;; TODO
;; parsing Rest Repeated seq->view
;; recursive Refer Let

;; INTERFACE

(defprotocol View
  (view [this info]))

(def input (gensym "input"))

(defmacro let-input [value body]
  `(let [~input ~value] ~body))

;; WRAPPER

(defn with-locals [bound code]
  `(let-mutable [~@(interleave bound (repeat nil))]
     ~code))

(defn view-with-locals [pattern info]
  (let [[pattern bound] (pattern/with-bound pattern)]
    (with-locals bound (view pattern info))))

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

(defn seq->view [pattern info]
  (if-let [[first-pattern & next-pattern] pattern]
    `(cons
      (let-input (first ~input) ~(view first-pattern info))
      (let-input (next ~input) ~(seq->view next-pattern info)))
    `(check (nil? ~input))))

(defn or->view [patterns info]
  (assert (not (empty? patterns)) "OR patterns must not be empty")
  (let [[first-pattern & next-pattern] patterns]
    (if next-pattern
      `(on-fail ~(view first-pattern info)
                ~(or->view next-pattern info))
      (view first-pattern info))))

(extend-protocol-by-fn
 View
 (fn view [this info]
   [nil Object]
   `(let [literal# '~this]
      (check (= literal# ~input))
      literal#)

   [ISeq IPersistentVector]
   `(do (check (instance? clojure.lang.Seqable ~input))
      (let-input (seq ~input) ~(seq->view (seq this) info)))

   [IPersistentMap]
   `(do (check (map? ~input))
      ~(for-map [[key pattern] this]
                key
                `(let-input (get ~input ~key) ~(view pattern info))))))

(extend-protocol-by-fn
 View
 (fn view [{:keys [pattern patterns meta-pattern name code f min-count max-count refers] :as this}
           {:keys [name->view] :as info}]
   [Any]
   input

   [Is]
   `(do (check (~f ~input))
      input)

   [Guard]
   `(let [output# ~(view pattern info)]
      (check ~code)
      output#)

   [Name]
   `(let [output# ~(view pattern info)]
      (set! ~name output#)
      output#)

   [Output]
   `(do ~(view pattern info)
      (trap-failure ~code))

   [Or]
   (or->view patterns info)

   [And]
   (do (assert (not (empty? patterns)) "AND patterns must not be empty")
     `(do ~@(for [pattern patterns]
               (view pattern info))))

   [WithMeta]
   `(try-with-meta ~(view pattern info)
                   (let-input (meta ~input) ~(view meta-pattern info)))

   [Refer]
   `(~(name->view name) ~input)

   [Let]
   (let [name->view (merge name->view
                           (for-map [[name pattern] refers]
                                    name (gensym name)))
         info (assoc info :name->view name->view)]
     `(letfn [~@(for [[name pattern] refers]
                  `(~(name->view name)
                     [~input]
                     ~(view-with-locals pattern info)))] ;; refers is not walked by pattern/with-bound, so it is scoped separately
        ~(view pattern info)))))