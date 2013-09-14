["At some point as a programmer you might have the insight/fear that all programming is just doing stuff to other stuff."](http://highscalability.com/blog/2013/2/14/when-all-the-programs-a-graph-prismatics-plumbing-library.html)

Strucjure is a library for doing stuff to other stuff. You provide a declarative grammar for your stuff and strucjure gives you validators, parsers, walks and lenses (and eventually generators).

## Note

The last stable version of strucjure is [https://github.com/jamii/strucjure/releases/tag/v0.3.5]. This readme refers to the version currently in development.

## Quickstart

```clojure
[strucjure "0.4.0-SNAPSHOT"]
```

```clojure
user> (require '[strucjure.pattern :as p]
               '[strucjure.graph :as g]
               '[strucjure.sugar :as s]
               '[plumbing.core :refer [fnk]])
nil
```

Let's define a grammar for peano numbers.

```
user> (def peano-graph
           (s/graph num ~(s/or ~succ ~zero)
                    succ (succ ~num)
                    zero zero))
#'user/peano-graph
user> (def peano (s/view ~(s/node-of peano-graph 'num)))
#'user/peano
user> (peano 'zero)
[zero nil]
user> (peano '(succ (succ zero)))
[(succ (succ zero)) nil]
user> (peano '(succ (succ)))
Exception Match failed  sun.reflect.NativeConstructorAccessorImpl.newInstance0 (NativeConstructorAccessorImpl.java:-2)
```

Views can do more than just validate grammars. We can also express transformations.

```
user> (def peano->int-graph
           (g/output-in peano-graph
                        'succ (fnk [num] (inc num))
                        'zero (fnk [] 0)))
#'user/peano-as-int
user> (def peano->int (s/view ~(s/node-of peano->int-graph 'num)))
#'user/peano->int
user> (peano->int 'zero)
[0 nil]
user> (peano->int '(succ (succ zero)))
[2 nil]
user> (peano->int '(succ (succ 0)))
Exception Match failed  sun.reflect.NativeConstructorAccessorImpl.newInstance0 (NativeConstructorAccessorImpl.java:-2)
```

Or inject debugging code.

```
user> (def trace-peano (s/view ~(s/node-of (strucjure.debug/graph-with-trace peano-graph) 'num)))
#'user/trace-peano
user> (trace-peano '(succ (succ succ)))
 => num (succ (succ succ))
     => succ (succ (succ succ))
         => num (succ succ)
             => succ (succ succ)
                 => num succ
                     => succ succ
                     X succ #<Exception java.lang.Exception: Match failed>
                     => zero succ
                     X zero #<Exception java.lang.Exception: Match failed>
                 X num #<Exception java.lang.Exception: Match failed>
             X succ #<Exception java.lang.Exception: Match failed>
             => zero (succ succ)
             X zero #<Exception java.lang.Exception: Match failed>
         X num #<Exception java.lang.Exception: Match failed>
     X succ #<Exception java.lang.Exception: Match failed>
     => zero (succ (succ succ))
     X zero #<Exception java.lang.Exception: Match failed>
 X num #<Exception java.lang.Exception: Match failed>
Exception Match failed  sun.reflect.NativeConstructorAccessorImpl.newInstance0 (NativeConstructorAccessorImpl.java:-2)
```

Patterns are just data-structures and can be built and modifed without using the macros in strucjure.sugar

``` clojure
user> (def negative-peano
           (-> peano-as-int
               (assoc 'pred (list 'pred (p/->Node 'num)))
               (update-in ['num] #(p/->Or [% (p/->Node 'pred)]))
               (g/output-in 'pred (fnk [num] (dec num)))))
#'user/negative-peano
user> (def negative-peano->int (s/view ~(s/node-of negative-peano 'num))))
#'user/negative-peano->int
user> (negative-peano->int '(succ (pred (succ zero))))
[1 nil]

user>
```

We can also use patterns for traditional pattern matching.

``` clojure
user> (def peano-num (s/node-of peano-as-int 'num))
#'user/peano-num
user> (s/match '(succ zero)
               (succ zero) :ok)
:ok
user> (s/match '(succ zero)
               ~peano-num :ok)
:ok
user> (s/match [1 2 '(succ (succ (succ zero))) 4 5]
               {:x 1 :y 2} :not-a-map
               (1 2 '(succ (succ zero)) 4 5) :not-a-seq
               [1 2 3 4 5] :not-quite-an-integer
               [1 2 ~(s/as ~peano-num 3) 4 5] :just-right)
:just-right
```


## Limitations

 * The functions that are passed to Input, Output, Guard and Is may not be closures (due to limitations of the jvm compiler, a workaround will appear later)

``` clojure
user> (let [x 1] (s/view ~(s/is #(= x %))))
CompilerException java.lang.InstantiationException: user$eval6991$eval7201__7202, compiling:(NO_SOURCE_PATH:1:35)
```

## Patterns

## Graphs

## Sugar

## Views

## Generators

## License

Distributed under the GNU Lesser General Public License.
