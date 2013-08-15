(ns strucjure.graph
  (:require [plumbing.core :refer [for-map aconcat]]
            [strucjure.pattern :as pattern]
            [strucjure.view :as view]))

;; TODO think about naming eg graph -> vars -> view
;; TODO get-in with-deepest-error

(defn output-in [patterns & names&forms]
  (apply assoc patterns
         (aconcat (for [[name form] (partition 2 names&forms)]
                    [name (pattern/->Output (get patterns name) form)]))))

(defn patterns->graph [patterns]
  `(with-meta
     ~(for-map [[name pattern] patterns]
               `'~name
               `(fn [{:syms [~@(keys patterns)]}] ~(view/pattern->view pattern)))
     {::wrapper identity}))

(defn graph->view [graph name]
  (let [vars (for-map [name (keys graph)] name (.. clojure.lang.Var create setDynamic))]
    (doseq [name (keys graph)]
      (alter-var-root (vars name) (constantly ((graph name) vars))))
    ((::wrapper (meta graph)) (var-get (vars name)))))

(defn update-in [graph & names&fs]
  (apply assoc graph
         (aconcat (for [[name f] (partition 2 names&fs)]
                   [name (fn [vars] (f ((get graph name) vars)))]))))

(defn update-all [graph f]
  (with-meta
    (for-map [[name node] graph] name (fn [vars] (f name (node vars))))
    (meta graph)))

(defn with-wrapper [graph wrapper]
  (vary-meta graph clojure.core/update-in [::wrapper] #(comp wrapper %)))

(defn with-binding [graph var init-val]
  (with-wrapper graph
    (fn [view]
      (fn [input]
        (push-thread-bindings {var init-val})
        (try
          (view input)
          (finally (pop-thread-bindings)))))))

(defn with-depth [graph on-input on-result]
  (let [depth (.. clojure.lang.Var create setDynamic)]
    (update-all (with-binding graph depth 0)
                (fn [name view]
                  (fn [input]
                    (on-input (var-get depth) name input)
                    (var-set depth (inc (var-get depth)))
                    (let [result (view input)]
                      (var-set depth (dec (var-get depth)))
                      (on-result (var-get depth) name result)
                      result))))))

(defn- indent [n]
  (apply str (repeat (* 4 n) \ )))

(defn trace [graph]
  (with-depth graph
    (fn [depth name input]
      (println (str (indent depth) "=>") name input))
    (fn [depth name result]
      (if result
        (println (str (indent depth) "<=") name result)
        (println (str (indent depth) "X ") name)))))

(comment
  (use 'strucjure.pattern 'clojure.pprint)
  (def eg-num
    {'num (->Or [(->View 'zero) (->View 'succ)])
     'zero 'zero
     'succ (list 'succ (->Bind (->View 'num) 'x))})
  (def eg-num-out
    (output-in eg-num
               'zero '0
               'succ '(inc x)))
  (patterns->graph eg-num-out)
  (def num-graph (eval (patterns->graph eg-num-out)))
  (def num (graph->view (trace num-graph) 'num))
  (num 'zero)
  (num 'foo)
  (num (list 'succ 'zero))
  (num (list 'succ (list 'succ 'zero)))
  (num (list 'succ (list 'succ 'succ)))
  )
