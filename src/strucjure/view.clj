(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [clojure.walk :refer [prewalk postwalk-replace]]
            [plumbing.core :refer [aconcat for-map]]
            [strucjure.util :refer [with-syms assert fnk->pos-fn fnk->args extend-protocol-by-fn try-with-meta]]
            [strucjure.pattern :as pattern])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]
           [strucjure.pattern Any Is Rest Guard Name Repeated WithMeta Or And Seqable Refer Where Output]
           [strucjure.view Failure]))

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

;; SEMANTICS

(defprotocol View
  (view [this]))

(def input (gensym "input"))

(defn seq->view [pattern]
  (if-let [[first-pattern & next-pattern] pattern]
    `(cons (::view ~view ~first-pattern (first ~input))
       (::view ~seq->view ~next-pattern (next ~input)))
    `(check (nil? ~input))))

(extend-protocol-by-fn
 View
 (fn view [this]
   [Object]
   `(do (check (= '~this ~input))
      '~this)

   [clojure.lang.ISeq]
   `(do (check (seq? ~input))
      (::view ~seq->view ~this ~input))))

(defn or->view [patterns]
  (assert (not (empty? patterns)) "OR patterns must not be empty")
  (let [[first-pattern & next-pattern] patterns]
    (if next-pattern
      `(on-fail (::view ~view ~first-pattern ~input)
                (::view ~or->view ~next-pattern ~input))
      `(::view ~view ~first-pattern ~input))))

(extend-protocol-by-fn
 View
 (fn view [{:keys [pattern patterns name code] :as this}]
   [Name]
   `(let [output# (::view ~view ~pattern ~input)]
      (.set ~name output#)
      output#)

   [Output]
   `(do (::view ~view ~pattern ~input)
      (trap-failure
       ~(postwalk-replace
         (for-map [name (:bound-here (meta this))]
                  name `(.x ~name)) ;; TODO this is crude - use the walk from proteus instead
         code)))

   [Or]
   `(::view ~or->view ~patterns ~input)
   ))

;; COMPILERS

(defn with-locals [bound code]
  `(let [~@(aconcat
            (for [name bound]
              [name `(new proteus.Containers$O nil)]))]
     ~code))

(defn rewrite [code keyword f]
  (prewalk
   (fn [code]
     (if (and (seq? code) (= keyword (first code)))
       (apply f (rest code))
       code))
   code))

(defn view-direct [pattern]
  (let [[pattern bound] (pattern/with-bound pattern)]
    (with-locals bound
      (rewrite (view pattern) ::view
               (fn [sub-view sub-pattern sub-input]
                 `(let [~input ~sub-input]
                    ~(sub-view sub-pattern)))))))

(declare walk-fn)

(defn new-fn [fns code]
  (let [name (gensym "foo")]
    (swap! fns conj `(~name [~input] ~(walk-fn fns code)))
    name))

(defn walk-fn [fns code]
  (rewrite code ::view
           (fn [sub-view sub-pattern sub-input]
             `(::call ~(new-fn fns (sub-view sub-pattern)) ~sub-input))))

(defn view-indirect [pattern]
  (let [[pattern bound] (pattern/with-bound pattern)
        fns (atom [])
        top (new-fn fns (view pattern))]
    (rewrite `(fn [~input]
                ~(with-locals bound
                   `(letfn [~@@fns] (~top ~input))))
             ::call
             (fn [f arg]
               `(~f ~arg)))))

(defn view-reified [pattern]
  (let [[pattern bound] (pattern/with-bound pattern)
        fns (atom [])
        top (new-fn fns (view pattern))
        interface (gensym "IMatch")]
    (eval `(definterface ~interface ~@(for [[name args & _] @fns] `(~name [~@args]))))
    (rewrite (with-locals bound
               `(let [r# (reify ~interface
                           ~@(for [[name args & body] @fns]
                               `(~name [~'this ~@args] ~@body)))]
                  (~(symbol (str "." top)) r# ~input)))
             ::call
             (fn [f arg]
               `(~(symbol (str "." f)) ~'this ~arg)))))

;; BENCHMARKS

(comment
  (use 'criterium.core)
  (def pat (concat (range 10000 10050) [(Name. 'x 10050)] (range 10051 10100)))
  (def test (range 10000 10100))

  (def direct (eval `(fn [~input] ~(view-direct pat))))
  (def indirect (eval (view-indirect pat)))
  (def reified (eval (view-reified pat)))

  (= pat test) ;; 5.797365 µs
  (direct test) ;; 34.865619 µs
  (indirect test) ;; 12.458736 µs
  (reified test) ;;  6.413043 µs

  (def named (Output. (list 1 (Name. 'x 2) 3) 'x))
  ((eval `(fn [~input] ~(view-direct named))) (list 1 2 3))
  ((eval (view-indirect named)) (list 1 2 3))
  ((eval (view-reified named)) (list 1 2 3))
  (view (list 1 2))
  (seq->view (list 1 2))
  )
