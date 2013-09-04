(ns strucjure.view
  (:refer-clojure :exclude [assert])
  (:require [plumbing.core :refer [aconcat]]
            [strucjure.util :refer [with-syms assert fnk->call]]
            [strucjure.core :as core]
            [strucjure.pattern :as pattern]))

;; TODO only allowed remaining inside Rest?
;; TODO catch exceptions from output and guards etc
;; TODO need to figure out what rules I'm assuming about &remaining

(defn with-output? [core output?]
  (let [output? (cond
                 (:override (meta core)) false
                 ((:used (meta core)) (:name core)) true
                 :else output?)
        core (if (instance? strucjure.core.And core)
               (update core :core-a #(with-output? % false) :core-b #(with-output? % output?))
               (fmap core #(with-output? % output?)))]
    (vary-meta core assoc :output? output?)))

(defn step-with-overrides [core name->override]
  (if-let [override (name->override (:name core))]
    (assoc core :fnk override)
    core))

(def failure (Exception. "Match failed"))

(def fail
  `(throw failure))

(defn on-fail [t f]
  `(try ~t
        (catch Exception exc#
          (if-not (identical? failure exc#)
            (throw exc#)
            ~f))))

(def new-mutable
  `(new proteus.Containers$O nil))

(defn get-mutable! [sym]
  `(.x ~sym))

(defn set-mutable! [sym value]
  `(.set ~sym ~value))

(defn core->view* [core input]
  (if-not (symbol? input)
    (with-syms [input']
      `(let [~input' ~input] ~(core->view* core input')))
    (let [{:keys [output? remaining? used? override]} (meta core)]
      (condp instance? core

        strucjure.core.All
        `(do ~(set-mutable! '&remaining nil)
             ~input)

        strucjure.core.None
        `(do ~(set-mutable! '&remaining input)
             nil)

        strucjure.core.Name
        (with-syms [output]
          `(let [~output ~(core->view* (:core core) input)
                 ~@(when override [output (fnk->call override)])]
             ~(when used? (set-mutable! (:name core) output))
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
        `(let [~(:input core) ~input
               ~@(aconcat (for [[name subcore subinput type] (:parts core)]
                            [name (if (= :remaining subinput)
                                    (get-mutable! '&remaining)
                                    subinput)
                             name (core->view* subcore name)]))]
           ~(when output? (:output core)))

        strucjure.core.Or
        (on-fail (core->view* (:core-a core) input)
                 (core->view* (:core-b core) input))

        strucjure.core.And
        `(do ~(core->view* (:core-a core) input)
             ~(core->view* (:core-b core) input))

        strucjure.core.ZeroOrMore
        (with-syms [remaining acc output]
          `(loop [~remaining ~input ~acc []]
             (let [~output ~(on-fail (core->view* (:core core) remaining) `failure)]
               (if (identical? ~output failure)
                 (do ~(set-mutable! '&remaining remaining) ;; reset it in case the core above modified it before failing
                     (seq ~acc))
                 (recur ~(get-mutable! '&remaining) ~(when output? `(apply conj ~acc ~output)))))))))))

(defn core->view [core name->override output?]
  (let [core (-> core
                 (core/postwalk #(step-with-overrides % name->override))
                 core/with-bound
                 (core/with-used #{})
                 (with-output? output?))]
    (core/check-unused core (keys name->override))
    (core/postwalk core core/step-check-unbound)
    (with-syms [input output remaining]
      `(fn [~input]
         (let [~@(interleave (cons '&remaining (get-used core)) (repeat new-mutable))]
           (let [~output ~(core->view* core input)]
             [~(when output? output) ~(get-mutable! '&remaining)]))))))

(defn pattern->view [pattern name->override output?]
  (core->view (pattern/pattern->core pattern) name->override output?))
