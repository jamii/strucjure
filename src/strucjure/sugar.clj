(ns strucjure.sugar
  (:require [strucjure.pattern :as pattern :refer [->Rest ->Seqable ->Any ->Is ->Guard ->Bind ->Output ->Or ->And ->ZeroOrMore ->WithMeta ->View]]
            [strucjure.graph :as graph]))

;; TODO graph sugar is just (let [foo (->Bind (->View 'foo) 'foo)] (pattern ...))

(comment
  (def pattern-pattern
    (s/graph
     pattern ~(s/with-meta ~unbound-pattern {:tag ~binding})
     unbound-pattern ~(s/or ~unquote ~seq ~vec ~map ~binding ~any ~symbol _)
     unquote (~`unquote _)
     seq (& ^elems * & elem)
     vec [& ^elems * & elem]
     elem ~(s/or (~pattern) ~parser ~rest)
     parser (~(s/with-meta ^prefix ~(s/or ~'* ~'+ ~'?) {:tag ~binding}) ~pattern)
     rest (~(s/with-meta ~'& {:tag ~binding}) ~pattern)
     map ~(s/and {} ^elems ~(s/seqable & * (~key ~pattern)))
     key ~(s/is keyword? &input)
     binding ~(s/is symbol? &input)
     any ~'_
     symbol ~(s/is symbol? &input))))

(def pattern-patterns ;D
  (letfn [(view [sym] (->Bind sym (->View sym)))
          (bindable [pattern] (->WithMeta pattern (->Or [{:tag (->Or [(view 'binding) nil])} nil])))]
    {'pattern (bindable (->Or [(view 'unquote) (view 'seq) (view 'vec) (view 'map) (view 'binding) (view 'any) (view 'symbol) (->Any)]))
     'unquote (list `unquote (->Any))
     'seq (list (->Rest (->Bind 'elems (->ZeroOrMore (->Rest (view 'elem))))))
     'vec (vector (->Rest (->Bind 'elems (->ZeroOrMore (->Rest (view 'elem))))))
     'elem (->Or [(view 'parser) (view 'rest) (list (view 'pattern))])
     'parser (list (bindable (->Bind 'prefix (->Or ['* '+ '?]))) (->Rest (view 'elem)))
     'rest (list (bindable '&) (->Rest (view 'elem)))
     'map (->And [{} (->Bind 'elems (->Seqable [(->Rest (->ZeroOrMore [(->Any) (view 'pattern)]))]))])
     'binding (->Is `(symbol? ~'&input))
     'any '_
     'symbol (->Is `(symbol? ~'&input))}))

(comment
  (pattern/pattern->clj (->Is `(symbol? &input)) 'input #{} (fn [_ _] 'body))
  (doseq [[_ pattern] pattern-patterns ])
  (def desugar (graph/graph->view (graph/trace (eval (graph/patterns->graph pattern-patterns))) 'pattern))
  (desugar '[1 2 & * 3])
  (desugar '{:foo 1 :bar (& * 3)})
)

(comment
  (def desugar-pattern
    (s/update-graph pattern-pattern
                    unquote (s/=> ~% (_ ?body) ~body)
                    parser (s/=> ~% (?action ?sub-pattern) `(~(in-raw action) ~sub-pattern))
                    binding (s/=> ~% (->Bind (nullable? &output) (binding-name &output)))
                    symbol (s/=> ~% `'~&output))))
