(ns strucjure.pattern
  (:require clojure.walk
            [clojure.set :refer [union]]
            [strucjure.pattern :refer [->Bind ->Or ->And]]))

;; --- STUBS ---
;; TODO use a version of walk that works on records

(defrecord Succeed [output remaining bound])
(defrecord Fail [])

(defn set-stubs [form succeed-fn fail-fn]
  (cond
   (instance? Succeed form) (apply succeed-fn (vals form))
   (instance? Fail form) (fail-fn)
   :else (clojure.walk/walk #(set-stubs % succeed-fn fail-fn) identity form)))

(defn get-stubs [form]
  (let [succeeds (atom [])
        fails (atom [])]
    (set-stubs form #(swap! succeeds conj (->Succeed %1 %2 %3)) #(swap! fails conj (->Fail)))
    [@succeeds @fails]))

;; --- BINDINGS ---
;; TODO use stubs and efficient local vars instead of atoms

(defn get-binding [symbol]
  `(deref ~symbol))

(defn set-binding [symbol value]
  `(reset! ~symbol ~value))

(defn with-bindings [symbols form]
  `(let [~@(apply concat (for [symbol symbols] [symbol `(atom nil)]))]
     ~form))

;; --- BUSHES ---
;; bushes are trees with more than one success/fail path

(defn bush->bound [bush]
  "The set of symbols that are bound if the bush succeeds"
  (let [[succeeds _] (get-stubs bush)]
    (assert (= 1 (count (set (map :bound succeeds)))) "All success paths must bind the same set of symbols")
    (:bound (first succeeds))))

(defn bush->tree [bush]
  "Reduce the number of success and failure paths to at most one each to avoid exponential branching"
  (let [[succeeds fails] (get-stubs bush)]
    (cond
     ;; already a tree
     (and (<= (count succeeds) 1) (<= (count fails) 1))
      bush

     ;; different returns from different success paths - store results in a mutable var
     :else
     (let [output-sym (gensym "output")
           remaining-sym (gensym "remaining")]
       `(if ~(set-stubs bush
                        (fn [output remaining _]
                          `(do ~(set-binding output-sym output)
                               ~(set-binding remaining-sym remaining)
                               true))
                        (fn [] false))
          ~(->Succeed (get-binding output-sym) (get-binding remaining-sym) (conj (bush->bound bush) output-sym remaining-sym))
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
        `(if (= ~(get-binding symbol) ~input)
           ~(->Succeed input nil bound)
           ~(->Fail))
        `(do ~(set-binding symbol input)
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

(defn pattern->tree-with-locals [pattern input bound]
  (let [tree (pattern->tree pattern input bound)]
    (with-bindings (bush->bound tree) tree)))

(defn pattern->view [pattern]
  (let [input-sym (gensym "input")]
    `(fn [~input-sym]
       ~(set-stubs (pattern->tree-with-locals pattern input-sym #{})
                   (fn [output remaining bound]
                     [output remaining])
                   (fn []
                     nil)))))

;; (pattern->tree-with-locals (->Bind 'a) 'input #{})
;; (pattern->tree-with-locals (->Bind 'a) 'input #{'a})
;; (and->bush [(->Bind 'a) (->Bind 'b)] 'input #{})
;; (meta (->And [(->Bind 'a) (->Bind 'b)]))
;; (command? 'and (->And [(->Bind 'a) (->Bind 'b)]))
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
;; ((eval (pattern->view (list 1 2))) (list 1))
;; ((eval (pattern->view (list 1 2))) (list 1 2 3))
;; ((eval (pattern->view (list 1 2))) (list 1 3))
;; ((eval (pattern->view (list 1 2))) [1 2])
;; ((eval (pattern->view (list 1 2))) 1)
