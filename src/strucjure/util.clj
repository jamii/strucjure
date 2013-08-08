(ns strucjure.util
  (:require [plumbing.core :refer [for-map map-vals]]))

;; --- WALKS ---

(defn walk
  "Like clojure.walk/walk but works (inefficiently) on records"
  [inner outer form]
  (cond
   (list? form) (outer (apply list (map inner form)))
   (seq? form) (outer (doall (map inner form)))
   (vector? form) (outer (vec (map inner form)))
   (instance? clojure.lang.IRecord form) (outer (reduce (fn [form [k v]] (assoc form k (inner v))) form form))
   (map? form) (outer (into (if (sorted? form) (sorted-map) {})
                            (map inner form)))
   (set? form) (outer (into (if (sorted? form) (sorted-set) #{})
                            (map inner form)))
   :else (outer form)))

(defn walk-replace [form class->fn]
  (if-let [replace-fn (class->fn (class form))]
    (replace-fn form)
    (walk #(walk-replace % class->fn) identity form)))

(defn walk-collect [form classes]
  (let [results (for-map [class classes] class (atom []))
        replace-fn (fn [class] (fn [form] (swap! (results (type form)) conj form)))
        class->fn (for-map [class classes] class (replace-fn class))]
    (walk-replace form class->fn)
    (map-vals deref results)))
