(ns strucjure.sugar
  (:require [plumbing.core :refer [for-map]]
            [strucjure.util :refer [let-syms]]
            [strucjure.pattern :as pattern :refer [->Rest ->Seqable ->Any ->Is ->Guard ->Bind ->Output ->Or ->And ->ZeroOrMore ->WithMeta ->View]]
            [strucjure.graph :as graph]))

;; TODO graph sugar is just (let [foo (->Bind (->View 'foo) 'foo)] (pattern ...))
;; TODO wrapping parser/rest in [] and calling first (elem) is ugly

(defn with-named-nodes [name->pattern]
  (with-meta
    (for-map [[name pattern] name->pattern] name (->Bind name pattern))
    (meta name->pattern)))

(def sugar-patterns ;D
  (letfn [(view [sym] (->Bind sym (->View sym)))
          (bindable [pattern] (->WithMeta pattern (->Or [{:tag (->Or [(view 'binding) (->Bind 'binding nil)])} (->Bind 'binding nil)])))]
    {'pattern (bindable (->Or [(view 'unquote) (view 'seq) (view 'vec) (view 'map) (view 'binding) (view 'any) (->Any)]))
     'unquote (list `unquote (->Bind 'unquoted (->Any)))
     'seq (list (->Rest (->Bind 'elems (->ZeroOrMore (->Rest (view 'elem))))))
     'vec (vector (->Rest (->Bind 'elems (->ZeroOrMore (->Rest (view 'elem))))))
     'elem (->Or [(view 'parser) (view 'rest) (list (view 'pattern))])
     'parser (list (bindable (->Bind 'prefix (->Or ['*]))) (->Rest (view 'elem))) ;; TODO + ?
     'rest (list (bindable '&) (->Rest (view 'elem)))
     'map (->And [{} (->Bind 'elems (->Seqable [(->Rest (->ZeroOrMore [(->Any) (view 'pattern)]))]))])
     'binding (->Is `(symbol? ~'&input))
     'any '_}))

(def prefixes
  {'* pattern/->ZeroOrMore
   '& pattern/->Rest})

(def desugar-patterns
  (graph/output-in (with-named-nodes sugar-patterns)
                   'pattern '(if binding `(->Bind '~binding ~pattern) pattern)
                   'unquote 'unquoted
                   'parser '[(let [parser `(~(prefixes prefix) ~(first elem))]
                               (if binding `(->Bind '~binding ~parser) parser))]
                   'rest '[`(pattern/->Rest ~(if binding `(->Bind '~binding ~(first elem)) (first elem)))]
                   'any '`(pattern/->Any)))

;; TODO error reporting here
(defn desugar [form]
  (let [desugar-view (graph/graph->view (eval (graph/patterns->graph desugar-patterns)) 'pattern)]
    (if-let [[output remaining] (desugar-view form)]
      (if (nil? remaining)
        output
        (throw (Exception. "Not a pattern")))
      (throw (Exception. "Not a pattern")))))

(defmacro pattern [form]
  (desugar form))

(defmacro view
  ([form]
     (pattern/pattern->view (eval (desugar form))))
  ([form input]
     (let-syms [input-sym]
               `(let [~input-sym ~input]
                  ~(pattern/pattern->clj (eval (desugar form)) input-sym #{:output}
                                         (fn [output remaining] [output remaining]))))))

(comment
  (def desugar (graph/graph->view (graph/trace (eval (graph/patterns->graph desugar-patterns))) 'pattern))
  (pattern [1 2 & * 3])
  (pattern [1 2 ^x & * 3])
  (pattern [1 2 & ^x * 3])
  (pattern {:foo 1 :bar (& * 3)})
  (pattern [1 2 ~(or 3 4)])
  (pattern [1 2 ^x ~(->View 'foo)])
  (view [1 2 & * 3] [1 2])
  (view [1 2 & * 3] [1 2 3 3 3])
  (view ~(->Output (pattern [1 2 ^rest & * 3]) 'rest) [1 2 3 3 3])
)

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

(comment
  (def desugar-pattern
    (s/update-graph pattern-pattern
                    unquote (s/=> ~% (_ ?body) ~body)
                    parser (s/=> ~% (?action ?sub-pattern) `(~(in-raw action) ~sub-pattern))
                    binding (s/=> ~% (->Bind (nullable? &output) (binding-name &output)))
                    symbol (s/=> ~% `'~&output))))
