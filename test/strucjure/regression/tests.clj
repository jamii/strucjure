;; (in-ns 'strucjure.regression.sandbox)
;; (set! *print-meta* true)
((eval (pattern->view (->Guard 1 (fnk [] true)) true true)) 1)
((eval (pattern->view (list 1 2 3) true true)) (list 1 2 3))
((eval (pattern->view (list 1 2 3) true true)) (list 1 2 3 4))
((eval (pattern->view (list 1 2 3) true true)) (list 1 2))
(pattern->view (->Name 'a 1) true true)
(bound (->Name 'a 1))
(used (->Output (->Name 'a 1) (fnk [a] (clojure.core/+ a 1))))
((eval (pattern->view (->Output (->Name 'a 1) (fnk [a] (clojure.core/+ a 1))) true true)) 1)
((eval (pattern->view (->Output (->Name 'a 1) (fnk [a] (clojure.core/+ a 1))) true true)) 2)
(pattern->view (list 1 2) true true)
((eval (pattern->view (list 1 2) true true)) (list 1 2))
((eval (pattern->view (list 1 2) true true)) (list 1))
((eval (pattern->view (list 1 2) true true)) (list 1 2 3))
((eval (pattern->view (list 1 2) true true)) (list 1 3))
((eval (pattern->view (list 1 2) true true)) [1 2])
((eval (pattern->view (list) true true)) (list))
((eval (pattern->view (list) true true)) (list 1 2))
((eval (pattern->view (list) true true)) nil)
((eval (pattern->view (zero-or-more 1) true true)) (list))
((eval (pattern->view (zero-or-more 1) true true)) (list 2))
((eval (pattern->view (zero-or-more 1) true true)) (list 1 1))
((eval (pattern->view (zero-or-more 1) true true)) (list 1 1 2))
((eval (pattern->view (zero-or-more (->Or [1 2])) true true)) (list 1 2 1 2 3))
((eval (pattern->view (zero-or-more (->Rest (list 1))) true true)) (list 1 1 1))
(pattern->view (zero-or-more 1) true true)
(pattern->view (->Output (zero-or-more 1) (fnk [] 'ones)) true true)
(pattern->view (->Output (->Name 'a (zero-or-more 1)) (fnk [a] a)) true true)
(let [[out rem] ((eval (pattern->view (->WithMeta (->Any) {:foo true}) true true)) ^:foo [])]
  (meta out))
((eval (pattern->view [1 2] true true)) [1])
((eval (pattern->view [1 2] true true)) [1 2])
((eval (pattern->view [1 2] true true)) [1 2 3])
((eval (pattern->view [1 2] true true)) [1 3])
((eval (pattern->view (list (->Rest (->Name 'elems (zero-or-more (->Any))))) true true)) (list 1 2 3))
((eval (pattern->view (->Seqable [(->Rest (zero-or-more [(->Any) (->Any)]))]) true true)) '{:foo 1 :bar (& * 3)})
((eval (pattern->view (->And [{} (->Name 'elems (->Seqable [(->Rest (zero-or-more [(->Any) (->Any)]))]))]) true true)) '{:foo 1 :bar (& * 3)})
((eval (pattern->view [(->Any) (->Any)] true true)) (first (seq '{:foo 1 :bar (& * 3)})))
((eval (pattern->view (->Output (->Name 'a (list 1 (->Name 'a 2))) (fnk [a] [:a a])) true true)) (list 1 2))
(def eg-num
  {'num (->Or [(->Node 'zero) (->Node 'succ)])
   'zero 'zero
   'succ (list 'succ (->Name 'x (->Node 'num)))})
(def eg-num-out
  (output-in eg-num
             'zero (fnk [] 0)
             'succ (fnk [x] (inc x))))
(def num (eval (graph->view 'num eg-num-out)))
(num 'zero)
(num 'foo)
(num (list 'succ 'zero))
(num (list 'succ (list 'succ 'zero)))
(num (list 'succ (list 'succ 'succ)))
(named-node (->Node 'zero))
(postwalk (->Node 'zero) named-node)
(with-named-inner-nodes eg-num)
(pattern->view (->Is #(symbol? %)) true true)
(macroexpand-1 '(pattern [1 2 & * 3]))
(pattern [1 2 & * 3])
((eval (pattern->view (zero-or-more 3) true true)) [])
((eval (pattern->view (pattern [& * 3]) true true)) [])
(pattern [1 2 ^x & * 3])
(pattern [1 2 & ^x * 3])
(pattern {:foo 1 :bar (& * 3)})
(pattern [1 2 ~(or 3 4)])
(pattern [1 2 ^x ~(->Node 'foo)])
(macroexpand '(view [1 2 & * 3]))
((view-with [] (pattern [1 2 & * 3])) [1 2])
((view-with [] (pattern [1 2 & * 3])) [1 2 3 3 3])
((view-with [] (pattern [1 2 & * 3])) [1 2 3 3 3 4])
((view [1 2 & * 3]) [1 2 3 3 3])
((view [1 2 & * 3]) [1 2 3 3 3 4])
(pattern [1 2 ^x _])
(macroexpand '(pattern [1 2 ^x & _]))
(macroexpand '(pattern [1 2 & ^x _]))
((view ~(output [1 2 ^x & _] (fnk [x] x))) [1 2 3 4])
((view ~(output [1 2 & ^x _] (fnk [x] x))) [1 2 3 4])
(pattern ~(output [1 2 ^rest & * 3] (fnk [rest] rest)))
(pattern->view (pattern ~(output [1 2 ^rest & * 3] (fnk [rest] rest))) true true)
((view ~(output [1 2 ^rest & * 3] (fnk [rest] rest))) [1 2 3 3 3])
(pattern ~(or [(->Name 'succ (->View 'succ)) (->Name 'zero (->View 'zero))]))
(macroexpand-1 '(pattern (1 2 3)))
(macroexpand-1 '(pattern (succ)))
(def num-graph
  (graph
   num ~(or ~succ ~zero)
   succ (succ ^x ~num)
   zero zero))
(def num-out
  (output-in num-graph
             'zero (fnk [] 0)
             'succ (fnk [x] (inc x))))
(macroexpand-1 '(graph foo ~foo))
(def num (eval (graph->view 'num num-out)))
(num 'zero)
(num '(succ (succ zero)))
(num '(1 (succ zero)))
(num '(succ succ))
(macroexpand-1 '(trace [1 2 3]))
((trace [1 2 3]) [1 2 3])
((trace ~(or {:1 2 :3 [4 5]} {:1 2})) {:1 2 :3 [3 5]})
(macroexpand-1 '(view ~(->NodeOf num-out 'num)))
(graph->view 'num num-out)
((view ~(->NodeOf num-out 'num)) '(succ (succ zero)))
(macroexpand '(match 1 1 :ok))
(match 1 1 :ok)
(match 1 2 :not-ok)
(match 1 2 :not-ok 1 :ok)
(match [1 2] [1 2 3] :not-ok)
(match [1 2] [1 2 & _] :ok)
(match [1 2 3 4] [1 2 & _] :ok)
(def x [1 2])
(match [1 2] ~x :ok)
(match [1 2 3] ~x :not-ok)
(match [1 2 3] [1 2 ^x _] x)
(match [1 2 3] [1 2 ^x & _] x)
(pattern [1 2 ~(? 3)])
(view [1 2 ~(? 3)])
(match [1 2 3] [1 2 & ~(? ^x _)] x)
(match [1 2] [1 2 & ~(? ^x _)] x)
((view [& * 1 & ? 2 3]) [1 1 1 2 3])
((view-with [] (pattern [& * 1 & ? 2 3])) [1 1 1 3])
((view-with [] (pattern [& * 1 & ? 2 3])) [1 1 1 2 2 3])
((view-with [] (pattern [& * 1  & ? 2  3])) [1 1 1])
(with-layers [with-depth trace-all] ((*view* (pattern [& * 1  & ? 2  3])) [1 1 1 3]))
