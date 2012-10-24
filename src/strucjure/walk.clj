(ns strucjure.walk
  (:require  [strucjure.parser :as parser]
             [strucjure.view :as view]))

(parser/defview walk
  (and clojure.lang.IRecord ?record [& ((view/zero-or-more walk) ?vals)])
  (clojure.lang.Reflector/invokeConstructor (class record) (to-array vals))

  (and list? [& ((view/zero-or-more walk) ?vals)])
  (apply list vals)

  (and clojure.lang.MapEntry [& ((view/zero-or-more walk) ?vals)])
  (vec vals)

  (and seq? [& ((view/zero-or-more walk) ?vals)])
  vals

  (and coll? ?collection [& ((view/zero-or-more walk) ?vals)])
  (into (empty collection) vals)

  ?other
  other)

(defn prewalk [f form]
  (view/run-or-throw walk form {:pre-view (fn [name form] (f form))}))

(defn postwalk [f form]
  (view/run-or-throw walk form {:post-view (fn [name form] (f form))}))

(defn replace [view input]
  (if-let [[remaining output] (view/run view input {})]
    output
    input))

(defn expand [view input]
  (if-let [[remaining output] (view/run view input {})]
    (recur view output)
    input))
