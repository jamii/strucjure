(ns strucjure.walk
  (:use clojure.test)
  (:require clojure.walk
            strucjure.view))

(defn walk [inner form]
  (cond
   (instance? clojure.lang.IRecord form) (clojure.lang.Reflector/invokeConstructor (class form) (to-array (map inner (vals form))))
   (list? form) (apply list (map inner form))
   (instance? clojure.lang.IMapEntry form) (vec (map inner form))
   (seq? form) (doall (map inner form))
   (coll? form) (into (empty form) (map inner form))
   :else form))

(defn visit [inner form]
  (when (instance? clojure.lang.Seqable form)
    (doseq [inner-form form]
      (inner inner-form))))

(defn postwalk [f form]
  (f (walk (partial postwalk f) form)))

(defn prewalk [f form]
  (walk (partial prewalk f) (f form)))

(defn postvisit [f form]
  (visit (partial postvisit f) form)
  (f form))

(defn previsit [f form]
  (f form)
  (visit (partial previsit f) form))

(defn map-reduce [view init form]
  (let [acc (atom init)
        visit-fn (fn [input]
                    (strucjure.view/run view [input @acc]
                                        (fn [new-acc _]
                                          (compare-and-set! acc @acc new-acc))
                                        (fn [] nil)))]
    (previsit visit-fn form)
    @acc))

(defn collect [view form]
  (let [acc (atom ())
        visit-fn (fn [input]
                   (strucjure.view/run view input
                                       (fn [output _]
                                         (swap! acc conj output))
                                       (fn [] nil)))]
    (previsit visit-fn form)
    @acc))

(defn postwalk-replace [view form]
  (postwalk (partial strucjure.view/replace view) form))

(defn prewalk-replace [view form]
  (prewalk (partial strucjure.view/replace view) form))

(defn postwalk-expand [view form]
  (postwalk (partial strucjure.view/expand view) form))

(defn prewalk-expand [view form]
  (prewalk (partial strucjure.view/expand view) form))

;; --- TESTS ---
;; really need quickcheck in here

(defrecord Leaf [x])
(defrecord Branch [x l r])

(def tree-form
  (Branch. "a"
           (Branch. "b" (Leaf. "d") (Leaf. "e"))
           (Branch. "c" (Leaf. "f") (Leaf. "g"))))

(def list-form
  '("a"
    ("b" "d" "e")
    ("c" "f" "g")))

(defn tree->list [form]
  (cond
   (string? form) form
   (instance? Leaf form) (:x form)
   (instance? Branch form) (list (:x form) (:l form) (:r form))
   :else (throw (Error. ::tree->list))))

(deftest identity-test
  (is (= tree-form (prewalk identity tree-form)))
  (is (= tree-form (postwalk identity tree-form))))

(deftest construction-test
  (is (= list-form (prewalk tree->list tree-form)))
  (is (= list-form (postwalk tree->list tree-form))))

(def str-reduce
  (strucjure.view/->Raw
   (fn [[input acc]]
     (if (string? input)
       (str input acc)
       acc))))

(deftest map-reduce-test
  (is (= "gfcedba" (map-reduce str-reduce "" tree-form))))

(deftest collect-test
  (is (= (list "c" "b" "a") (collect #"[a-c]" tree-form))))

(defn replace-string [form]
  (if (string? form)
    (str "replacement for " form)
    form))

(def replaced-strings
  (into {} (for [char "abcdefg"] [(str char) (replace-string (str char))])))

(deftest replace-test
  (is (=
       (clojure.walk/prewalk-replace replaced-strings list-form)
       (prewalk-replace (strucjure.view/->Raw replace-string) list-form))))
