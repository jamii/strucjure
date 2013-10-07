(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [plumbing.core :refer [aconcat for-map]]
            [strucjure.util :refer [with-syms assert fnk->pos-fn fnk->args extend-protocol-by-fn try-with-meta]]
            [strucjure.pattern :as pattern]
            [strucjure.graph :as graph])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]
           [strucjure.pattern Any Is Rest Guard Name Repeated WithMeta Or And Seqable Node NodeOf]
           [strucjure.view Failure]))

;; TODO go back to single failure and identical? once done debugging
;; TODO when input is unchanged just return input, no need to allocate

(defprotocol View
  (view [this subview info]
    "Returns a view-fn.
     Call subview rather than recursing directly."))

;; --- FAILURE ---

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

(defn quote-except [exceptions form]
  (clojure.walk/postwalk
   (fn [form]
     (cond
      (contains? exceptions form) form
      (seq? form) `(list ~@form)
      (coll? form) form
      true `(quote ~form)))
   form))

(defmacro check [pred]
  `(if-not ~pred (throw (Failure. (pr-str ~(quote-except &env pred))))))

;; --- REMAINING ---

(defmacro new-remaining! []
  `(new proteus.Containers$O nil))

(defmacro get-remaining! [remaining]
  `(.x ~remaining))

(defmacro set-remaining! [remaining value]
  `(if (nil? ~remaining)
     (check (nil? ~value))
     (.set ~remaining ~value)))

(defn fnk->call [fnk arg->index]
  (let [pure-call (with-syms [env]
                    `(fn [pos-fn#]
                       (fn [~env]
                         (pos-fn#
                          ~@(for [arg (fnk->args fnk)]
                              `(aget ~env ~(arg->index arg)))))))]
    ((eval pure-call) (fnk->pos-fn fnk))))

;; --- VALUE PATTERNS ---

(defn head->view [this subview info]
  (if (instance? Rest this)
    (subview (:pattern this) info)
    (let [head-view (subview this info)]
      (fn [input remaining env]
        (check input)
        (set-remaining! remaining (next input))
        (list (head-view (first input) nil env))))))

(defn seq->view [this subview info]
  (if-let [[head & tail] this]
    (let [head-view (head->view head subview info)
          tail-view (seq->view tail subview info)]
      (fn [input remaining env]
        (let [side-channel (new-remaining!)]
          (concat
           (head-view input side-channel env)
           (tail-view (get-remaining! side-channel) remaining env)))))
    (fn [input remaining env]
      (set-remaining! remaining input)
      nil)))

(defn map->view [this subview info]
  (if-let [[[key value] & tail] (seq this)]
    (let [value-view (subview value info)
          tail-view (map->view tail subview info)]
      (fn [input remaining env]
        (assoc (tail-view input remaining env)
          key (value-view (get input key) nil env))))
    (fn [input remaining env]
      {})))

(extend-protocol-by-fn
 View
 (fn view [this subview info]
   [nil]
   (fn [input remaining env]
     (check (nil? input))
     input)

   [Object]
   (fn [input remaining env]
     (check (= this input))
     input)

   [ISeq]
   (let [seq-view (seq->view this subview info)]
     (fn [input remaining env]
       (check (seq? input))
       (seq-view (seq input) remaining env)))

   [IPersistentVector]
   (let [seq-view (seq->view this subview info)]
     (fn [input remaining env]
       (check (vector? input))
       (vec (seq-view (seq input) remaining env))))

   [Seqable]
   (let [seq-view (seq->view this subview info)]
     (fn [input remaining env]
       (check (or (nil? input) (instance? clojure.lang.Seqable input)))
       (seq-view (seq input) remaining env)))

   [IPersistentMap]
   (let [map-view (map->view this subview info)]
     (fn [input remaining env]
       (check (map? input))
       (map-view input remaining env)))

   [Rest]
   (assert nil "Cannot compile Rest outside of a parsing context:" this)))

;; --- LOGICAL PATTERNS ---

(defn or->view [this subview info]
  (let [[head & tail] (seq this)]
    (if tail
      (let [head-view (subview head info)
            tail-view (or->view tail subview info)]
        (fn [input remaining env]
          (prn head tail)
          (let [old-remaining (when remaining (get-remaining! remaining))]
            (on-fail (head-view input remaining env)
                     (do (set-remaining! remaining old-remaining)
                         (tail-view input remaining env))))))
      (subview head info))))

(defn and->view [this subview info]
  (let [[head & tail] (seq this)]
    (if tail
      (let [head-view (subview head info)
            tail-view (and->view tail subview info)]
        (fn [input remaining env]
          (do (head-view input remaining env)
              (tail-view input remaining env))))
      (subview head info))))

(defprotocol IPointer
  (get-val [this])
  (set-val [this new-val]))

(deftype NodePointer [^:volatile-mutable f] ;; only set by constructing function
  clojure.lang.IFn
  (invoke [this input remaining env]
    (f input remaining env))
  IPointer
  (get-val [this]
    f)
  (set-val [this new-val]
    (set! f new-val)))

(extend-protocol-by-fn
 View
 (fn view [{:keys [pattern patterns meta-pattern f fnk name graph min-count max-count]}
          subview {:keys [name->pos name->node] :as info}]
   [Any]
   (fn [input remaining env]
     input)

   [Is]
   (fn [input remaining env]
     (check (trap-failure (f input)))
     input)

   [Guard]
   (let [inner-view (subview pattern info)
         fnk-call (fnk->call fnk name->pos)]
     (fn [input remaining env]
       (let [output (inner-view input remaining env)]
         (check (trap-failure (fnk-call env)))
         output)))

   [Name]
   (let [inner-view (subview pattern info)
         pos (name->pos name)]
     (assert pos name "does not exist in" name->pos)
     (fn [input remaining env]
       (let [output (inner-view input remaining env)]
         (aset env pos output)
         output)))

   [Repeated]
   (let [inner-view (head->view pattern subview info)
         failure (Failure. "Loop failure")]
     (fn [input remaining env]
       (check (or (nil? input) (instance? clojure.lang.Seqable input)))
       (let [side-channel (new-remaining!)]
         (loop [loop-input (seq input)
                loop-output []
                loop-count 0]
           (let [result (on-fail (do (check (< loop-count max-count))
                                     (inner-view loop-input side-channel env))
                                 failure)]
             (if (identical? failure result)
               (do (check (>= loop-count min-count))
                   (set-remaining! remaining loop-input)
                   (seq loop-output))
               (recur (get-remaining! side-channel)
                      (into loop-output result)
                      (inc loop-count))))))))

   [WithMeta]
   (let [pattern-view (subview pattern info)
         meta-view (subview meta-pattern info)]
     (fn [input remaining env]
       (try-with-meta (pattern-view input remaining env)
                      (meta-view input nil env))))

   [Or]
   (or->view patterns subview info)

   [And]
   (and->view patterns subview info)

   [Node]
   (name->node name)

   [NodeOf]
   (let [name->pointer (for-map [[name pattern] graph]
                                name (NodePointer. nil))
         info (update-in info [:name->node] merge name->pointer)
         name->node (for-map [[name pattern] graph]
                             name (subview pattern info))]
     (doseq [[name pattern] graph]
       (set-val (name->pointer name) (name->node name)))
     (name->node name))))

;; --- LAYERS ---

(deftype ViewPointer [^:volatile-mutable f] ;; only set by constructing function
  clojure.lang.IFn
  (invoke [this pattern info]
    (f pattern info))
  IPointer
  (get-val [this]
    f)
  (set-val [this new-val]
    (set! f new-val)))

(defn info [pattern]
  (let [[pattern bound] (pattern/with-bound pattern)
        pattern (pattern/with-used pattern #{})]
    (prn 'b bound)
    (pattern/check-used-not-bound pattern)
    {:name->pos (zipmap bound (range))}))

(defn view-with [layers pattern]
  (let [subview (ViewPointer. nil)]
    (set-val subview #(view %1 subview %2))
    (doseq [layer layers]
      (let [restview (get-val subview)]
        (set-val subview #(layer %1 subview restview %2))))
    (let [info (info pattern)
          superview (subview pattern info)
          env-length (count (:name->pos info))]
      (fn ([input] (superview input nil (object-array env-length)))
         ([input remaining] (superview input remaining (object-array env-length)))))))

(defn with-output [name->fnk]
  (fn [pattern subview restview info]
    (if (and (instance? Name pattern) (contains? name->fnk (:name pattern)))
      (let [fnk-call (fnk->call (name->fnk (:name pattern)) (:name->pos info))
            inner-view (restview pattern info)]
        (fn [input remaining env]
          (inner-view input remaining env)
          (fnk-call env)))
      (restview pattern info))))
