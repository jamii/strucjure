(ns strucjure.sugar
  (:refer-clojure :exclude [with-meta * + or and])
  (:require [plumbing.core :refer [fnk for-map aconcat]]
            [strucjure.util :refer [with-syms]]
            [strucjure.pattern :as pattern :refer [->Rest ->Seqable ->Any ->Is ->Guard ->Name ->Or ->And ->Repeated ->WithMeta ->Node ->Edge ->Graph]]
            [strucjure.graph :as graph]
            [strucjure.view :as view]))

(def _ (->Any))
(def is ->Is)
(def guard ->Guard)
(def name ->Name)
(defn * [pattern] (->Repeated 0 Long/MAX_VALUE pattern))
(defn + [pattern] (->Repeated 1 Long/MAX_VALUE pattern))
(defn ? [pattern] (->Repeated 0 1 pattern))
(def with-meta ->WithMeta)
(defn or [& patterns] (->Or (vec patterns)))
(defn and [& patterns] (->And (vec patterns)))
(defn seqable [& patterns] (->Seqable (vec patterns)))
(def edge ->Edge)
(def node ->Node)
(def node-of ->Graph) ;; TODO poor naming?
(def & ->Rest)
(defn &* [pattern] (& (* pattern)))
(defn &+ [pattern] (& (+ pattern)))
(defn &? [pattern] (& (? pattern)))
(defn &*& [pattern] (& (* (& pattern))))
(defn &+& [pattern] (& (+ (& pattern))))
(defn &?& [pattern] (& (? (& pattern))))

(defn- with-names [form]
  (clojure.walk/prewalk
   (fn [form]
     (if-let [name (:tag (meta form))]
       `(->Name '~name ~(vary-meta form dissoc :tag))
       form))
   form))

(def overrides #{'_ 'is 'guard 'name '* '+ '? 'with-meta 'or 'and 'seqable 'edge 'node 'node-of 'graph '& '&* '&+ '&? '&*& '&+& '&?&})

(defn- with-overrides [form]
  (clojure.walk/prewalk
   (fn [form]
     (if (contains? overrides form)
       (symbol (str "strucjure.sugar/" form))
       form))
   form))

(defmacro pattern [sugar]
  (with-overrides (with-names sugar)))

(defmacro with-edges [names & body]
  `(let [~@(interleave names (for [name names] `(->Edge '~name)))]
     (do ~@body)))

(defn- with-names-and-edges [names&sugars]
  `(graph/with-named-nodes
     (graph/with-named-edges
       (with-edges [~@(take-nth 2 names&sugars)]
         ~(for-map [[name sugar] (partition 2 names&sugars)] `'~name `(pattern ~sugar))))))

(defmacro graph [& names&sugars]
  (with-names-and-edges names&sugars))

(defn prewalk [pattern names&fnks]
  (view/with-layers [(view/with-pre-fns strucjure.pattern.Node names&fnks)]
    (view/*view* pattern)))

(defn postwalk [pattern names&fnks]
  (view/with-layers [(view/with-post-fns strucjure.pattern.Node names&fnks)]
    (view/*view* pattern)))
