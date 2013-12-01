(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [clojure.walk :refer [prewalk]]
            [plumbing.core :refer [aconcat for-map]]
            [strucjure.util :refer [with-syms assert fnk->pos-fn fnk->args extend-protocol-by-fn try-with-meta]]
            [strucjure.pattern :as pattern]
            [strucjure.graph :as graph])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]
           [strucjure.pattern Any Is Rest Guard Name Repeated WithMeta Or And Seqable Node Edge Graph]
           [strucjure.view Failure]))

;; SEMANTICS

(defprotocol View
  (view [this]))

(def input (gensym "input"))

(defn seq->view [pattern]
  (if-let [[first-pattern & next-pattern] pattern]
    `(do (::view ~view ~first-pattern (first ~input))
         (::view ~seq->view ~next-pattern (next ~input)))
    `(assert (nil? ~input))))

(extend-protocol-by-fn
 View
 (fn view [this]
   [Object]
   `(assert (= '~this ~input))

   [clojure.lang.ISeq]
   `(do (assert (seq? ~input))
        (::view ~seq->view ~this ~input))))

(extend-protocol-by-fn
 View
 (fn view [{:keys [pattern name]}]
   [Name]
   `(.set ~name (::view ~view ~pattern ~input))


   ))

;; COMPILERS

(defn with-locals [pattern code]
  (let [[pattern bound] (pattern/with-bound pattern)
        pattern (pattern/with-used pattern #{})]
    (pattern/check-used-not-bound pattern)
    `(let [~@(aconcat
              (for [name bound]
                [name `(new proteus.Containers$O nil)]))]
       ~code)))

(defn rewrite [code keyword f]
  (prewalk
   (fn [code]
     (if (and (seq? code) (= keyword (first code)))
       (apply f (rest code))
       code))
   code))

(defn view-direct [pattern]
  (with-locals pattern
    (rewrite (view pattern) ::view
             (fn [sub-view sub-pattern sub-input]
               `(let [~input ~sub-input]
                  ~(sub-view sub-pattern))))))

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
  (let [fns (atom [])
        top (new-fn fns (view pattern))]
    (rewrite `(fn [~input]
                ~(with-locals pattern
                   `(letfn [~@@fns] (~top ~input))))
             ::call
             (fn [f arg]
               `(~f ~arg)))))

(defn view-reified [pattern]
  (let [fns (atom [])
        top (new-fn fns (view pattern))
        interface (gensym "IMatch")]
    (eval `(definterface ~interface ~@(for [[name args & _] @fns] `(~name [~@args]))))
    (rewrite `(fn [~input]
                ~(with-locals pattern
                   `(let [r# (reify ~interface ~@(for [[name args & body] @fns] `(~name [~'this ~@args] ~@body)))]
                     (~(symbol (str "." top)) r# ~input))))
             ::call
             (fn [f arg]
               `(~(symbol (str "." f)) ~'this ~arg)))))

;; BENCHMARKS

(comment
  (use 'criterium.core)
  (def pat (range 10000 10100))
  (def test (range 10000 10100))

  (def direct (eval `(fn [~input] ~(view-direct pat))))
  (def indirect (eval (view-indirect pat)))
  (def reified (eval (view-reified pat)))

  (bench (= pat test)) ;; 5.797365 µs
  (bench (direct test)) ;; 34.865619 µs
  (bench (indirect test)) ;; 12.458736 µs
  (bench (reified test)) ;;  6.413043 µs

  (view-direct (Name. 'x (list 1 2 3)))
  (view-indirect (Name. 'x (list 1 2 3)))
  (view-reified (Name. 'x (list 1 2 3)))

  (view (list 1 2))
  (seq->view (list 1 2))
)
