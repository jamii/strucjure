(fn [:strucjure.regression/gensym] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) [:strucjure.regression/gensym nil]))
(fn [:strucjure.regression/gensym] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) (clojure.core/let [a :strucjure.regression/gensym] [(:strucjure.regression/fn a) nil])))
(fn [:strucjure.regression/gensym] (clojure.core/when (clojure.core/seq? :strucjure.regression/gensym) (clojure.core/when (clojure.core/seq :strucjure.regression/gensym) (clojure.core/let [:strucjure.regression/gensym (clojure.core/first (clojure.core/seq :strucjure.regression/gensym))] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) (clojure.core/when (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)) (clojure.core/let [:strucjure.regression/gensym (clojure.core/first (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)))] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 2)) [(clojure.core/cons :strucjure.regression/gensym (clojure.core/cons :strucjure.regression/gensym nil)) (clojure.core/next (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)))]))))))))
[(1 2) nil]
nil
[(1 2) (3)]
nil
nil
nil
(fn [:strucjure.regression/gensym] (clojure.core/when (clojure.core/seq? :strucjure.regression/gensym) (clojure.core/when (clojure.core/seq :strucjure.regression/gensym) (clojure.core/let [:strucjure.regression/gensym (clojure.core/first (clojure.core/seq :strucjure.regression/gensym))] (clojure.core/when-let [[:strucjure.regression/gensym :strucjure.regression/gensym] (:strucjure.regression/fn :strucjure.regression/gensym)] (clojure.core/when (clojure.core/nil? :strucjure.regression/gensym) (clojure.core/when (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)) (clojure.core/let [:strucjure.regression/gensym (clojure.core/first (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)))] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 2)) [(clojure.core/cons :strucjure.regression/gensym (clojure.core/cons :strucjure.regression/gensym nil)) (clojure.core/next (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)))])))))))))
[(1 2) (3)]
[nil nil]
[nil (1 2)]
nil
[nil nil]
[nil (2)]
[(1 1) nil]
[(1 1) (2)]
[(1 2 1 2) (3)]
[(1 1 1) nil]
(fn [:strucjure.regression/gensym] (clojure.core/when (clojure.core/or (clojure.core/nil? :strucjure.regression/gensym) (clojure.core/seq? :strucjure.regression/gensym)) (clojure.core/loop [:strucjure.regression/gensym [] :strucjure.regression/gensym (clojure.core/seq :strucjure.regression/gensym)] (clojure.core/if-let [[:strucjure.regression/gensym :strucjure.regression/gensym] (clojure.core/and :strucjure.regression/gensym (clojure.core/when :strucjure.regression/gensym (clojure.core/let [:strucjure.regression/gensym (clojure.core/first :strucjure.regression/gensym)] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) [:strucjure.regression/gensym (clojure.core/next :strucjure.regression/gensym)]))))] (recur (clojure.core/conj :strucjure.regression/gensym :strucjure.regression/gensym) :strucjure.regression/gensym) [(clojure.core/seq :strucjure.regression/gensym) :strucjure.regression/gensym]))))
(fn [:strucjure.regression/gensym] (clojure.core/when (clojure.core/or (clojure.core/nil? :strucjure.regression/gensym) (clojure.core/seq? :strucjure.regression/gensym)) (clojure.core/loop [:strucjure.regression/gensym [] :strucjure.regression/gensym (clojure.core/seq :strucjure.regression/gensym)] (clojure.core/if-let [[:strucjure.regression/gensym] (clojure.core/and :strucjure.regression/gensym (clojure.core/when :strucjure.regression/gensym (clojure.core/let [:strucjure.regression/gensym (clojure.core/first :strucjure.regression/gensym)] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) [(clojure.core/next :strucjure.regression/gensym)]))))] (recur nil :strucjure.regression/gensym) [(:strucjure.regression/fn) :strucjure.regression/gensym]))))
(fn [:strucjure.regression/gensym] (clojure.core/when (clojure.core/or (clojure.core/nil? :strucjure.regression/gensym) (clojure.core/seq? :strucjure.regression/gensym)) (clojure.core/loop [:strucjure.regression/gensym [] :strucjure.regression/gensym (clojure.core/seq :strucjure.regression/gensym)] (clojure.core/if-let [[:strucjure.regression/gensym :strucjure.regression/gensym] (clojure.core/and :strucjure.regression/gensym (clojure.core/when :strucjure.regression/gensym (clojure.core/let [:strucjure.regression/gensym (clojure.core/first :strucjure.regression/gensym)] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) [:strucjure.regression/gensym (clojure.core/next :strucjure.regression/gensym)]))))] (recur (clojure.core/conj :strucjure.regression/gensym :strucjure.regression/gensym) :strucjure.regression/gensym) (clojure.core/let [a (clojure.core/seq :strucjure.regression/gensym)] [(:strucjure.regression/fn a) :strucjure.regression/gensym])))))
{:foo true}
nil
[[1 2] nil]
[[1 2] (3)]
nil
[(1 2 3) nil]
[([:foo 1] [:bar (& * 3)]) nil]
[([:foo 1] [:bar (& * 3)]) nil]
[[:foo 1] nil]
:strucjure.regression/fn
[(1 2) nil]
^{:ns #<Namespace strucjure.regression.sandbox>, :name eg-num, :file "NO_SOURCE_PATH"} #'strucjure.regression.sandbox/eg-num
^{:ns #<Namespace strucjure.regression.sandbox>, :name eg-num-out, :file "NO_SOURCE_PATH"} #'strucjure.regression.sandbox/eg-num-out
^{:ns #<Namespace strucjure.regression.sandbox>, :name num, :file "NO_SOURCE_PATH"} #'strucjure.regression.sandbox/num
[0 nil]
nil
[1 nil]
[2 nil]
nil
[(quote 1) (quote 2) (strucjure.pattern/->Rest (strucjure.pattern/->ZeroOrMore (quote 3)))]
[1 2 ^{:strucjure.pattern/rest true} #strucjure.pattern.ZeroOrMore{:pattern 3}]
[1 2 ^{:strucjure.pattern/rest true} #strucjure.pattern.Bind{:symbol x, :pattern #strucjure.pattern.ZeroOrMore{:pattern 3}}]
[1 2 ^{:strucjure.pattern/rest true} #strucjure.pattern.Bind{:symbol x, :pattern #strucjure.pattern.ZeroOrMore{:pattern 3}}]
{:foo 1, :bar (^{:strucjure.pattern/rest true} #strucjure.pattern.ZeroOrMore{:pattern 3})}
[1 2 #strucjure.pattern.Or{:patterns [3 4]}]
[1 2 #strucjure.pattern.Bind{:symbol x, :pattern #strucjure.pattern.View{:form foo}}]
[[1 2] nil]
[[1 2 3 3 3] nil]
[[1 2 3 3 3] (4)]
[1 2 #strucjure.pattern.Bind{:symbol x, :pattern #strucjure.pattern.Any{}}]
[(quote 1) (quote 2) (strucjure.pattern/->Rest (strucjure.pattern/->Bind (quote x) (strucjure.pattern/->Any)))]
[(quote 1) (quote 2) (strucjure.pattern/->Rest (strucjure.pattern/->Bind (quote x) (strucjure.pattern/->Any)))]
[(3 4) nil]
[(3 4) nil]
#strucjure.pattern.Output{:pattern [1 2 ^{:strucjure.pattern/rest true} #strucjure.pattern.Bind{:symbol rest, :pattern #strucjure.pattern.ZeroOrMore{:pattern 3}}], :fnk :strucjure.regression/fn}
(fn [:strucjure.regression/gensym] (clojure.core/when (clojure.core/vector? :strucjure.regression/gensym) (clojure.core/when (clojure.core/seq :strucjure.regression/gensym) (clojure.core/let [:strucjure.regression/gensym (clojure.core/first (clojure.core/seq :strucjure.regression/gensym))] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) (clojure.core/when (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)) (clojure.core/let [:strucjure.regression/gensym (clojure.core/first (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)))] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 2)) (clojure.core/let [:strucjure.regression/gensym (clojure.core/next (clojure.core/next (clojure.core/seq :strucjure.regression/gensym)))] (clojure.core/when (clojure.core/or (clojure.core/nil? :strucjure.regression/gensym) (clojure.core/seq? :strucjure.regression/gensym)) (clojure.core/loop [:strucjure.regression/gensym [] :strucjure.regression/gensym (clojure.core/seq :strucjure.regression/gensym)] (clojure.core/if-let [[:strucjure.regression/gensym :strucjure.regression/gensym] (clojure.core/and :strucjure.regression/gensym (clojure.core/when :strucjure.regression/gensym (clojure.core/let [:strucjure.regression/gensym (clojure.core/first :strucjure.regression/gensym)] (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 3)) [:strucjure.regression/gensym (clojure.core/next :strucjure.regression/gensym)]))))] (recur (clojure.core/conj :strucjure.regression/gensym :strucjure.regression/gensym) :strucjure.regression/gensym) (clojure.core/let [rest (clojure.core/seq :strucjure.regression/gensym)] [(:strucjure.regression/fn rest) :strucjure.regression/gensym])))))))))))))
[(3 3 3) nil]
#strucjure.pattern.Or{:patterns [[(->Bind (quote succ) (->View (quote succ))) (->Bind (quote zero) (->View (quote zero)))]]}
(clojure.core/list (quote 1) (quote 2) (quote 3))
(clojure.core/list (quote succ))
^{:ns #<Namespace strucjure.regression.sandbox>, :name num-graph, :file "NO_SOURCE_PATH"} #'strucjure.regression.sandbox/num-graph
^{:ns #<Namespace strucjure.regression.sandbox>, :name num-out, :file "NO_SOURCE_PATH"} #'strucjure.regression.sandbox/num-out
(clojure.core/let [foo (strucjure.pattern/->Bind (quote foo) (strucjure.pattern/->View (quote foo)))] (strucjure.graph/with-named-nodes {(quote foo) (strucjure.sugar/pattern (clojure.core/unquote foo))}))
^{:ns #<Namespace strucjure.regression.sandbox>, :name num, :file "NO_SOURCE_PATH"} #'strucjure.regression.sandbox/num
[0 nil]
[2 nil]
nil
nil
(trace-pattern [1 2 3] [1 2 3])
(fn [:strucjure.regression/gensym] (clojure.core/binding [strucjure.debug/*depth* 0] (clojure.core/let [_ (:strucjure.regression/fn (quote "[1 2 3]") :strucjure.regression/gensym) :strucjure.regression/gensym (clojure.core/when (clojure.core/vector? :strucjure.regression/gensym) (clojure.core/when (clojure.core/>= (clojure.core/count :strucjure.regression/gensym) 3) (clojure.core/let [:strucjure.regression/gensym (clojure.core/nth :strucjure.regression/gensym 0)] (clojure.core/let [_ (:strucjure.regression/fn (quote "1") :strucjure.regression/gensym) :strucjure.regression/gensym (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) (clojure.core/let [:strucjure.regression/gensym (clojure.core/nth :strucjure.regression/gensym 1)] (clojure.core/let [_ (:strucjure.regression/fn (quote "2") :strucjure.regression/gensym) :strucjure.regression/gensym (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 2)) (clojure.core/let [:strucjure.regression/gensym (clojure.core/nth :strucjure.regression/gensym 2)] (clojure.core/let [_ (:strucjure.regression/fn (quote "3") :strucjure.regression/gensym) :strucjure.regression/gensym (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 3)) [[:strucjure.regression/gensym :strucjure.regression/gensym :strucjure.regression/gensym] (clojure.core/seq (clojure.core/subvec :strucjure.regression/gensym 3))]) _ (:strucjure.regression/fn (quote "3") :strucjure.regression/gensym)] :strucjure.regression/gensym))) _ (:strucjure.regression/fn (quote "2") :strucjure.regression/gensym)] :strucjure.regression/gensym))) _ (:strucjure.regression/fn (quote "1") :strucjure.regression/gensym)] :strucjure.regression/gensym)))) _ (:strucjure.regression/fn (quote "[1 2 3]") :strucjure.regression/gensym)] :strucjure.regression/gensym)))
[[1 2 3] nil]
[1 2 3]
(fn [:strucjure.regression/gensym] (clojure.core/binding [strucjure.debug/*depth* 0] (clojure.core/let [_ (:strucjure.regression/fn (quote "[1 2 3]") :strucjure.regression/gensym) :strucjure.regression/gensym (clojure.core/when (clojure.core/vector? :strucjure.regression/gensym) (clojure.core/when (clojure.core/>= (clojure.core/count :strucjure.regression/gensym) 3) (clojure.core/let [:strucjure.regression/gensym (clojure.core/nth :strucjure.regression/gensym 0)] (clojure.core/let [_ (:strucjure.regression/fn (quote "1") :strucjure.regression/gensym) :strucjure.regression/gensym (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 1)) (clojure.core/let [:strucjure.regression/gensym (clojure.core/nth :strucjure.regression/gensym 1)] (clojure.core/let [_ (:strucjure.regression/fn (quote "2") :strucjure.regression/gensym) :strucjure.regression/gensym (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 2)) (clojure.core/let [:strucjure.regression/gensym (clojure.core/nth :strucjure.regression/gensym 2)] (clojure.core/let [_ (:strucjure.regression/fn (quote "3") :strucjure.regression/gensym) :strucjure.regression/gensym (clojure.core/when (clojure.core/= :strucjure.regression/gensym (quote 3)) [[:strucjure.regression/gensym :strucjure.regression/gensym :strucjure.regression/gensym] (clojure.core/seq (clojure.core/subvec :strucjure.regression/gensym 3))]) _ (:strucjure.regression/fn (quote "3") :strucjure.regression/gensym)] :strucjure.regression/gensym))) _ (:strucjure.regression/fn (quote "2") :strucjure.regression/gensym)] :strucjure.regression/gensym))) _ (:strucjure.regression/fn (quote "1") :strucjure.regression/gensym)] :strucjure.regression/gensym)))) _ (:strucjure.regression/fn (quote "[1 2 3]") :strucjure.regression/gensym)] :strucjure.regression/gensym)))