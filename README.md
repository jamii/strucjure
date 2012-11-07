Pattern-matching, parsing and generic traversals through the medium of [PEGs](http://en.wikipedia.org/wiki/Parsing_expression_grammar).

Leiningen couldn't handle the irony.

## Installation

```clojure
[strucjure "0.3.1"]
```

```clojure
(:use strucjure)
```

## TODO

Strucjure is still unstable and may change significantly in future versions.

Strucjure does not yet support:

 * Set literals or record literals

 * Informative error messages / traces

 * Memoisation

 * Ad-hoc extension

## Limitations

 * The current implementation (since 0.3.0) is entirely interpreted and for simple patterns will be significantly slower than the equivalent hand-written code.

 * Strucjure cannot automatically rewrite left-recursive rules. You have to manually transform your grammars into a non-left-recursive form.

 * Views retain all of their input until finishing. This makes them unsuitable for writing streaming parsers

## Concepts

The core constructs in strucjure are patterns and views.

A pattern takes an input and a set of bindings, consumes some or all of the input and returns new bindings. Patterns implement the strucjure.pattern/Pattern protocol. The pattern macro implements a small DSL for constructing patterns (detailed in the Patterns section below).

```clojure
user> (pattern ?n)
#strucjure.pattern.Bind{:symbol n}
user> (pattern number?)
#strucjure.pattern.Guard{:fun #<user$eval2102$fn__2103 user$eval2102$fn__2103@46cfd22a>}
user> (pattern (and number? ?n))
#strucjure.pattern.And{:patterns [#strucjure.pattern.Guard{:fun #<user$eval2107$fn__2108 user$eval2107$fn__2108@4ca4f040>} #strucjure.pattern.Bind{:symbol n}]}
user> (pattern [?x '+ ?y])
#strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value +}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol y}}]}}}
```

A view takes an input, consumes some or all of the input and returns a value. Views implement the strucjure.view/View protocol. All patterns are also views. The view macro constructs a view from a list of [pattern value] pairs, where the value forms have access to the patterns bindings eg

```clojure
user> (view
        (and number? ?n) n
        [?x '+ ?y] (+ x y)
        [?x '* ?y] (* x y))
#strucjure.view.Or{:views [#strucjure.view.Match{:pattern #strucjure.pattern.And{:patterns [#strucjure.pattern.Guard{:fun #<user$eval2183$fn__2184 user$eval2183$fn__2184@6e1b0caf>} #strucjure.pattern.Bind{:symbol n}]}, :result-fun #<user$eval2183$fn__2186 user$eval2183$fn__2186@611c4041>} #strucjure.view.Match{:pattern #strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value +}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol y}}]}}}, :result-fun #<user$eval2183$fn__2188 user$eval2183$fn__2188@63f5acd0>} #strucjure.view.Match{:pattern #strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value *}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol y}}]}}}, :result-fun #<user$eval2183$fn__2190 user$eval2183$fn__2190@4674d22e>}]}
```

Views are executed with strucjure/run:

```clojure
user> (run (pattern ?x) 'hello)
{x hello}
user> (run (pattern [?x '+ ?y]) '[1 + 2])
{y 2, x 1}
user> (run (pattern [?x '+ ?y]) '(1 + 2))
{y 2, x 1}
user> (run (pattern [?x '+ ?y]) '(1 * 2))
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value +}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol y}}]}}}, :input (1 * 2)}  strucjure.view/run-or-throw (view.clj:38)
user> (defview calculate
        (and number? ?n) n
        [?x '+ ?y] (+ x y)
        [?x '* ?y] (* x y))
#'user/calculate
user> (run calculate '(1 + 2))
3
```

Views executed with run will throw an exception if fail to match or if they do not consume all their input. You can get access to the underlying result with strucjure.view/run.

```clojure
user> (run calculate '(1 * 2))
2
user> (run calculate '(1 / 2))
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.view.Or{:views [#strucjure.view.Match{:pattern #strucjure.pattern.And{:patterns [#strucjure.pattern.Guard{:fun #<user$fn__3736 user$fn__3736@464242a9>} #strucjure.pattern.Bind{:symbol n}]}, :result-fun #<user$fn__3738 user$fn__3738@79feea8f>} #strucjure.view.Match{:pattern #strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value +}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol y}}]}}}, :result-fun #<user$fn__3740 user$fn__3740@2db5424e>} #strucjure.view.Match{:pattern #strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value *}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol y}}]}}}, :result-fun #<user$fn__3742 user$fn__3742@4f7d24b6>}]}, :input (1 / 2)}  strucjure.view/run-or-throw (view.clj:30)
user> (require '[strucjure.view :as view])
nil
user> (view/run calculate '(1 * 2) {})
[nil 2]
user> (view/run calculate '(1 / 2) {})
nil
```

Views are first-class objects and can be used in other views or even recursively call themselves.

```clojure
user> (defview calculate
        (and number? ?n) n
        [(calculate ?x) '+ (calculate ?y)] (+ x y)
        [(calculate ?x) '* (calculate ?y)] (* x y))
#'user/calculate
user> (run calculate '((1 * 2) + (3 * (4 + 5))))
29
```

Views can also be created and used in-place with match.

```clojure
user> (def x {:a 1 :b 2 :c 3})
#'user/x
user> (match x
             {:a 3} 'not-going-to-happen
             {:a 1 :c ?c} c)
3
```

## Patterns

The symbol _ matches anything.

```clojure
user> (run (pattern _) 1)
{}
```

Primitives (nil, true, false, numbers, keywords, characters and strings) create patterns which match themselves.

```clojure
user> (run (pattern 1) 1)
{}
user> (run (pattern 2) 1)
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Literal{:value 2}, :input 1}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern "foo") "foo")
{}
```

Quoted forms match their quoted self.

```clojure
user> (run (pattern '(+ 1 2)) '(+ 1 2))
{}
user> (run (pattern 'vector?) vector?)
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Literal{:value vector?}, :input #<core$vector clojure.core$vector@cd3509c>}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern 'vector?) 'vector?)
{}
```

Symbols beginning with ? match anything and create a new binding.

```clojure
user> (run (pattern ?x) 1)
{x 1}
user> (run (pattern ?x) [1 2 3])
{x [1 2 3]}
```

Repeated uses of the same binding must match the same value.

```clojure
user> (run (pattern [?x ?x]) [1 2])
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Guard{:fun #<user$eval8319$fn__8321 user$eval8319$fn__8321@463c4bca>}}]}}}, :input [1 2]}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern [?x ?x]) [1 1])
{x 1}
```

Anonymous functions and symbols ending in ? are applied to the input and match if they return a truthy value.

```clojure
user> (run (pattern vector?) [1 2 3])
{}
user> (run (pattern string?) [1 2 3])
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Guard{:fun #<user$eval6476$fn__6478 user$eval6476$fn__6478@284876e0>}, :input [1 2 3]}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern #(> (count %) 3)) [1 2 3])
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Guard{:fun #<user$eval6493$fn__6495 user$eval6493$fn__6495@507792fe>}, :input [1 2 3]}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern #(> (count %) 2)) [1 2 3])
{}
```

Classes match instances of that class.

```clojure
user> (run (pattern java.lang.String) "foo")
{}
user> (run (pattern java.lang.Object) "foo")
{}
user> (run (pattern java.lang.Appendable) "foo")
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Guard{:fun #<user$eval6642$fn__6644 user$eval6642$fn__6644@2c9f3eba>}, :input "foo"}  strucjure.view/run-or-throw (view.clj:38)
```

Any other symbols are assumed to be externally bound and match against their value.

```clojure
user> (let [easy-arith '(+ 1 2)]
         (run (pattern easy-arith) '(+ 1 2)))
{}
```

Regular expressions call re-find on the input and match if the result is not nil.

```clojure
user> (run (pattern #"foo") "foo")
{}
user> (run (pattern #"foo") "foobear")
{}
user> (run (pattern #"foo\b") "foobear")
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Regex{:regex #"foo\b"}, :input "foobear"}  strucjure.view/run-or-throw (view.clj:38)
```

Maps lookup their keys and, if the key is found, match against the associated pattern.

```clojure
user> (run (pattern {:a 1 :b ?x}) {:a 1})
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Map{:keys&patterns [[:a #strucjure.pattern.Literal{:value 1}] [:b #strucjure.pattern.Bind{:symbol x}]]}, :input {:a 1}}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern {:a 1 :b ?x}) {:a 2 :b 1})
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Map{:keys&patterns [[:a #strucjure.pattern.Literal{:value 1}] [:b #strucjure.pattern.Bind{:symbol x}]]}, :input {:a 2, :b 1}}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern {:a 1 :b ?x}) {:a 1 :b 2})
{x 2}
```

If the key is not found then even nil patterns will not match.

```clojure
user> (run (pattern {:a nil}) {:b 2})
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Map{:keys&patterns [[:a #strucjure.pattern.Literal{:value nil}]]}, :input {:b 2}}  strucjure.view/run-or-throw (view.clj:38)
```

Map patterns do not fail if other keys are present too.

```clojure
user> (run (pattern {:a 1 :b ?x}) {:a 1 :b 2 :c 3})
{x 2}
```

Calls to class constructors will match instances of the class and additionally call clojure.core/vals to match subpatterns. This is intended for use with records but will work on any class that supports callings clojure.core/vals.

```clojure
user> (defrecord Foo [bear])
user.Foo
user> (run (pattern (Foo. ?bear)) (Foo. "imabear!"))
{bear "imabear!"}
user> (defrecord Bar [bear])
user.Bar
user> (run (pattern (Foo. ?bear)) (Bar. "imabear!"))
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Record{:class-name user.Foo, :patterns [#strucjure.pattern.Bind{:symbol bear}]}, :input #user.Bar{:bear "imabear!"}}  strucjure.view/run-or-throw (view.clj:38)
```

Record literals will eventually be supported.

Vectors match any input which is an instance of clojure.lang.Seqable.

```clojure
user> (run (pattern [1 2 ?x]) [1 2 3])
{x 3}
user> (run (pattern [1 2 ?x]) (list 1 2 3))
{x 3}
```

Unfortunately strings are not instances of clojure.lang.Seqable. Vector patterns may be expanded in future version to match strings too.

```clojure
user> (run (pattern [\a \b ?x]) "abc")
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value \a}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value \b}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}}]}}}, :input "abc"}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern [\a \b ?x]) (seq "abc"))
{x \c}
```

Inside a vector you can use '& pattern' to apply a pattern to the rest of the sequence, rather than to the next element.

```clojure
user> (run (pattern [1 2 & ?x]) [1 2 3])
{x (3)}
user> (run (pattern [1 2 & ?x]) [1 2 3 4])
{x (3 4)}
user> (run (pattern [1 2 & ?x]) [1 2])
{x nil}
```

The special form 'prefix' behaves much like a vector but is allowed to match only the beginning of a sequence. This is useful when chaining parsers together (see eg the arith macro in the examples section below).

```clojure
user> (run (pattern (prefix 1 2)) [1 2])
{}
user> (run (pattern (prefix 1 2)) [1 2 3])
ExceptionInfo throw+: #strucjure.view.PartialMatch{:view #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value 1}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value 2}}]}}, :input [1 2 3], :remaining (3), :output {}}  strucjure.view/run-or-throw (pattern.clj:28)
user> (require '[strucjure.pattern :as pattern])
nil
user> (pattern/run (pattern (prefix 1 2)) [1 2 3] {} {})
[(3) {}]
```

The special forms 'and', 'or' and 'not' combine patterns much as you would expect. To simplifiy the semantics, 'or' patterns must create the same set of bindings in every branch (eg what should [(or ?x 1) ?x] match?).

```clojure
user> (run (pattern (not integer?)) [2 2])
{}
user> (run (pattern (not integer?)) "foo")
{}
user> (run (pattern (not integer?)) 1)
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Not{:pattern #strucjure.pattern.Guard{:fun #<user$eval8595$fn__8597 user$eval8595$fn__8597@3db625b7>}}, :input 1}  strucjure.view/run-or-throw (view.clj:38)
user> (run (pattern (and string? ?x)) "foo")
{x "foo"}
user> (run (pattern (or [1 ?x] [?x 1])) [1 2])
{x 2}
user> (run (pattern (or [1 ?x] [?x 1])) [2 1])
{x 2}
user> (run (pattern (or [1 ?x] [?x 1])) [2 2])
ExceptionInfo throw+: #strucjure.view.NoMatch{:view #strucjure.pattern.Or{:patterns [#strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value 1}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}}]}}} #strucjure.pattern.Total{:pattern #strucjure.pattern.Seq{:pattern #strucjure.pattern.Chain{:patterns [#strucjure.pattern.Head{:pattern #strucjure.pattern.Bind{:symbol x}} #strucjure.pattern.Head{:pattern #strucjure.pattern.Literal{:value 1}}]}}}]}, :input [2 2]}  strucjure.view/run-or-throw (view.clj:38)
```

Any other function call should be of the form (view* pattern). This calls an external view on the current input and matches the output against the pattern.

```clojure
user> (defnview my-zero-or-more [elem]
        (prefix (elem ?x) & ((my-zero-or-more elem) ?xs)) (cons x xs)
        (prefix) nil)
#'user/my-zero-or-more
user> (defview one
        1 'one)
#'user/one
user> (defview two
        2 'two)
#'user/two
user> (defpattern ones&twos
        [& ((my-zero-or-more one) ?ones) & ((my-zero-or-more two) ?twos)])
#'user/ones&twos
user> (run ones&twos [])
{twos nil, ones nil}
user> (run ones&twos [1 1 1 1 2])
{twos (two), ones (one one one one)}
```

## Examples

Operations on red-black trees:

```clojure
(defrecord Leaf [])
(defrecord Red [value left right])
(defrecord Black [value left right])

;; A red-black tree is balanced when:
;; * every path from root to leaf has the same number of black nodes
;; * no red node has red children

;; Matches balanced trees and returns the number of black nodes per path
(defview balanced-height
  Leaf 0
  (and (Black. _ (balanced-height ?l) (balanced-height ?r)) #(= l r)) (+ 1 l)
  (and (Red. _ (and (not Red) (balanced-height ?l)) (and (not Red) (balanced-height ?r))) #(= l r)) l)

(defn balanced? [tree]
  (matches? balanced-height tree))

;; Balance operation from http://www.cs.cornell.edu/courses/cs3110/2009sp/lectures/lec11.html
(defview balance
  (Black. ?z (Red. ?y (Red. ?x ?a ?b) ?c) ?d)
  (Red. y (Black. x a b) (Black. z c d))

  (Black. ?z (Red. ?x ?a (Red. ?y ?b ?c)) ?d)
  (Red. y (Black. x a b) (Black. z c d))

  (Black. ?x ?a (Red. ?z (Red. ?y ?b ?c)?d))
  (Red. y (Black. x a b) (Black. z c d))

  (Black. ?x ?a (Red. ?y ?b (Red. ?z ?c ?d)))
  (Red. y (Black. x a b) (Black. z c d))

  ?other
  other)
```

A recursive descent parser with operator precedence:

```clojure
user> (declare math-value math-mult-div math-plus-minus)
#'user/math-plus-minus
user> (def reserved? #{'* '/ '+ '-})
#'user/reserved?
user> (defview math-value
  (prefix [(math-plus-minus ?x)]) x
  (prefix (and (not reserved?) ?n)) n)
#'user/math-value
user> (defview math-mult-div
  (prefix & (math-value ?x) '* & (math-mult-div ?y)) `(~'* ~x ~y)
  (prefix & (math-value ?x) '/ & (math-mult-div ?y)) `(~'/ ~x ~y)
  (prefix & (math-value ?x)) x)
#'user/math-mult-div
user> (defview math-plus-minus
  (prefix & (math-mult-div ?x) '+ & (math-plus-minus ?y)) `(~'+ ~x ~y)
  (prefix & (math-mult-div ?x) '- & (math-plus-minus ?y)) `(~'- ~x ~y)
  (prefix & (math-mult-div ?x)) x)
#'user/math-plus-minus
user> (defmacro math [& tokens]
  (run math-plus-minus tokens))
#'user/math
user> (macroexpand '(math 1 + 2 * 7 + 1 / 2))
(+ 1 (+ (* 2 7) (/ 1 2)))
user> (macroexpand '(math 1 + 2 * (7 + 1) / 2))
(+ 1 (* 2 (/ (7 + 1) 2)))
```

The syntax of strucjure itself is self-defined using views.

```clojure
(defview parse-key&pattern-ast
  [?key (parse-pattern-ast ?pattern)] [key pattern])

(defview parse-import
  [?view (parse-pattern-ast ?pattern)] (view/->Import view pattern)
  [?view & (parse-import ?import)] (view/->Import view import))

(defview parse-seq-pattern-ast
  ;; & PATTERNS
  (prefix '& (parse-pattern-ast ?pattern)) pattern

  ;; ALL OTHER PATTERNS
  (prefix (parse-pattern-ast ?pattern)) (->Head pattern))

(defview parse-pattern-ast
  ;; BINDINGS
  '_ (->Ignore)
  (and binding? ?binding) (->Bind (binding-name binding))

  ;; LITERALS
  (and primitive? ?primitive) (->Literal primitive) ; primitives evaluate to themselves, so don't need quoting
  (and class-name? ?class-name) (->Guard `(instance? ~class-name ~util/input-sym))
  (and (or clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap) [& ((view/zero-or-more parse-key&pattern-ast) ?keys&patterns)])
    (->Map keys&patterns)
  (and seq? [(and constructor? ?constructor) & ((view/zero-or-more parse-pattern-ast) ?arg-patterns)])
    (->Record (constructor-name constructor) arg-patterns)

  ;; PREDICATES
  (and java.util.regex.Pattern ?regex) (->Regex regex)
  (and predicate? ?predicate) (->Guard `(~predicate ~util/input-sym))
  (and seq? [(or 'fn 'fn*) [] & ?body]) (->Guard `(do ~@body))
  (and seq? [(or 'fn 'fn*) [?arg] & ?body]) (->Guard `(do ~@(clojure.walk/prewalk-replace {arg util/input-sym} body)))

  ;; SEQUENCES
  (and vector? [& ((view/zero-or-more-prefix parse-seq-pattern-ast) ?seq-patterns)]) (apply seqable seq-patterns)
  (and seq? ['prefix & ((view/zero-or-more-prefix parse-seq-pattern-ast) ?seq-patterns)]) (apply prefix seq-patterns)

  ;; SPECIAL FORMS
  (and seq? ['quote ?quoted]) (->Literal `(quote ~quoted))
  (and seq? ['and & ((two-or-more parse-pattern-ast) ?patterns)]) (->And patterns)
  (and seq? ['or & ((two-or-more parse-pattern-ast) ?patterns)]) (->Or patterns)
  (and seq? ['not (parse-pattern-ast ?pattern)]) (->Not pattern)

  ;; EXTERNAL VARIABLES
  (and symbol? ?variable) (->Literal variable)

  ;; IMPORTED VIEWS
  (and seq? (parse-import ?import)) import)

(defview parse-match
  (prefix (parse-pattern-ast ?pattern-ast) ?result-src)
  (let [[pattern scope] (pattern/with-scope pattern-ast #{})
        result-fun (util/src-with-scope result-src scope)]
    `(view/->Match ~pattern ~result-fun)))

(defview parse-view
  ((view/zero-or-more-prefix parse-match) ?matches)
  `(view/->Or ~(vec matches)))
```

The strucjure.test namespace defines a view which parses this readme and runs all of the examples.

## License

Distributed under the GNU Lesser General Public License.
