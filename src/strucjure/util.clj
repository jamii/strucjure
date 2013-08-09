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
    (walk-replace (replace-fn form) class->fn)
    (walk #(walk-replace % class->fn) identity form)))

(defn walk-collect [form classes]
  (let [results (for-map [class classes] class (atom []))]
    (letfn [(walk-collect-loop [form]
              (when (contains? classes form)
                (swap! (results class) conj form))
              (walk walk-collect-loop identity form))]
      (walk-collect-loop form)
      (map-vals deref results))))
