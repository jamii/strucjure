(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [plumbing.core :refer [aconcat for-map]]
            [strucjure.util :refer [with-syms assert fnk->pos-fn fnk->args extend-protocol-by-fn try-with-meta]]
            [strucjure.pattern :as pattern]
            [strucjure.graph :as graph])
  (:import [clojure.lang ISeq IPersistentVector IPersistentMap]
           [strucjure.pattern Any Is Rest Guard Name Repeated WithMeta Or And Seqable Node Edge Graph]
           [strucjure.view Failure]))

;; TODO go back to single failure and identical? once done debugging
;; TODO when input is unchanged just return input, no need to allocate

(defprotocol View
  (view [this info]
    "Returns a view-fn.
     Call *view* rather than recursing directly."))

(defn pattern->info [pattern]
  (let [[pattern bound] (pattern/with-bound pattern)
        pattern (pattern/with-used pattern #{})]
    (pattern/check-used-not-bound pattern)
    {:name->pos (zipmap bound (range))}))

(def ^:dynamic *view*
  (fn
    ([pattern] ;; top-level call
       (let [info (pattern->info pattern)
             sub-view (*view* pattern info)
             env-length (count (:name->pos info))]
         (fn [input]
           (sub-view input nil (object-array env-length)))))
    ([pattern info] ;; recursive call
       (view pattern info))))

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
                              (let [index (arg->index arg)]
                                (assert (integer? index) "Can't find name" arg "in" (set (keys arg->index)))
                                `(aget ~env ~index)))))))]
    ((eval pure-call) (fnk->pos-fn fnk))))

;; --- VALUE PATTERNS ---

(defn head->view [this info]
  (if (instance? Rest this)
    (*view* (:pattern this) info)
    (let [head-view (*view* this info)]
      (fn [input remaining env]
        (check input)
        (set-remaining! remaining (next input))
        (list (head-view (first input) nil env))))))

(defn seq->view [this info]
  (if-let [[head & tail] this]
    (let [head-view (head->view head info)
          tail-view (seq->view tail info)]
      (fn [input remaining env]
        (let [side-channel (new-remaining!)]
          (concat
           (head-view input side-channel env)
           (tail-view (get-remaining! side-channel) remaining env)))))
    (fn [input remaining env]
      (set-remaining! remaining input)
      nil)))

(defn map->view [this info]
  (if-let [[[key value] & tail] (seq this)]
    (let [value-view (*view* value info)
          tail-view (map->view tail info)]
      (fn [input remaining env]
        (assoc (tail-view input remaining env)
          key (value-view (get input key) nil env))))
    (fn [input remaining env]
      {})))

(extend-protocol-by-fn
 View
 (fn view [this info]
   [nil]
   (fn [input remaining env]
     (check (nil? input))
     input)

   [Object]
   (fn [input remaining env]
     (check (= this input))
     input)

   [ISeq]
   (let [seq-view (seq->view this info)]
     (fn [input remaining env]
       (check (seq? input))
       (seq-view (seq input) remaining env)))

   [IPersistentVector]
   (let [seq-view (seq->view this info)]
     (fn [input remaining env]
       (check (vector? input))
       (vec (seq-view (seq input) remaining env))))

   [Seqable]
   (let [seq-view (seq->view (:patterns this) info)]
     (fn [input remaining env]
       (check (or (nil? input) (instance? clojure.lang.Seqable input)))
       (seq-view (seq input) remaining env)))

   [IPersistentMap]
   (let [map-view (map->view this info)]
     (fn [input remaining env]
       (check (map? input))
       (map-view input remaining env)))

   [Rest]
   (assert nil "Cannot compile Rest outside of a parsing context:" this)))

;; --- LOGICAL PATTERNS ---

(defn or->view [this info]
  (let [[head & tail] (seq this)]
    (if tail
      (let [head-view (*view* head info)
            tail-view (or->view tail info)]
        (fn [input remaining env]
          (let [old-remaining (when remaining (get-remaining! remaining))]
            (on-fail (head-view input remaining env)
                     (do (set-remaining! remaining old-remaining)
                         (tail-view input remaining env))))))
      (*view* head info))))

(defn and->view [this info]
  (let [[head & tail] (seq this)]
    (if tail
      (let [head-view (*view* head info)
            tail-view (and->view tail info)]
        (fn [input remaining env]
          (do (head-view input remaining env)
              (tail-view input remaining env))))
      (*view* head info))))

(defprotocol IPointer
  (set-val [this new-val]))

(deftype NodePointer [^:volatile-mutable f] ;; only set by constructing function
  clojure.lang.IFn
  (invoke [this input remaining env]
    (f input remaining env))
  IPointer
  (set-val [this new-val]
    (set! f new-val)))

(extend-protocol-by-fn
 View
 (fn view [{:keys [pattern patterns meta-pattern f fnk name graph min-count max-count]}
          {:keys [name->pos name->node] :as info}]
   [Any]
   (fn [input remaining env]
     input)

   [Is]
   (fn [input remaining env]
     (check (trap-failure (f input)))
     input)

   [Guard]
   (let [inner-view (*view* pattern info)
         fnk-call (fnk->call fnk name->pos)]
     (fn [input remaining env]
       (let [output (inner-view input remaining env)]
         (check (trap-failure (fnk-call env)))
         output)))

   [Name]
   (let [inner-view (*view* pattern info)
         pos (name->pos name)]
     (assert pos name "does not exist in" name->pos)
     (fn [input remaining env]
       (let [output (inner-view input remaining env)]
         (aset env pos output)
         output)))

   [Repeated]
   (let [inner-view (head->view pattern info)
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
   (let [pattern-view (*view* pattern info)
         meta-view (*view* meta-pattern info)]
     (fn [input remaining env]
       (try-with-meta (pattern-view input remaining env)
                      (meta-view (meta input) nil env))))

   [Or]
   (or->view patterns info)

   [And]
   (and->view patterns info)

   [Edge]
   (name->node name)

   [Node]
   (*view* pattern info)

   [Graph]
   (let [name->pointer (for-map [[name pattern] graph]
                                name (NodePointer. nil))
         name->node (for-map [[name pattern] graph]
                             name
                             (let [info (pattern->info pattern)
                                   node-view (*view* (Node. name pattern) (assoc info :name->node name->pointer))
                                   env-length (count (:name->pos info))]
                               (fn [input remaining _]
                                 (node-view input remaining (object-array env-length)))))]
     (doseq [[name pattern] graph]
       (set-val (name->pointer name) (name->node name)))
     (name->node name))))

;; --- LAYERS ---

(defmacro with-layers [layers & body]
  (let [old-view (gensym "old-view")
        new-view (reduce (fn [mid-view layer] `(partial ~layer ~mid-view)) old-view layers)]
    `(let [~old-view *view*]
       (binding [*view* ~new-view]
         ~@body))))

;; --- OUTPUT ---

(defn with-pre-fns [class name->fnk]
  (fn
    ([old-view pattern]
       (old-view pattern))
    ([old-view pattern info]
       (if (and (instance? class pattern) (contains? name->fnk (:name pattern)))
         (let [fnk-call (fnk->call (name->fnk (:name pattern)) (:name->pos info))
               inner-view (old-view pattern info)]
           (fn [input remaining env]
             (let [output (fnk-call env)]
               (inner-view input remaining env)
               output)))
         (old-view pattern info)))))

(defn with-post-fns [class name->fnk]
  (fn
    ([old-view pattern]
       (old-view pattern))
    ([old-view pattern info]
       (if (and (instance? class pattern) (contains? name->fnk (:name pattern)))
         (let [fnk-call (fnk->call (name->fnk (:name pattern)) (:name->pos info))
               inner-view (old-view pattern info)]
           (fn [input remaining env]
             (inner-view input remaining env)
             (fnk-call env)))
         (old-view pattern info)))))

;; --- DEPTH ---

(def ^:dynamic *depth*)

(defn with-depth
  ([old-view pattern]
     (let [f (old-view pattern)]
       (fn [input]
         (binding [*depth* 0]
           (f input)))))
  ([old-view pattern info]
     (let [f (old-view pattern info)]
       (fn [input remaining env]
         (binding [*depth* (inc *depth*)]
           (f input remaining env))))))

(defn with-node-depth
  ([old-view pattern]
     (let [f (old-view pattern)]
       (fn [input]
         (binding [*depth* 0]
           (f input)))))
  ([old-view pattern info]
     (let [f (old-view pattern info)]
       (if (instance? Node pattern)
         (fn [input remaining env]
           (binding [*depth* (inc *depth*)]
             (f input remaining env)))
         f))))

;; --- TRACING ---

(defn- indent [n]
  (apply str (repeat (* 4 n) " ")))

(defn wrap-with-trace [f name]
  (fn [input remaining env]
    (println (indent *depth*) "=>" name input)
    (try
      (let [output (f input remaining env)]
        (println (indent *depth*) "<=" name output (when remaining (get-remaining! remaining)))
        output)
      (catch Failure failure
        (println (indent *depth*) "X" name (str failure))
        (throw failure)))))

(defn trace-all
  ([old-view pattern]
     (old-view pattern))
  ([old-view pattern info]
     (wrap-with-trace (old-view pattern info) (pr-str pattern))))

(defn trace-nodes
  ([old-view pattern]
     (old-view pattern))
  ([old-view pattern info]
     (if (instance? Node pattern)
       (wrap-with-trace (old-view pattern info) (:name pattern))
       (old-view pattern info))))

;; --- DEEPEST FAILURE ---

(def ^:dynamic *deepest-depth*)
(def ^:dynamic *deepest-failure*)

(defn with-deepest-failure
  ([old-view pattern]
     (let [f (old-view pattern)]
       (fn [input]
         (binding [*deepest-depth* 0
                   *deepest-failure* nil]
           (try
             (f input)
             (catch Failure failure
               (throw *deepest-failure*)))))))
  ([old-view pattern info]
     (let [f (old-view pattern info)]
       (if (instance? Node pattern)
         (let [name (:name pattern)]
           (fn [input remaining env]
             (try
               (f input remaining env)
               (catch Failure failure
                 (when (> *depth* *deepest-depth*)
                   (set! *deepest-depth* *depth*)
                   (set! *deepest-failure* (Failure. (str failure " at node `" name "` on input `" input "`"))))
                 (throw failure)))))
         f))))
