(ns strucjure.view
  (:require [clojure.walk :refer [prewalk postwalk-replace]]
            [plumbing.core :refer [aconcat for-map]]
            [strucjure.util :refer [extend-protocol-by-fn try-with-meta]]
            [strucjure.pattern :as pattern]
            [proteus :refer [let-mutable]])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]
           [strucjure.pattern Any Is Rest Guard Name Repeated WithMeta Or And Refer Let Output Trace]
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

(def last-failure
  (gensym "last-failure"))

(defmacro on-fail [t f]
  `(try ~t
        (catch Failure exc#
          (.set ~last-failure exc#)
          ~f)))

(defmacro trap-failure [body]
  `(try ~body
        (catch Exception exc#
          (if (instance? Failure exc#)
            (throw (Exception. (str exc#)))
            (throw exc#)))))

(defmacro check [pred pattern]
  `(when-not ~pred
     (throw (Failure. ~(pr-str pred) ~(pr-str pattern) ~input (.x ~last-failure)))))

;; REMAINING

;; Invariants:
;; remaining is always nil at start of pattern
;; if remaining? is false then the pattern may not set remaining

(def remaining
  (gensym "remaining"))

(defmacro get-remaining []
  `(.x ~remaining))

(defmacro set-remaining [value]
  `(.set ~remaining ~value))

(defmacro pass-remaining [remaining? value pattern]
  (cond
   remaining? `(set-remaining ~value)
   value `(check (nil? ~value) ~pattern)))

(defmacro clear-remaining [remaining? body]
  (if remaining?
    `(do (set-remaining nil)
       ~body)
    body))

;; WRAPPER

(defn view-with-locals [pattern info]
  (let [[pattern bound] (pattern/with-bound pattern)]
    `(let [~@(interleave bound (repeat `(proteus.Containers$O. nil)))]
       ~(view pattern info))))

(defn view-top [pattern]
  `(let [~last-failure (proteus.Containers$O. nil)
         ~remaining (proteus.Containers$O. nil)]
     ~(view-with-locals pattern {:name->view {} :output? true :remaining? false})))

;; UTILS

(defn rest? [pattern]
  (or (instance? Rest pattern)
      (and (instance? Name pattern)
           (rest? (:pattern pattern)))))

(defn seqable? [input]
  (or (nil? input) (instance? clojure.lang.Seqable input)))

(defn view-first [pattern info]
  `(do (check (not (nil? ~input)) ~pattern)
     (let-input (first ~input) ~(view pattern (assoc info :remaining? false)))))

(defn let-bound [bound code]
  `(let [~@(aconcat
            (for [name bound]
              [name `(.x ~name)]))]
     ~code))

(def depth
  (gensym "depth"))

;; STRUCTURAL PATTERNS

(defn seq->view [pattern {:keys [output? remaining?] :as info}]
  (if-let [[first-pattern & next-pattern] pattern]
    (if (rest? first-pattern)
      `(~(if output? 'concat 'do)
        ~(view first-pattern info)
        (let-input (get-remaining) (clear-remaining true ~(seq->view next-pattern info))))
      `(~(if output? 'cons 'do)
        ~(view-first first-pattern info)
        (let-input (next ~input) ~(seq->view next-pattern info))))
    `(do (pass-remaining ~remaining? ~input ~pattern) nil)))

(defn or->view [patterns {:keys [remaining?] :as info}]
  (assert (not (empty? patterns)) "OR patterns must not be empty")
  ;;(let [bindings (for [pattern patterns] (let [[_ bound] (pattern/with-bound pattern)] bound))]
  ;;  (assert (every? #(= (first bindings) %) bindings) "All branches of an Or pattern must have the same bound variables"))
  (let [[first-pattern & next-pattern] patterns]
    (if next-pattern
      `(on-fail ~(view first-pattern info)
                (clear-remaining remaining? ~(or->view next-pattern info)))
      (view first-pattern info))))

(extend-protocol-by-fn
 View
 (fn view [this {:keys [output?] :as info}]
   [nil Object]
   `(let [literal# '~this]
      (check (= literal# ~input) ~this)
      literal#)

   [ISeq IPersistentVector]
   `(do (check (seqable? ~input) ~this)
      (let-input (seq ~input) ~(seq->view (seq this) info)))

   [IPersistentMap]
   `(do (check (map? ~input) ~this)
      ~(let [map (for-map [[key pattern] this]
                          key
                          `(let-input (get ~input ~key) ~(view pattern (assoc info :remaining? false))))]
         (if output? map `(do ~@(vals map)))))))

;; LOGICAL PATTERNS

(extend-protocol-by-fn
 View
 (fn view [{:keys [pattern patterns meta-pattern name code f min-count max-count refers] :as this}
           {:keys [name->view output? remaining?] :as info}]
   [Any]
   input

   [Is]
   `(do (check (~f ~input) ~this)
      ~input)

   [Guard]
   `(let [output# ~(view pattern info)]
      (check ~(let-bound (:bound-here (meta this)) code) ~this)
      output#)

   [Name]
   `(let [output# ~(view pattern (assoc info :output? true))]
      (.set ~name output#)
      output#)

   [Output]
   `(do ~(view pattern (assoc info :output? false))
      (trap-failure ~(let-bound (:bound-here (meta this)) code)))

   [Or]
   (or->view patterns info)

   [And]
   (do (assert (not (empty? patterns)) "AND patterns must not be empty")
     `(do ~@(for [pattern patterns]
              `(clear-remaining remaining? ~(view pattern info)))))

   [WithMeta]
   `(~(if output? 'try-with-meta 'do)
     ~(view pattern info)

     (let-input (meta ~input) ~(view meta-pattern (assoc info :remaining? false))))

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
        ~(view pattern info)))

   [Rest]
   (view pattern (assoc info :remaining? true))

   [Repeated]
   `(do (check (seqable? ~input) ~this)
        (loop [~input (seq ~input)
               loop-output# ~(if output? [] nil)
               loop-count# 0]
          (let [result# (on-fail (do (check (< loop-count# ~max-count) this)
                                   ~(if (rest? pattern)
                                      (view pattern info)
                                      (view-first pattern info)))
                                 failure)]
            (if (identical? failure result#)
              (do (check (>= loop-count# ~min-count) ~this)
                (pass-remaining ~remaining? ~input ~this)
                (seq loop-output#))
              (recur
               ~(if (rest? pattern) `(let [remaining# (get-remaining)] (set-remaining nil) remaining#) `(next ~input))
               (~(if output? (if (rest? pattern) 'into 'conj) 'comment) loop-output# result#)
               (unchecked-inc loop-count#))))))

   [Trace]
   `(do (.set ~depth (inc (.x ~depth)))
      (println (apply str (repeat (.x ~depth) " ")) "=>" ~name ~input)
      (try
        (let [output# ~(view pattern info)]
          (println (apply str (repeat (.x ~depth) " ")) "<=" ~name output#)
          (.set ~depth (dec (.x ~depth)))
          output#)
        (catch Exception exc#
          (println (apply str (repeat (.x ~depth) " ")) "XX" ~name (pr-str exc#))
          (.set ~depth (dec (.x ~depth)))
          (throw exc#))))))