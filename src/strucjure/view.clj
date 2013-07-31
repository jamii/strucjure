(ns strucjure.pattern
  (:require [clojure.set :refer [union]]
            [strucjure.pattern :refer [->Bind ->Or ->And]]))

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
  (let [results (into {} (for [class classes] [class (atom [])]))
        replace-fn (fn [class] (fn [form] (swap! (results (type form)) conj form)))
        class->fn (into {} (for [class classes] [class (replace-fn class)]))]
    (walk-replace form class->fn)
    (into {} (for [[class forms] results] [class @forms]))))

;; --- STUBS ---
;; TODO use a version of walk that works on records

(defrecord Succeed [output remaining bound])
(defrecord Fail [])

(defn set-stubs [bush succeed-fn fail-fn]
  (walk-replace bush
                {Succeed (fn [form] (apply succeed-fn (vals form)))
                 Fail (fn [form] (apply fail-fn (vals form)))}))

(defn get-stubs [bush]
  (let [results (walk-collect bush #{Succeed Fail})]
    [(results Succeed) (results Fail)]))

;; --- BINDINGS ---
;; TODO use efficient local vars instead of atoms

(defrecord GetBinding [symbol])
(defrecord SetBinding [symbol value])

(defn with-bindings [bush]
  (let [bound (set (map :symbol (get (walk-collect bush #{GetBinding}) GetBinding)))]
    `(let [~@(apply concat (for [symbol bound] [symbol `(atom nil)]))]
       ~(walk-replace bush
                      {GetBinding (fn [{:keys [symbol]}]
                                    `(deref ~symbol))
                       SetBinding (fn [{:keys [symbol value]}]
                                    (when (bound symbol)
                                      `(reset! ~symbol ~value)))}))))

;; --- BUSHES ---
;; bushes are trees with more than one success/fail path

(defn bush->bound
  "The set of symbols that are bound if the bush succeeds"
  [bush]
  (let [[succeeds _] (get-stubs bush)]
    (assert (= 1 (count (set (map :bound succeeds)))) "All success paths must bind the same set of symbols")
    (:bound (first succeeds))))

(defn bush->tree
  "Reduce the number of success and failure paths to at most one each to avoid exponential branching"
  [bush]
  (let [[succeeds fails] (get-stubs bush)]
    (cond
     (and (<= (count succeeds) 1) (<= (count fails) 1)) ;; already a tree
      bush

     :else ;; store results in a binding
     (let [output-sym (gensym "output")
           remaining-sym (gensym "remaining")]
       `(if ~(set-stubs bush
                        (fn [output remaining _]
                          `(do ~(->SetBinding output-sym output)
                               ~(->SetBinding remaining-sym remaining)
                               true))
                        (fn [] false))
          ~(->Succeed (->GetBinding output-sym) (->GetBinding remaining-sym) (bush->bound bush))
          ~(->Fail))))))

;; --- COMPILER ---

(defprotocol View
  (pattern->tree [this input bound]))

(defn and->bush [sub-patterns input bound]
  (let [[sub-pattern & sub-patterns] sub-patterns]
    (if sub-patterns
      (set-stubs (pattern->tree sub-pattern input bound)
                 (fn [_ _ bound] (and->bush sub-patterns input bound))
                 ->Fail)
      (pattern->tree sub-pattern input bound))))

(defn or->bush [sub-patterns input bound]
  (let [[sub-pattern & sub-patterns] sub-patterns]
    (if sub-patterns
      (set-stubs (pattern->tree sub-pattern input bound)
                 ->Succeed
                 (fn [] (or->bush sub-patterns input bound)))
      (pattern->tree sub-pattern input bound))))

(defn seq-rest->bush [sub-patterns output remaining bound]
  (set-stubs (seq->bush sub-patterns remaining bound)
             (fn [rest-output rest-remaining rest-bound]
               (->Succeed (cons 'list (concat output (rest rest-output))) rest-remaining rest-bound))
             ->Fail))

(defn seq->bush [sub-patterns input bound]
  (if-let [[sub-pattern & sub-patterns] sub-patterns]
    (if (instance? strucjure.pattern.& sub-pattern)
      (set-stubs (pattern->tree sub-pattern input bound)
                 (partial seq-rest->bush sub-patterns)
                 ->Fail)
      (let [first-sym (gensym "first")
            rest-sym (gensym "rest")]
        `(if-let [[~first-sym & ~rest-sym] ~input]
           ~(set-stubs (pattern->tree sub-pattern first-sym bound)
                       (fn [output _ bound]
                         (seq-rest->bush sub-patterns (list output) rest-sym bound))
                       ->Fail)
           ~(->Fail))))
    (->Succeed nil input bound)))

(extend-protocol View
  Object
  (pattern->tree [this input bound]
    `(if (= ~input ~this)
       ~(->Succeed input nil bound)
       ~(->Fail)))
  strucjure.pattern.Bind
  (pattern->tree [this input bound]
    (let [symbol (:symbol this)]
      (if (bound symbol)
        `(if (= ~(->GetBinding symbol) ~input)
           ~(->Succeed input nil bound)
           ~(->Fail))
        `(do ~(->SetBinding symbol input)
             ~(->Succeed input nil (conj bound symbol))))))
  strucjure.pattern.And
  (pattern->tree [this input bound]
    (bush->tree (and->bush (:patterns this) input bound)))
  strucjure.pattern.Or
  (pattern->tree [this input bound]
    (bush->tree (or->bush (:patterns this) input bound)))
  clojure.lang.ISeq
  (pattern->tree [this input bound]
    (bush->tree
     `(if (instance? clojure.lang.Seqable ~input)
        ~(seq->bush this input bound)
        ~(->Fail)))))

;; TODO not sure I like the use of the dummy arg to decide whether output is produced
(defn pattern->view [pattern]
  (let [input-sym (gensym "input")
        tree (pattern->tree pattern input-sym #{})]
    `(fn
       ([~input-sym]
          ~(with-bindings (set-stubs tree
                                     (fn [output remaining _] [remaining])
                                     (fn [] nil))))
       ([~input-sym ~'_]
          ~(with-bindings (set-stubs tree
                                     (fn [output remaining _] [output remaining])
                                     (fn [] nil)))))))

;; (pattern->tree-with-locals (->Bind 'a) 'input #{})
;; (pattern->tree-with-locals (->Bind 'a) 'input #{'a})
;; (and->bush [(->Bind 'a) (->Bind 'b)] 'input #{})
;; (pattern->tree (->And [(->Bind 'a) 1]) 'input #{})
;; (pattern->tree (->And [(->Bind 'a) 1 2]) 'input #{})
;; (pattern->tree (->And [(->Bind 'a) (->Bind 'b)]) 'input #{})
;; (pattern->tree-with-locals (->And [(->Bind 'a) (->Bind 'b)]) 'input #{})
;; (pattern->view (->And [(->Bind 'a) (->Bind 'b)]))
;; ((eval (pattern->view (->And [(->Bind 'a) (->Bind 'b)]))) 1)
;; ((eval (pattern->view (->And [1 (->Bind 'b)]))) 1)
;; ((eval (pattern->view (->And [1 (->Bind 'b)]))) 2)
;; (pattern->tree (list 1) 'input #{})
;; (pattern->tree (list 1 2) 'input #{})
;; ((eval (pattern->view (list 1 2))) (list 1 2))
;; ((eval (pattern->view (list 1 2))) (list 1 2) nil)
;; ((eval (pattern->view (list 1 2))) (list 1))
;; ((eval (pattern->view (list 1 2))) (list 1 2 3))
;; ((eval (pattern->view (list 1 2))) (list 1 3))
;; ((eval (pattern->view (list 1 2))) [1 2])
;; ((eval (pattern->view (list 1 2))) 1)
