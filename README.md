Pattern-matching, parsing and generic traversals through the medium of [PEGs](http://en.wikipedia.org/wiki/Parsing_expression_grammar).

Leiningen couldn't handle the irony.

## Installation

```clojure
[strucjure "0.1.0"]
```

```clojure
:use [strucjure :only [match view defview defnview]]
```

## TODO

Strucjure is still unstable and may change significantly in future versions.

Strucjure does not yet support:

 * Set literals or record literals

 * Exporting bindings from Or and Not patterns

 * Informative error messages

 * Left-recursive rules (you'll get a stack overflow)

 * Memoisation

 * Ad-hoc extension

## Concepts

The core constructs in strucjure are patterns and views.

A pattern takes an input and a set of bindings, consumes some or all of the input and returns new bindings. These are all patterns:

```clojure
?n
number?
(and number? ?n)
[?x '+ ?y]
```

A view takes an input, consumes some or all of the input and returns a value. A view is constructed from a list of [pattern value] pairs, where the value forms have access to the patterns bindings eg

```clojure
(defview calculate
  (and number? ?n) n
  [?x '+ ?y] (+ x y)
  [?x '* ?y] (* x y))
```

Views are callable - the output of a view is the value corresponding to the first matching pattern.

```clojure
user> (calculate '(1 + 2))
3
```

By default views will fail if they do not consume all their input but you can overide this by passing your own functions for success and failure.

```clojure
user> (calculate '(1 + 2) (fn [output rest] (prn "output" output "rest" rest)) (fn [] (prn "failed :(")))
"output" 3 "rest" nil
nil
user> (calculate '(1 / 2) (fn [output rest] (prn "output" output "rest" rest)) (fn [] (prn "failed :(")))
"failed :("
nil
```

Views are first-class objects and can be used in other views or even recursively call themselves.

```clojure
user> (defview calculate
        (and number? ?n) n
        [(calculate ?x) '+ (calculate ?y)] (+ x y)
        [(calculate ?x) '* (calculate ?y)] (* x y))
#'user/calculate
user> (calculate '((1 * 2) + (3 * (4 + 5))))
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
user> (match 1
             _ 'yup)
yup
```

Primitives (nil, true, false, numbers, keywords, characters and strings) create patterns which match themselves.

```clojure
user> (match "foo"
             42 'nope
             \f 'nope
             "foo" 'yup)
yup
```

Quoted terms match their quoted self.

```clojure
user> (match '(+ 1 2)
             '(+ 1 2) 'yup)
yup
user> (match vector?
             'vector? 'yup)
ExceptionInfo throw+: :strucjure/no-matching-pattern  user/eval28197/thunk--28196--28198 (NO_SOURCE_FILE:1)
user> (= vector? 'vector?)
false
```

Symbols beginning with ? match anything and create a new binding.

```clojure
user> (match [1 2]
             ?x x)
[1 2]
```

Symbols ending in ? and anonymous functions are treated as predicates applied to the input and match if they return true.

```clojure
user> (match [1 2]
             string? 'nope
             #(odd? (count %)) 'nope
             vector? 'yup)
yup
```

Classes match instances of that class.

```clojure
user> (match "foobear"
             java.lang.String 'yup)
yup
```

Any other symbols are assumed to be externally bound and match against their value.

```clojure
user> (let [cinq 5]
        (match 5
               cinq 'yup))
yup
```

Regular expressions call re-find on the input and match if the result is not nil.

```clojure
user> (match "foobear"
             #"bear" 'yup)
yup
```

Maps lookup their keys and, if the key is found, match against the associated pattern.

```clojure
user> (match {:a 1 :b 2}
             {:a odd?} 'yup)
yup
```

If the key is not found then even nil patterns will not match.

```clojure
user> (match {:a 1 :b 2}
             {:c nil} 'nope)
ExceptionInfo throw+: :strucjure/no-matching-pattern  strucjure/fail (strucjure.clj:275)
```

Vectors match any seqable type and recursively test their inner patterns.

```clojure
user> (match [1 2 3]
             [1 2] 'nope
             [1 2 3 4] 'nope
             [1 2 ?x] x)
3
```

Inside a vector you can use '& pattern' to apply a pattern to the rest of the sequence, rather than to the next element.

```clojure
user> (match [1 2 3]
             [1 & #(> (count %) 1)] 'yup)
yup
```

The special form 'prefix' may leave part of the input unconsumed and otherwise behaves like a vector. This is useful when chaining parsers together (see eg the arith macro in the examples section below).

The special forms 'and', 'or' and 'not' combine patterns in the obvious way.

```clojure
user> (match "foobear"
             (and #"foo" #"bar") 'nope
             (not #"foo") 'nope
             (or #"foo" "bar") 'yup)
yup
```

Three other special forms ('seq', 'guard' and 'leave') are not documented and may be removed in future versions.

Calls to record constructors match records and recursively apply their inner patterns.

```clojure
user> (defrecord Foo [bear])
user.Foo
user> (match (Foo. 'iama-bear)
             (Foo. ?bear) bear)
iama-bear
```

Any other function call should be of the form (view pattern). This calls an external view on the current input and matches the output against the pattern.

```clojure
user> (defnview zero-or-more [elem]
        (prefix (elem ?x) & ((zero-or-more elem) ?xs)) (cons x xs)
        (prefix) nil)
#'user/zero-or-more
user> (defview one
        1 'one)
#'user/one
user> (defview two
        2 'two)
#'user/two
user> (match [1 1 1 1 2 2]
             [& ((zero-or-more one) ?ones) & ((zero-or-more two) ?twos)] [(count ones) (count twos)])
[4 2]
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
  (balanced-height tree (fn [_ _] true) (fn [] false)))

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
(declare arith-value arith-mult-div arith-plus-minus)

(def reserved? #{'* '/ '+ '-})

(defview arith-value
  (prefix [(arith-plus-minus ?x)]) x
  (prefix (and (not reserved?) ?n)) n)

(defview arith-mult-div
  (prefix & (arith-value ?x) '* & (arith-mult-div ?y)) `(~'* ~x ~y)
  (prefix & (arith-value ?x) '/ & (arith-mult-div ?y)) `(~'/ ~x ~y)
  (prefix & (arith-value ?x)) x)

(defview arith-plus-minus
  (prefix & (arith-mult-div ?x) '+ & (arith-plus-minus ?y)) `(~'+ ~x ~y)
  (prefix & (arith-mult-div ?x) '- & (arith-plus-minus ?y)) `(~'- ~x ~y)
  (prefix & (arith-mult-div ?x)) x)

(defmacro arith [& tokens]
  (arith-plus-minus tokens))

user> (macroexpand '(arith 1 + 2 * 7 + 1 / 2))
(+ 1 (+ (* 2 7) (/ 1 2)))
user> (macroexpand '(arith 1 + 2 * (7 + 1) / 2))
(+ 1 (* 2 (/ (7 + 1) 2)))
```

The syntax of strucjure itself is self-defined using views.

```clojure
(defnview optional [elem]
  (prefix (elem ?x)) x
  (prefix) nil)

(defnview zero-or-more [elem]
  (prefix (elem ?x) & ((zero-or-more elem) ?xs)) (cons x xs)
  (prefix) nil)

(defnview one-or-more [elem]
  (prefix (elem ?x) & ((zero-or-more elem) ?xs)) (cons x xs))

(defnview zero-or-more-prefix [elem]
  (prefix & (elem ?x) & ((zero-or-more-prefix elem) ?xs)) (cons x xs)
  (prefix) nil)

(defnview one-or-more-prefix [elem]
  (prefix & (elem ?x) & ((zero-or-more-prefix elem) ?xs)) (cons x xs))

(defview key&pattern
  [?key (pattern ?pattern)] [key pattern])

(defview pattern
  ;; BINDINGS
  '_ (->Leave nil)
  (and binding? ?binding) (->Bind (binding-name binding))

  ;; LITERALS
  (and primitive? ?primitive) (literal-ast primitive) ; primitives evaluate to themselves, so don't need quoting
  (and class-name? ?class-name) (class-ast class-name)
  (and (or clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap) [& ((zero-or-more key&pattern) ?keys&patterns)]) (map-ast keys&patterns)
  (and seq? [(and constructor? ?constructor) & ((zero-or-more pattern) ?arg-patterns)]) (constructor-ast (constructor-name constructor) arg-patterns)

  ;; PREDICATES
  (and java.util.regex.Pattern ?regex) (regex-ast regex)
  (and predicate? ?predicate) (predicate-ast `(~predicate ~input-sym))
  (and seq? [(or 'fn 'fn*) [] & ?body]) (predicate-ast `(do ~@body))
  (and seq? [(or 'fn 'fn*) [?arg] & ?body]) (predicate-ast `(do ~@(clojure.walk/prewalk-replace {arg input-sym} body)))

  ;; SEQUENCES
  (and vector? [& ((zero-or-more-prefix seq-pattern) ?seq-patterns)]) (apply seqable-ast seq-patterns)
  (and seq? ['prefix & ((zero-or-more-prefix seq-pattern) ?seq-patterns)]) (apply prefix-ast seq-patterns)

  ;; SPECIAL FORMS
  (and seq? ['quote ?quoted]) (literal-ast `(quote ~quoted))
  (and seq? ['guard ?form]) (->Guard form)
  (and seq? ['leave ?form]) (->Leave form)
  (and seq? ['and & ((one-or-more pattern) ?patterns)]) (apply and-ast patterns)
  (and seq? ['seq & ((one-or-more pattern) ?patterns)]) (apply seq-ast patterns)
  (and seq? ['or & ((one-or-more pattern) ?patterns)]) (apply or-ast patterns)
  (and seq? ['not (pattern ?pattern)]) (->Not pattern)

  ;; EXTERNAL VARIABLES
  (and symbol? ?variable) (literal-ast variable)

  ;; IMPORTED VIEWS
  (and seq? [?view (pattern ?pattern)]) (import-ast view pattern))

(defview seq-pattern
  ;; & PATTERNS
  (prefix '& (pattern ?pattern)) pattern

  ;; ESCAPED PATTERNS
  (prefix (and seq? ['guard ?form])) (->Guard form)

  ;; ALL OTHER PATTERNS
  (prefix (pattern ?pattern)) (head-ast pattern)))
```

## License

Distributed under the GNU Lesser General Public License.
