["At some point as a programmer you might have the insight/fear that all programming is just doing stuff to other stuff."](http://highscalability.com/blog/2013/2/14/when-all-the-programs-a-graph-prismatics-plumbing-library.html)

In idiomatic clojure data is not hidden behind classes and methods, but instead left lying around in a homogenous heap of stuff. Assumptions about the shape of stuff are implicitly encoded in the functions used to operate on it. When your stuff is the wrong shape things blow up far down the line in an unhelpful fashion.

``` clojure
user> (doc ns)
-------------------------
clojure.core/ns
([name docstring? attr-map? references*])
...
user> (ns foo
        (:require [bar :refer-all]))
IllegalArgumentException No value supplied for key: true  clojure.lang.PersistentHashMap.create (PersistentHashMap.java:77)
```

Strucjure is a library for describing stuff in an executable manner. You provide a declarative grammar for your stuff and strucjure gives you pattern matching, validators, parsers, walks and lenses (and eventually generators). The shape of your data is immediately apparent from your code and errors are clearly reported.

``` clojure
user> (require '[strucjure.sugar :as s] '[strucjure.view :as v])
nil

user> (def ns-grammar
  (s/graph
   ns (list 'ns ^name symbol (&? docstring) (&? attr-map) (&* reference))
   docstring (is string?)
   attr-map (is map?)
   reference (or require import)
   require (list :require (&* libspec))
   libspec (or symbol
               [^prefix symbol (&* libspec)]
               [symbol (&*& option)])
   option (or (list :as symbol)
              (list :refer (or :all [(&* symbol)]))
              (list :reload)
              (list :reload-all)
              (list :verbose))
   import (list :import (&* [symbol & * symbol]))
   symbol (is symbol?)))
#'user/ns-grammar

user> (def ns-validate
  (v/with-layers [v/with-depth v/with-deepest-failure]
    (v/*view* (s/node-of 'ns ns-grammar))))
#'user/ns-validate

user> (ns-validate '(ns foo (:require [bar :refer :all])))
(ns foo (:require [bar :refer :all]))

user> (ns-validate '(ns foo (:require [bar :refer-all])))
Failure strucjure.view.Failure: (trap-failure (#<core$symbol_QMARK_ clojure.core$symbol_QMARK_@7078cdad> :refer-all)) at node `symbol` on input `:refer-all`  strucjure.view/with-deepest-failure/fn--42754 (view.clj:372)

user> (def ns-validate-verbose
  (v/with-layers [v/with-node-depth v/trace-nodes]
    (v/*view* (s/node-of 'ns ns-grammar))))
#'user/ns-validate-verbose

user> (ns-validate-verbose '(ns foo (:require [bar :refer-all])))
 => ns (ns foo (:require [bar :refer-all]))
     => symbol foo
     <= symbol foo nil
     => docstring (:require [bar :refer-all])
     X docstring strucjure.view.Failure: (trap-failure (#<core$string_QMARK_ clojure.core$string_QMARK_@5d850909> (:require [bar :refer-all])))
     => attr-map (:require [bar :refer-all])
     X attr-map strucjure.view.Failure: (trap-failure (#<core$map_QMARK_ clojure.core$map_QMARK_@581cb215> (:require [bar :refer-all])))
     => reference (:require [bar :refer-all])
         => require (:require [bar :refer-all])
             => libspec [bar :refer-all]
                 => symbol [bar :refer-all]
                 X symbol strucjure.view.Failure: (trap-failure (#<core$symbol_QMARK_ clojure.core$symbol_QMARK_@7078cdad> [bar :refer-all]))
                 => symbol bar
                 <= symbol bar nil
                 => libspec :refer-all
                     => symbol :refer-all
                     X symbol strucjure.view.Failure: (trap-failure (#<core$symbol_QMARK_ clojure.core$symbol_QMARK_@7078cdad> :refer-all))
                 X libspec strucjure.view.Failure: (vector? :refer-all)
                 => symbol bar
                 <= symbol bar nil
                 => option (:refer-all)
                 X option strucjure.view.Failure: (= :verbose :refer-all)
             X libspec strucjure.view.Failure: (clojure.core/nil? (:refer-all))
         X require strucjure.view.Failure: (clojure.core/nil? ([bar :refer-all]))
         => import (:require [bar :refer-all])
         X import strucjure.view.Failure: (= :import :require)
     X reference strucjure.view.Failure: (= :import :require)
 X ns strucjure.view.Failure: (clojure.core/nil? ((:require [bar :refer-all])))
Failure strucjure.view.Failure: (trap-failure (#<core$symbol_QMARK_ clojure.core$symbol_QMARK_@7078cdad> :refer-all)) at node `symbol` on input `:refer-all`  strucjure.view/with-deepest-failure/fn--42754 (view.clj:372)
```

## Note

The last stable version of strucjure is [v0.3.5](https://github.com/jamii/strucjure/releases/tag/v0.3.5). This readme refers to the version currently in development. Here be dragons...

## Quickstart

``` clojure
[strucjure "0.4.0-SNAPSHOT"]
```

...

## Patterns

## Graphs

## Sugar

## Views

## Generators

## License

Distributed under the GNU Lesser General Public License.
