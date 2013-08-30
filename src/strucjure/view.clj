(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [clojure.set :refer [subset? union intersection difference]]
            [plumbing.core :refer [aconcat]]
            [proteus :refer [let-mutable]]
            [strucjure.util :refer [with-syms]]
            [strucjure.core :as core]))

(defmacro assert [bool & msg]
  `(clojure.core/assert ~bool (binding [*print-meta* true] (pr-str ~@msg))))

(defn key->sym [key]
  (symbol (.substring (str key) 1)))

(defn fnk->call [fnk]
  (if-let [[pos-fn keywords] (:plumbing.fnk.impl/positional-info (meta fnk))]
    (cons pos-fn (map key->sym keywords))
    (assert nil (pr-str "Not a fnk:" fnk))))

(defn step-with-overrides [core name->override]
  (if-let [override (name->override (:name core))]
    (vary-meta core assoc :override override)
    core))

(defn with-bound [core]
  (let [subcores (map with-bound (core/subcores core))
        bounds (map #(:bound (meta %)) subcores)]
    (if-not (instance? strucjure.core.Or core)
      (let [collisions (apply intersection #{} bounds)]
        (assert (empty? collisions) "Names" collisions "collide in" core)))
    (let [bound (conj (apply union bounds) (:name core))]
      (vary-meta (core/with-subcores core subcores) assoc :bound bound))))

(defn with-used [core used]
  (let [used (if-let [fnk (or (:fnk core) (:override (meta core)))]
               (union used (rest (fnk->call fnk)))
               used)
        used? (used (:name core))]
    (vary-meta (core/fmap core #(with-used % used)) assoc :used used :used? used?)))

(defn get-used [core]
  (apply union (:used (meta core)) (map get-used (core/subcores core))))

(defn check-unused [core name->override]
  (let [unused (difference (set (keys name->override)) (:bound (meta core)))]
    (assert (empty? unused) "Names" unused "are overridden but not bound in" core)))

(defn step-check-unbound [core]
  (let [unbound (difference (:used (meta core)) (:bound (meta core)))]
    (assert (empty? unbound) "Names" unbound "are used but not bound in" core)))

(defn with-output? [core output?]
  (let [output? (cond
                 (:override (meta core)) false
                 ((:used (meta core)) (:name core)) true
                 :else output?)
        core (if (instance? strucjure.core.And core)
               (core/->And (with-output? (:core-a core) false) (with-output? (:core-b core) output?))
               (core/fmap core #(with-output? % output?)))]
    (vary-meta core assoc :output? output?)))

(defn with-remaining? [core remaining?]
  (let [core (vary-meta core assoc :remaining? remaining?)]
    (condp instance? core
      strucjure.core.Struct (core/fmap core #(with-remaining? % false))
      strucjure.core.Chain (core/->Chain (with-remaining? (:core-a core) true) (with-remaining? (:core-b core) remaining?))
      (core/fmap core #(with-remaining? % remaining?)))))

(def failure (Exception. "Match failed"))

(def fail
  `(throw failure))

(defn on-fail [t f]
  `(try ~t
        (catch Exception exc#
          (if-not (identical? failure exc#)
            (throw exc#)
            ~f))))

(defn core->view* [core input]
  (if-not (symbol? input)
    (with-syms [input']
      `(let [~input' ~input] ~(core->view* core input')))
    (let [{:keys [output? remaining? used? override]} (meta core)]
      (condp instance? core

        strucjure.core.Constant
        `(if (= ~input '~(:constant core))
           (do ~(when remaining? `(set! ~'&remaining nil))
               ~input)
           ~fail)

        strucjure.core.Name
        (with-syms [output]
          `(let [~output ~(core->view* (:core core))
                 ~@(when override [output (fnk->call override)])]
             ~(when used? `(set! ~(:name core) ~output))
             ~output))

        strucjure.core.GuardOuter
        `(if (~(:fn core) ~input)
           ~(core->view* (:core core) input)
           ~fail)

        strucjure.core.GuardInner
        `(let [output# ~(core->view* (:core core) input)]
           (if ~(fnk->call (:fnk core))
             output#
             ~fail))

        strucjure.core.Struct
        (let [remaining (:remaining core)]
          `(let [~(:input core) ~input
                 ~@(aconcat (for [[name subcore subinput] (:parts core)]
                              [name (core->view* subcore subinput)]))]
             ~(if remaining?
                `(set! ~'&remaining ~remaining)
                (when remaining `(when ~remaining ~fail)))
             ~(when output? (:output core))))

        strucjure.core.Chain
        (with-syms [link-a remaining link-b]
          `(let [~link-a ~(core->view* (:core-a core) input)
                 ~remaining ~'&remaining
                 ~link-b ~(core->view* (:core-b core) remaining)]
             ~(when output? `(concat ~link-a ~link-b))))

        strucjure.core.Or
        (on-fail (core->view* (:core-a core) input)
                 (core->view* (:core-b core) input))

        strucjure.core.And
        `(do ~(core->view* (:core-a core) input)
             ~(core->view* (:core-b core) input))

        strucjure.core.ZeroOrMore
        (with-syms [acc output remaining]
          `(set [~'&remaining ~input]
             (loop [~acc []]
               (let [~remaining ~'&remaining
                     ~output ~(on-fail (core->view* (:core core) remaining) `failure)]
                 (if (identical? ~output failure)
                   (do (set! ~'&remaining ~remaining) ;; reset it in case the core above modified it before failing
                       ~acc)
                   (recur ~(when output? `(conj ~acc ~output))))))))))))

(defn core->view [core name->override output? remaining?]
  (let [core (-> core
                 (core/postwalk #(step-with-overrides % name->override))
                 with-bound
                 (with-used #{})
                 (with-output? output?)
                 (with-remaining? remaining?))]
    (check-unused core name->override)
    (core/postwalk core step-check-unbound)
    (with-syms [input output remaining]
      `(fn [~input]
         (let-mutable [~'&remaining nil
                       ~@(interleave (get-used core) (repeat nil))]
                      (let [~output ~(core->view* core input)
                            ~remaining ~'&remaining] ;; workaround for https://github.com/ztellman/proteus/issues/1
                        [~(when output? output) ~remaining]))))))

(comment
  (use 'strucjure.core 'clojure.stacktrace 'clojure.pprint)
  (with-out-str (e))
  (set! *print-meta* true)
  (get-used (with-used (with-bound (->Constant [1 2])) #{}))
  (core->view (->Constant [1 2]) {} true true)
  (core->view (->Constant [1 2]) {} false true)
  (defn list-x [x] (->Struct 'in-a [['first-a (->Constant x) '(first in-a)]] '(list first-a) '(next in-a)))
  (with-remaining? (list-x 1) true)
  (with-output? (with-used (list-x 1) #{}) true)
  ((eval (core->view (list-x 1) {} true true)) (list 1))
  ((eval (core->view (list-x 1) {} true true)) (list 1 2))
  ((eval (core->view (list-x 1) {} true false)) (list 1 2))
  (defn list-xy [x y] (->Chain (list-x x) (list-x y)))
  (pprint (core->view (list-xy 1 2) {} true true))
  ((eval (core->view (list-xy 1 2) {} true true)) (list 1 2))
  )
