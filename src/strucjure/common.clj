(ns strucjure.common
  (:require clojure.walk))

(defrecord Or [patterns])
(defrecord And [patterns])

;; TODO replace clojure.walk/walk with a version that can handle records
(defn set-stubs [form class->fn]
  (if-let [update-fn (class->fn (class form))]
    (update-fn form)
    (clojure.walk/walk #(set-stubs % class->fn) identity form)))

(defn get-stubs [form classes]
  (let [results (into {} (for [class classes] [class (atom [])]))
        update-fn (fn [class] (fn [form] (swap! (results (type form)) conj form)))
        class->fn (into {} (for [class classes] [class (update-fn class)]))]
    (set-stubs form class->fn)
    results))
