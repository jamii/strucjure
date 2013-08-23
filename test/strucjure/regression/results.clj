nil
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
#'strucjure.regression.tests/eg-num
#'strucjure.regression.tests/eg-num-out
#'strucjure.regression.tests/num
[0 nil]
nil
[1 nil]
[2 nil]
nil
[(quote 1) (quote 2) (strucjure.pattern/->Rest (strucjure.pattern/->ZeroOrMore (quote 3)))]
[1 2 #strucjure.pattern.Rest{:pattern #strucjure.pattern.ZeroOrMore{:pattern 3}}]
[1 2 #strucjure.pattern.Rest{:pattern #strucjure.pattern.Bind{:symbol x, :pattern #strucjure.pattern.ZeroOrMore{:pattern 3}}}]
[1 2 #strucjure.pattern.Rest{:pattern #strucjure.pattern.Bind{:symbol x, :pattern #strucjure.pattern.ZeroOrMore{:pattern 3}}}]
{:foo 1, :bar (#strucjure.pattern.Rest{:pattern #strucjure.pattern.ZeroOrMore{:pattern 3}})}
[1 2 #strucjure.pattern.Or{:patterns [3 4]}]
[1 2 #strucjure.pattern.Bind{:symbol x, :pattern #strucjure.pattern.View{:form foo}}]
[[1 2] nil]
[[1 2 3 3 3] nil]
[[1 2 3 3 3] (4)]
[(3 3 3) nil]
#strucjure.pattern.Or{:patterns [[(->Bind (quote succ) (->View (quote succ))) (->Bind (quote zero) (->View (quote zero)))]]}
(clojure.core/list (quote 1) (quote 2) (quote 3))
(clojure.core/list (quote succ))
#'strucjure.regression.tests/num-graph
#'strucjure.regression.tests/num
[zero nil]
[(succ (succ zero)) nil]
nil
nil