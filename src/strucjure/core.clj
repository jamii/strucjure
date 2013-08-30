(ns strucjure.core)

;; the narrow waist

(defrecord Constant [constant])
(defrecord Name [name core])
(defrecord GuardOuter [fn core])
(defrecord GuardInner [core fnk])
(defrecord Struct [input parts output remaining]) ;; parts is [[name core input]]
(defrecord Chain [core-a core-b])
(defrecord Or [core-a core-b])
(defrecord And [core-a core-b])
(defrecord ZeroOrMore [core])

(defn core? [form]
  (#{Constant Name GuardOuter GuardInner Struct Chain Or And ZeroOrMore} (class form)))

(defn subcores [core]
  (assert core? core)
  (condp contains? (class core)
    #{Constant} []
    #{Struct} (for [[_ subcore _] (:parts core)] subcore)
    #{Or And Chain} [(:core-a core) (:core-b core)]
    [(:core core)]))

(defn with-subcores [core subcores]
  (assert core? core)
  (condp contains? (class core)
    #{Constant} core
    #{Struct} (assoc core :parts (for [[[name _ input] subcore] (map vector (:parts core) subcores)] [name subcore input]))
    #{Or And Chain} (assoc core :core-a (first subcores) :core-b (second subcores))
    (assoc core :core (first subcores))))

(defn fmap [core f]
  (with-subcores core (map f (subcores core))))

(defn postwalk [core f]
  (f (fmap core f)))
