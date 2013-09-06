(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [plumbing.core :refer [aconcat]]
            [strucjure.util :refer [with-syms assert fnk->pos-fn fnk->args extend-protocol-by-fn]]
            [strucjure.core :as core]
            [strucjure.pattern :as pattern])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]
           [strucjure.pattern Any Is Rest Guard Output Name ZeroOrMore WithMeta Or And Seqable]))

;; TODO only allowed remaining inside Rest?
;; TODO catch exceptions from output and guards etc
;; TODO optimise output
;; TODO optimise checks and sets

(defprotocol View
  (view* [this meta input output? remaining?]
    "A clj form which either a) throws failure b) returns output.
     If output? is false, its output value will be ignored.
     At the start of the form, &remaining is nil.
     If remaining? is true, on success &remaining should be set if the pattern leaves any remaining.
     If remaining? is false, the form should fail if the pattern leaves any remaining and leave the value of &remaining unchanged."))

(defn view [pattern input output? remaining?]
  (prn 'view pattern)
  (view* pattern (meta pattern) input output? remaining?))

(defn cache-input [f pattern input output? remaining?]
  (with-syms [cached-input]
    `(let [~cached-input ~input]
       ~(f pattern cached-input output? remaining?))))

;; --- MUTABLE ---

(defmacro new-mutable! []
  `(new proteus.Containers$O nil))

(defmacro get-mutable! [sym]
  `(.x ~sym))

(defmacro set-mutable! [sym value]
  `(.set ~sym ~value))

(defmacro get-remaining! []
  `(get-mutable! ~'&remaining))

(defmacro set-remaining! [value]
  `(set-mutable! ~'&remaining ~value))

(defmacro swap-remaining! [value]
  `(let [remaining# (get-remaining!)]
     (set-remaining! ~value)
     remaining#))

(defmacro check-remaining! [remaining? value output]
  (if remaining?
    `(do (set-remaining! ~value) ~output)
    `(check (nil? ~value) ~output)))

(defmacro call-fnk [fnk]
  `(~(fnk->pos-fn fnk)
    ~@(for [arg (fnk->args fnk)] `(get-mutable! ~arg))))

;; --- FAILURE ---

(def failure
  (Exception. "Match failed"))

(defmacro fail []
  `(throw failure))

(defmacro failure? [exc]
  `(identical? ~failure ~exc))

(defmacro on-fail [t f]
  `(try ~t
        (catch Exception exc#
          (if (failure? exc#)
            ~f
            (throw exc#)))))

(defmacro trap-fail [body]
  `(try ~body
        (catch Exception exc#
          (if (failure? exc#)
            (throw (Exception. (str exc#)))
            (throw exc#)))))

(defmacro check [cond body]
  `(if ~cond ~body (fail)))

;; --- UTIL ---

(defn head->view [pattern input output? remaining?]
  `(check ~input ~(cache-input view pattern `(first ~input) output? remaining?)))

(defn seq->view [patterns input output? remaining?]
  (println 'seq patterns)
  (if-let [[pattern & patterns] (seq patterns)]
    (if (instance? Rest pattern)
      `(concat ~(view (:pattern pattern) input output? true)
               ~(cache-input seq->view patterns `(swap-remaining! nil) output? remaining?))
      `(cons ~(head->view pattern input output? false)
             ~(cache-input seq->view patterns `(next ~input) output? remaining?)))
    `(check-remaining! ~remaining? ~input ~nil)))

(defn map->view [key->pattern input output? remaining?]
  `(assoc ~input
     ~@(aconcat (for [[key pattern] key->pattern]
                  [key (view pattern `(get ~input ~key) output? false)]))))

(defn or->view [patterns input output? remaining?]
  (if-let [[pattern & patterns] (seq patterns)]
    (if patterns
      `(on-fail ~(view pattern input output? remaining?)
                (do (set-remaining! nil)
                    ~(or->view patterns input output? remaining?)))
      (view pattern input output? remaining?))
    (assert nil "'Or' patterns may not be empty")))

;; --- VALUE PATTERNS ---

(extend-protocol-by-fn
 View
 (fn view* [this {:keys [used-above]} input output? remaining?]

   [nil]
   `(check (nil? ~input) nil)

   [Object]
   `(check (= ~input '~this) ~input)

   [Rest]
   (assert nil "Cannot compile Rest outside of a parsing context:" this)

   [ISeq]
   (do (println 'this this)
       `(check (seq? ~input)
               ~(seq->view this input output? remaining?)))

   [IPersistentVector]
   `(check (vector? ~input)
           ~(cache-input seq->view this `(seq ~input) output? remaining?))

   [Seqable]
   `(check (instance? clojure.lang.ISeqable ~input)
           ~(cache-input seq->view (:patterns this) `(seq ~input) output? remaining?))

   [IPersistentMap]
   `(check (map? ~input)
           ~(map->view this output? remaining?))))

;; --- LOGIC PATTERNS ---

(extend-protocol-by-fn
 View
 (fn view* [{:keys [pattern patterns meta-pattern fn fnk symbol]}
           {:keys [used-above]} input output? remaining?]

   [Any]
   input

   [Is]
   `(check (~fn ~input)
           ~(view pattern input output? remaining?))

   [Guard]
   `(let [output# ~(view pattern input output? remaining?)]
      (check (call-fnk ~fnk) output#))

   [Output]
   `(do ~(view pattern input false remaining?)
        (call-fnk ~fnk))

   [Name]
   (with-syms [output]
     `(let [~output ~(view pattern input (or output? (used-above symbol)) remaining?)]
        ~(when (used-above symbol) `(set-mutable! ~symbol ~output))
        output#))

   [ZeroOrMore]
   (with-syms [loop-input loop-output result]
     (let [body (if (instance? Rest pattern)
                  (view (:pattern pattern) loop-input output? true)
                  (head->view pattern loop-input output? false))
           recur (if (instance? Rest pattern)
                   `(recur (swap-remaining! nil) (apply conj ~loop-output ~result))
                   `(recur (rest ~loop-input) (conj ~loop-output ~result)))
           set-remaining (cond
                          remaining? `(set-remaining! ~loop-input)
                          (instance? Rest pattern) `(set-remaining! nil))]
       `(loop [~loop-input ~input
               ~loop-output []]
          (let [~result (on-fail ~body failure)]
            (if (failure? ~result)
              (do ~set-remaining
                  (seq ~loop-output))
              ~recur)))))

   [WithMeta]
   `(try-with-meta ~(view pattern input output? remaining?)
                   ~(cache-input view meta-pattern `(meta input) output? false))

   [Or]
   (or->view patterns input output? remaining?)

   [And]
   (do ~@(interleave (map #(view % input false remaining?) (butlast patterns))
                     (repeat (when remaining? `(set-remaining! false))))
       ~(view (last patterns) input output? remaining?))))

(defn pattern->view [pattern output? remaining?]
  (let [[pattern bound-here] (pattern/with-scope pattern #{})]
    (with-syms [input]
      `(fn [~input]
         (let [~@(interleave (cons '&remaining bound-here) (repeat `(new-mutable!)))]
           [~(view pattern input output? remaining?) (get-remaining!)])))))

(comment
  ((eval (pattern->view 1 true true)) 1)
  ((eval (pattern->view [] true true)) [1])
  ((eval (pattern->view [] true false)) [1])
  ((eval (pattern->view [1 2] true false)) [1])
  ((eval (pattern->view [1 2] true false)) [1 2])
  ((eval (pattern->view [1 2] true false)) [1 2 3])
  ((eval (pattern->view [1 2] true true)) [1 2 3])
  ((eval (pattern->view [1 2 (strucjure.pattern/->Rest (list 3 4))] true true)) [1 2 3 4])
  ((eval (pattern->view [1 2 (strucjure.pattern/->Rest (list 3 4))] true true)) [1 2 3 4 5])
  )
