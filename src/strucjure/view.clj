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

(defn rest? [pattern]
  (or (instance? Rest pattern)
      (and (instance? Name pattern)
           (rest? (:pattern pattern)))))

;; WRAPPER

(defn view-with-locals [pattern info]
  (let [[pattern bound] (pattern/with-bound pattern)]
    `(let-mutable [~@(interleave bound (repeat nil))]
                  ~(view pattern info))))

(defn view-top [pattern]
  `(let [~remaining (proteus.Containers$O. nil)]
     ~(view-with-locals pattern {})))

;; STRUCTURAL PATTERNS

(defn seq->view [pattern info]
  (if-let [[first-pattern & next-pattern] pattern]
    (if (rest? first-pattern)
      `(concat
        ~(view first-pattern info)
        (let-input (get-remaining)
                   (do (set-remaining nil)
                     ~(seq->view next-pattern info))))
      `(cons
        (let-input (first ~input) (check-remaining ~(view first-pattern info)))
        (let-input (next ~input) ~(seq->view next-pattern info))))
    `(do (set-remaining ~input)
       nil)))

(defn or->view [patterns info]
  (assert (not (empty? patterns)) "OR patterns must not be empty")
  (let [[first-pattern & next-pattern] patterns]
    (if next-pattern
      `(on-fail ~(view first-pattern info)
                (do (set-remaining nil)
                  ~(or->view next-pattern info)))
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
                `(let-input (get ~input ~key) (check-remaining ~(view pattern info)))))))

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
              `(do (set-remaining nil)
                ~(view pattern info)))))

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
   (view pattern (assoc info :remaining? true))))