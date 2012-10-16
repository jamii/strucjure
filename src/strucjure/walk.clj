(ns strucjure.walk
  (:use clojure.test)
  (:require clojure.walk))

;; TODO switch to views

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

(defn map-reduce [filter map reduce init form]
  (let [result (atom init)
        visit-fn (fn [form]
                   (when (filter form)
                     (swap! result reduce (map form))))]
    (previsit visit-fn form)
    @result))

(defn collect [filter form]
  (map-reduce filter identity conj () form))

(defn replace* [view form]
  (replace
    (if (= form new-form)
      new-form
      (replace* f new-form))))

(defn postreplace [f form]
  (postwalk (partial replace* f) form))

(defn prereplace [f form]
  (prewalk (partial replace* f) form))

;; TESTS
;; really need quickcheck in here

(defrecord Leaf [x])
(defrecord Branch [x l r])

(def tree-form (Branch. 4
                        (Branch. 2 (Leaf. 1) (Leaf. 3))
                        (Branch. 6 (Leaf. 5) (Leaf. 7))))

(def list-form '(4
                 (2 1 3)
                 (6 5 7)))

(defn tree->list [tree]
  (cond
   (integer? tree) tree
   (instance? Leaf tree) (:x tree)
   (instance? Branch tree) (list (:x tree) (:l tree) (:r tree))
   :else (throw (Error. ::tree->list))))

(defn split [list]
  (if (seq? list)
    (let [n (count list)
          k (int (/ n 2))]
      (if (= 1 (count list))
        (Leaf. (first list))
        (Branch. (nth list k) (take k list) (take-last k list))))
    list))

(deftest identity-test
  (is (= tree-form (prewalk identity tree-form)))
  (is (= tree-form (postwalk identity tree-form))))

(deftest construction-test
  (is (= list-form (prewalk tree->list tree-form)))
  (is (= list-form (postwalk tree->list tree-form))))

(deftest map-reduce-test
  (is (= 28 (map-reduce integer? identity + 0 tree-form))))

(deftest collect-test
  (is (= (list 7 5 6 3 1 2 4) (collect integer? tree-form))))

(deftest replace-test
  (is (= tree-form (prereplace split (list 1 2 3 4 5 6 7)))))
