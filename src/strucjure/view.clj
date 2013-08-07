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

(defn set-stubs [tree succeed-fn fail-fn]
  (walk-replace tree
                {Succeed (fn [form] (apply succeed-fn (vals form)))
                 Fail (fn [form] (apply fail-fn (vals form)))}))

(defn get-stubs [tree]
  (let [results (walk-collect tree #{Succeed Fail})]
    [(results Succeed) (results Fail)]))

;; --- BINDINGS ---
;; TODO use efficient local vars instead of atoms

(defrecord GetBinding [symbol])
(defrecord SetBinding [symbol value])

(defn with-bindings [tree]
  (let [bound (set (map :symbol (get (walk-collect tree #{GetBinding}) GetBinding)))]
    `(let [~@(apply concat (for [symbol bound] [symbol `(atom nil)]))]
       ~(walk-replace tree
                      {GetBinding (fn [{:keys [symbol]}]
                                    `(deref ~symbol))
                       SetBinding (fn [{:keys [symbol value]}]
                                    (when (bound symbol)
                                      `(reset! ~symbol ~value)))}))))

;; --- TREES ---
;; trees are decisions with more than one success/fail path

(defn tree->bound
  "The set of symbols that are bound if the tree succeeds"
  [tree]
  (let [[succeeds _] (get-stubs tree)]
    (assert (= 1 (count (set (map :bound succeeds)))) "All success paths must bind the same set of symbols")
    (:bound (first succeeds))))

(defn tree->decision
  "Reduce the number of success and failure paths to at most one each to avoid exponential branching"
  [tree]
  (let [[succeeds fails] (get-stubs tree)]
    (cond
     (and (<= (count succeeds) 1) (<= (count fails) 1)) ;; already a decision
      tree

     :else ;; store results in a binding
     (let [output-sym (gensym "output")
           remaining-sym (gensym "remaining")]
       `(if ~(set-stubs tree
                        (fn [output remaining _]
                          `(do ~(->SetBinding output-sym output)
                               ~(->SetBinding remaining-sym remaining)
                               true))
                        (fn [] false))
          ~(->Succeed (->GetBinding output-sym) (->GetBinding remaining-sym) (tree->bound tree))
          ~(->Fail))))))

;; --- COMPILER ---

(defn when-nil-remaining [remaining body]
  (if (nil? remaining)
    body
    `(if (nil? ~remaining)
       ~body
       ~(->Fail))))

(defprotocol View
  (pattern->decision [this input bound]))

(defn and->tree [sub-patterns input bound]
  (let [[sub-pattern & sub-patterns] sub-patterns]
    (if sub-patterns
      (set-stubs (pattern->decision sub-pattern input bound)
                 (fn [_ remaining bound]
                   (when-nil-remaining remaining
                                       (and->tree sub-patterns input bound)))
                 ->Fail)
      (pattern->decision sub-pattern input bound))))

(defn or->tree [sub-patterns input bound]
  (let [[sub-pattern & sub-patterns] sub-patterns]
    (if sub-patterns
      (set-stubs (pattern->decision sub-pattern input bound)
                 ->Succeed
                 (fn []
                   (or->tree sub-patterns input bound)))
      (pattern->decision sub-pattern input bound))))

(declare seq->tree)

(defn seq-rest->tree [sub-patterns output remaining bound]
  (set-stubs (seq->tree sub-patterns remaining bound)
             (fn [rest-output rest-remaining rest-bound]
               (->Succeed (cons 'list (concat output (rest rest-output))) rest-remaining rest-bound))
             ->Fail))

(defn seq->tree [sub-patterns input bound]
  (if-let [[sub-pattern & sub-patterns] sub-patterns]
    (if (instance? strucjure.pattern.& sub-pattern)
      (set-stubs (pattern->decision sub-pattern input bound)
                 (fn [output remaining bound]
                   (seq-rest->tree sub-patterns output remaining bound))
                 ->Fail)
      (let [first-sym (gensym "first")
            rest-sym (gensym "rest")]
        `(if-let [[~first-sym & ~rest-sym] ~input]
           ~(set-stubs (pattern->decision sub-pattern first-sym bound)
                       (fn [output remaining bound]
                         (when-nil-remaining remaining
                                             (seq-rest->tree sub-patterns (list output) rest-sym bound)))
                       ->Fail)
           ~(->Fail))))
    (->Succeed nil input bound)))

(extend-protocol View
  Object
  (pattern->decision [this input bound]
    `(if (= ~input ~this)
       ~(->Succeed input nil bound)
       ~(->Fail)))
  strucjure.pattern.Bind
  (pattern->decision [this input bound]
    (let [symbol (:symbol this)]
      (if (bound symbol)
        `(if (= ~(->GetBinding symbol) ~input)
           ~(->Succeed input nil bound)
           ~(->Fail))
        `(do ~(->SetBinding symbol input)
             ~(->Succeed input nil (conj bound symbol))))))
  strucjure.pattern.And
  (pattern->decision [this input bound]
    (tree->decision (and->tree (:patterns this) input bound)))
  strucjure.pattern.Or
  (pattern->decision [this input bound]
    (tree->decision (or->tree (:patterns this) input bound)))
  clojure.lang.ISeq
  (pattern->decision [this input bound]
    (tree->decision
     `(if (instance? clojure.lang.Seqable ~input)
        ~(seq->tree this input bound)
        ~(->Fail)))))

;; TODO not sure I like the use of the dummy arg to decide whether output is produced
;;      may be sufficient to always have a final output in views?
(defn pattern->view [pattern]
  (let [input-sym (gensym "input")
        decision (pattern->decision pattern input-sym #{})]
    `(fn
       ([~input-sym]
          ~(with-bindings (set-stubs decision
                                     (fn [output remaining _] [remaining])
                                     (fn [] nil))))
       ([~input-sym ~'_]
          ~(with-bindings (set-stubs decision
                                     (fn [output remaining _] [output remaining])
                                     (fn [] nil)))))))

;; (pattern->decision-with-locals (->Bind 'a) 'input #{})
;; (pattern->decision-with-locals (->Bind 'a) 'input #{'a})
;; (and->tree [(->Bind 'a) (->Bind 'b)] 'input #{})
;; (pattern->decision (->And [(->Bind 'a) 1]) 'input #{})
;; (pattern->decision (->And [(->Bind 'a) 1 2]) 'input #{})
;; (pattern->decision (->And [(->Bind 'a) (->Bind 'b)]) 'input #{})
;; (pattern->decision-with-locals (->And [(->Bind 'a) (->Bind 'b)]) 'input #{})
;; (pattern->view (->And [(->Bind 'a) (->Bind 'b)]))
;; ((eval (pattern->view (->And [(->Bind 'a) (->Bind 'b)]))) 1)
;; ((eval (pattern->view (->And [1 (->Bind 'b)]))) 1)
;; ((eval (pattern->view (->And [1 (->Bind 'b)]))) 2)
;; (pattern->decision (list 1) 'input #{})
;; (pattern->decision (list 1 2) 'input #{})
;; ((eval (pattern->view (list 1 2))) (list 1 2))
;; ((eval (pattern->view (list 1 2))) (list 1 2) nil)
;; ((eval (pattern->view (list 1 2))) (list 1))
;; ((eval (pattern->view (list 1 2))) (list 1 2 3))
;; ((eval (pattern->view (list 1 2))) (list 1 3))
;; ((eval (pattern->view (list 1 2))) [1 2])
;; ((eval (pattern->view (list 1 2))) 1)
