["At some point as a programmer you might have the insight/fear that all programming is just doing stuff to other stuff."](http://highscalability.com/blog/2013/2/14/when-all-the-programs-a-graph-prismatics-plumbing-library.html)

In idiomatic clojure data is not hidden behind classes and methods, but instead left lying around in a homogenous heap of stuff. Assumptions about the shape of stuff are implicitly encoded in the functions used to operate on it. When your stuff is the wrong shape things blow up far down the line in an unhelpful fashion.

```
(ns example
  (:require [foo :refer-all]))
;; java.lang.IllegalArgumentException: No value supplied for key: true
;; PersistentHashMap.java:77 clojure.lang.PersistentHashMap.create
;; Yep, thanks for that.
```

Strucjure is a library for describing stuff in an executable manner. It gives you pattern matching (with first-class patterns), validators, parsers, walks and lenses (and eventually generators). The shape of your data is immediately apparent from your code and errors are clearly reported.

``` clojure
[strucjure "0.4.0"]
```

## Concision

Pattern matching tends to be far more concise than imperative style chains of boolean tests which we still use in clojure every day.

Compare the imperative approach...

``` java
private void adjustAfterInsertion(Node n) {
        // Step 1: color the node red
        setColor(n, Color.red);

        // Step 2: Correct double red problems, if they exist
        if (n != null && n != root && isRed(parentOf(n))) {

            // Step 2a (simplest): Recolor, and move up to see if more work
            // needed
            if (isRed(siblingOf(parentOf(n)))) {
                setColor(parentOf(n), Color.black);
                setColor(siblingOf(parentOf(n)), Color.black);
                setColor(grandparentOf(n), Color.red);
                adjustAfterInsertion(grandparentOf(n));
            }

            // Step 2b: Restructure for a parent who is the left child of the
            // grandparent. This will require a single right rotation if n is
            // also
            // a left child, or a left-right rotation otherwise.
            else if (parentOf(n) == leftOf(grandparentOf(n))) {
                if (n == rightOf(parentOf(n))) {
                    rotateLeft(n = parentOf(n));
                }
                setColor(parentOf(n), Color.black);
                setColor(grandparentOf(n), Color.red);
                rotateRight(grandparentOf(n));
            }

            // Step 2c: Restructure for a parent who is the right child of the
            // grandparent. This will require a single left rotation if n is
            // also
            // a right child, or a right-left rotation otherwise.
            else if (parentOf(n) == rightOf(grandparentOf(n))) {
                if (n == leftOf(parentOf(n))) {
                    rotateRight(n = parentOf(n));
                }
                setColor(parentOf(n), Color.black);
                setColor(grandparentOf(n), Color.red);
                rotateLeft(grandparentOf(n));
            }
        }

        // Step 3: Color the root black
        setColor((Node) root, Color.black);
    }
```

...to the declarative approach.

``` clojure
(defrecord Red [value left right])
(defrecord Black [value left right])

(defn balance [tree]
  (s/match tree
         (s/or
          (Black. ^z _ (Red. ^y _ (Red. ^x _ ^a _ ^b _) ^c _) ^d _)
          (Black. ^z _ (Red. ^x _ ^a _ (Red. ^y _ ^b _ ^c _)) ^d _)
          (Black. ^x _ ^a _ (Red. ^z _ (Red. ^y _ ^b _ ^c _) ^d _))
          (Black. ^x _ ^a _ (Red. ^y _ ^b _ (Red. ^z _ ^c _ ^d _))))
         (Red. y (Black. x a b) (Black. z c d))

         ^other _
         other))
```

## First-class patterns

Patterns in strucjure are first-class. The pattern part of the match statement is not a special langauge but just clojure code that is evaluated at compile-time and returns an instance of the `Pattern` and `View` protocols. This means you can easily extend the pattern language.

``` clojure
(match {:a 1 :b 2}
       {:a ^a _ :b ^b _} [a b])

(defn my-keys* [symbols]
  (for-map [symbol symbols]
           (keyword (str symbol))
           (s/name symbol _)))

(defmacro my-keys [& symbols]
  `(my-keys* '~symbols)))

(s/match {:a 1 :b 2}
       (my-keys a b) [a b])
```

Even the recursive patterns used in parsing are first-class data structures which can be modified and composed.

``` clojure
(def expr
  (s/letp [num (s/or succ zero)
           succ (s/case ['succ num] (inc num))
           zero (s/case 'zero 0)
           expr (s/or num add)
           add (s/case ['add ^a expr ^b expr] (+ a b))]
          expr))

(match '(add (succ zero) (succ zero))
       ^result expr result)
;; 2

(def expr-with-sub
  (-> expr
      (update-in [:refers 'expr] #(s/or % (->Refer 'sub)))
      (assoc-in [:refers 'sub] (s/case ['sub ^a expr ^b expr] (- a b)))))

(s/match '(sub (add (succ zero) (succ zero)) (succ zero))
       ^result expr-with-sub result)
;; 1
```

## Error reporting

Clojure destructuring can be a little too helpful at times.

``` clojure
(defn f [{:keys [x y] :as z}]
      [x y z])

(f {:x 1 :y 2})
;; [1 2 {:x 1 :y 2}]

(f nil)
;; [nil nil nil]

(f (list 1 2 3 4))
;; [nil nil {1 2 3 4}]
```

Strucjure sanity-checks its input so you don't have to.

``` clojure
(require '[strucjure.sugar :as s :refer [_]])

(defn g [input]
  (s/match input
         ^z (s/keys x y) [x y z]))

(g {:x 1 :y 2})
;; [1 2 {:x 1 :y 2}]

(g nil)
;; strucjure.view.Failure:
;; Failed test (clojure.core/map? input6214) in pattern {:x #strucjure.pattern.Name{:name x, :pattern #strucjure.pattern.Any{}}, :y #strucjure.pattern.Name{:name y, :pattern #strucjure.pattern.Any{}}} on input nil

(g (list 1 2 3 4))
;; strucjure.view.Failure:
;; Failed test (clojure.core/map? input6214) in pattern {:x #strucjure.pattern.Name{:name x, :pattern #strucjure.pattern.Any{}}, :y #strucjure.pattern.Name{:name y, :pattern #strucjure.pattern.Any{}}} on input (1 2 3 4)
```

The errors produced by failing matches contain a list of every point at which the match backtracked (in reverse order).

``` clojure
(s/match [1 2 3]
         [1 2] :nope
         [1 2 3 4] :nope
         [1 :x] :oh-noes)
;; strucjure.view.Failure:
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern :x on input 2
;; Failed test (clojure.core/not (clojure.core/nil? input6214)) in pattern 4 on input nil
;; Failed test (clojure.core/nil? input6214) in pattern nil on input (3)

(s/match '(add (sub (succ zero) (succ zero)) (succ zero))
         ^result expr result)
;; strucjure.view.Failure:
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern add on input sub
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (sub (succ zero) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input sub
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (sub (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add
```

If that isn't enough to locate the failure, you can also run the match with tracing enabled:

``` clojure
(with-out-str
  (match-with trace-let '(add (add (succ zero) (succ zero)) (succ zero))
              expr expr))
;;  => expr (add (add (succ zero) (succ zero)) (succ zero))
;;    => num (add (add (succ zero) (succ zero)) (succ zero))
;;     => succ (add (add (succ zero) (succ zero)) (succ zero))
;;     XX succ #<Failure strucjure.view.Failure:
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;     => zero (add (add (succ zero) (succ zero)) (succ zero))
;;     XX zero #<Failure strucjure.view.Failure:
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (add (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;    XX num #<Failure strucjure.view.Failure:
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (add (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;    => add (add (add (succ zero) (succ zero)) (succ zero))
;;     => expr (add (succ zero) (succ zero))
;;      => num (add (succ zero) (succ zero))
;;       => succ (add (succ zero) (succ zero))
;;       XX succ #<Failure strucjure.view.Failure:
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (add (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;       => zero (add (succ zero) (succ zero))
;;       XX zero #<Failure strucjure.view.Failure:
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (succ zero) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (add (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;      XX num #<Failure strucjure.view.Failure:
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (succ zero) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (add (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;      => add (add (succ zero) (succ zero))
;;       => expr (succ zero)
;;        => num (succ zero)
;;         => succ (succ zero)
;;          => num zero
;;           => succ zero
;;           XX succ #<Failure strucjure.view.Failure:
;; Failed test (strucjure.view/seqable? input6214) in pattern [succ #strucjure.pattern.Name{:name num, :pattern #strucjure.pattern.Refer{:name num}}] on input zero
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (succ zero) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (add (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;           => zero zero
;;           <= zero 0
;;          <= num 0
;;         <= succ 1
;;        <= num 1
;;       <= expr 1
;;       => expr (succ zero)
;;        => num (succ zero)
;;         => succ (succ zero)
;;          => num zero
;;           => succ zero
;;           XX succ #<Failure strucjure.view.Failure:
;; Failed test (strucjure.view/seqable? input6214) in pattern [succ #strucjure.pattern.Name{:name num, :pattern #strucjure.pattern.Refer{:name num}}] on input zero
;; Failed test (strucjure.view/seqable? input6214) in pattern [succ #strucjure.pattern.Name{:name num, :pattern #strucjure.pattern.Refer{:name num}}] on input zero
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (succ zero) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (add (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;           => zero zero
;;           <= zero 0
;;          <= num 0
;;         <= succ 1
;;        <= num 1
;;       <= expr 1
;;      <= add 2
;;     <= expr 2
;;     => expr (succ zero)
;;      => num (succ zero)
;;       => succ (succ zero)
;;        => num zero
;;         => succ zero
;;         XX succ #<Failure strucjure.view.Failure:
;; Failed test (strucjure.view/seqable? input6214) in pattern [succ #strucjure.pattern.Name{:name num, :pattern #strucjure.pattern.Refer{:name num}}] on input zero
;; Failed test (strucjure.view/seqable? input6214) in pattern [succ #strucjure.pattern.Name{:name num, :pattern #strucjure.pattern.Refer{:name num}}] on input zero
;; Failed test (strucjure.view/seqable? input6214) in pattern [succ #strucjure.pattern.Name{:name num, :pattern #strucjure.pattern.Refer{:name num}}] on input zero
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (succ zero) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern zero on input (add (add (succ zero) (succ zero)) (succ zero))
;; Failed test (clojure.core/= literal__6312__auto__ input6214) in pattern succ on input add>
;;         => zero zero
;;         <= zero 0
;;        <= num 0
;;       <= succ 1
;;      <= num 1
;;     <= expr 1
;;    <= add 3
;;   <= expr 3
 ```

## Performance

The aim for the 1.0 release is for every match to execute at least as fast as the equivalent idiomatic clojure code.

``` clojure
(= {:a 1 :b 2}
   {:a 1 :b 2})
;; 173 ns

(let [{:keys [a b]} {:a 1 :b 2}]
  (and (= a 1) (= b 2)))
;; 464 ns (really?)

(match {:a 1 :b 2}
       {:a 1 :b 2} :ok)
;; 159 ns
```

Binding variables in a match is currently expensive relative to normal clojure destructuring (due to using proteus.Container to fake mutable variables).

``` clojure
(let [{:keys [a b]} {:a 1 :b 2}]
  [a b])
;; 123 ns

(s/match {:a 1 :b 2}
         (s/keys a b) [a b])
;; 648 ns :(
```

Other performance disparities are less clear.

``` clojure
(defn f [pairs]
  (if-let [[x y & more] pairs]
    (cons (clojure.core/+ x y) (f more))
    nil))

(f (range 10))
;; 3.5 us

(defn g [pairs]
  (match pairs
         [^x _ ^y _ ^more (& _)] (cons (clojure.core/+ x y) (g more))
         [] nil))

(g (range 10))
;; 7.1 us

(defn h [pairs]
  (match pairs
         (letp [p (case [^x _ ^y _ (& p)] (cons (clojure.core/+ x y) p)
                        [] nil)]
               p)))

(h (range 10))
;; 9.7 µs
```

## License

Distributed under the GNU Lesser General Public License.
