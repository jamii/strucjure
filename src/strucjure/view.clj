(ns strucjure.view
  (:require [clojure.walk :refer [prewalk postwalk-replace]]
            [plumbing.core :refer [aconcat for-map]]
            [strucjure.util :refer [extend-protocol-by-fn try-with-meta]]
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

;; FAILURE

(def failure
  (Failure. "<internal>" nil))

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

;; REMAINING

(def remaining
  (gensym "remaining"))

(defmacro get-remaining []
  `(.x ~remaining))

(defmacro set-remaining [value]
  `(set! (.x ~remaining) ~value))

(defmacro check-remaining [body]
  `(let [output# ~body]
     (check (nil? (get-remaining)))
     output#))

(defmacro clear-remaining [body]
  `(do (set-remaining nil)
     ~body))

;; WRAPPER

(defn view-with-locals [pattern info]
  (let [[pattern bound] (pattern/with-bound pattern)]
    `(let [~@(interleave bound (repeat `(proteus.Containers$O. nil)))]
       ~(view pattern info))))

(defn view-top [pattern]
  `(let [~remaining (proteus.Containers$O. nil)]
     (check-remaining ~(view-with-locals pattern {}))))

;; UTILS

(defn rest? [pattern]
  (or (instance? Rest pattern)
      (and (instance? Name pattern)
           (rest? (:pattern pattern)))))

(defn seqable? [input]
  (or (nil? input) (instance? clojure.lang.Seqable input)))

(defn view-first [pattern info]
  `(do (check (not (nil? ~input)))
     (let-input (first ~input) (check-remaining ~(view pattern info)))))

(defn let-bound [bound code]
  `(let [~@(aconcat
            (for [name bound]
              [name `(.x ~name)]))]
     ~code))

;; STRUCTURAL PATTERNS

(defn seq->view [pattern info]
  (if-let [[first-pattern & next-pattern] pattern]
    (if (rest? first-pattern)
      `(concat
        ~(view first-pattern info)
        (let-input (get-remaining) (clear-remaining ~(seq->view next-pattern info))))
      `(cons
        ~(view-first first-pattern info)
        (let-input (next ~input) ~(seq->view next-pattern info))))
    `(do (set-remaining ~input) nil)))

(defn or->view [patterns info]
  (assert (not (empty? patterns)) "OR patterns must not be empty")
  (let [[first-pattern & next-pattern] patterns]
    (if next-pattern
      `(on-fail ~(view first-pattern info)
                (clear-remaining ~(or->view next-pattern info)))
      (view first-pattern info))))

(extend-protocol-by-fn
 View
 (fn view [this info]
   [nil Object]
   `(let [literal# '~this]
      (check (= literal# ~input))
      literal#)

   [ISeq IPersistentVector]
   `(do (check (seqable? ~input))
      (let-input (seq ~input) ~(seq->view (seq this) info)))

   [IPersistentMap]
   `(do (check (map? ~input))
      ~(for-map [[key pattern] this]
                key
                `(let-input (get ~input ~key) (check-remaining ~(view pattern info)))))))

;; LOGICAL PATTERNS

(extend-protocol-by-fn
 View
 (fn view [{:keys [pattern patterns meta-pattern name code f min-count max-count refers] :as this}
           {:keys [name->view] :as info}]
   [Any]
   input

   [Is]
   `(do (check (~f ~input))
      ~input)

   [Guard]
   `(let [output# ~(view pattern info)]
      (check ~(let-bound (:bound-here (meta this)) code))
      output#)

   [Name]
   `(let [output# ~(view pattern info)]
      (.set ~name output#)
      output#)

   [Output]
   `(do ~(view pattern info)
      (trap-failure ~(let-bound (:bound-here (meta this)) code)))

   [Or]
   (or->view patterns info)

   [And]
   (do (assert (not (empty? patterns)) "AND patterns must not be empty")
     `(do ~@(for [pattern patterns]
              `(clear-remaining ~(view pattern info)))))

   [WithMeta]
   `(try-with-meta ~(view pattern info)
                   (let-input (meta ~input) (check-remaining ~(view meta-pattern info))))

   [Refer]
   `(~(name->view name) ~input ~remaining)

   [Let]
   (let [name->view (merge name->view
                           (for-map [[name pattern] refers]
                                    name (gensym name)))
         info (assoc info :name->view name->view)]
     `(letfn [~@(for [[name pattern] refers]
                  `(~(name->view name)
                     [~input ~remaining]
                     ~(view-with-locals pattern info)))] ;; refers is not walked by pattern/with-bound, so it is scoped separately
        ~(view pattern info)))

   [Rest]
   (view pattern (assoc info :remaining? true))

   [Repeated]
   `(do (check (seqable? ~input))
        (loop [~input (seq ~input)
               loop-output# []
               loop-count# 0]
          (let [result# (on-fail (do (check (< loop-count# ~max-count))
                                   ~(if (rest? pattern)
                                      (view pattern info)
                                      (view-first pattern info)))
                                 failure)]
            (if (identical? failure result#)
              (do (check (>= loop-count# ~min-count))
                (set-remaining ~input)
                (seq loop-output#))
              (recur
               ~(if (rest? pattern) `(let [remaining# (get-remaining)] (set-remaining nil) remaining#) `(next ~input))
               (~(if (rest? pattern) 'into 'conj) loop-output# result#)
               (unchecked-inc loop-count#))))))))