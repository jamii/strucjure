Pattern-matching, parsing and generic traversals through the medium of PEGs.

Leiningen couldn't handle the irony.

## Usage

Coming soon...

## Syntax

```clojure
(defn primitive? [value]
  (or (#{nil true false} value)
      (number? value)
      (string? value)
      (char? value)
      (keyword? value)))

(defn binding? [value]
  (and (symbol? value)
       (= \? (.charAt (name value) 0))
       (> (count (name value)) 1)))

(defn binding-name [value]
  (let [var-name (name value)
        name-rest (.substring var-name 1 (.length var-name))]
    (symbol name-rest)))

(declare seq-pattern)

(defmatch pattern
  ;; BINDINGS
  '_ (->Bind '_)
  (and (guard (binding? %)) ?binding) (->Bind (binding-name binding))

  ;; LITERALS
  (and (guard (primitive? %)) ?literal) (->Literal literal) ; primitives evaluate to themselves, so don't need quoting

  ;; SEQUENCES
  (and (guard (vector? %)) [(& ((zero-or-more seq-pattern) ?seq-patterns))]) (apply seq-all seq-patterns)

  ;; SPECIAL FORMS
  (and (guard (seq? %)) ['quote ?quoted]) (->Literal quoted)
  (and (guard (seq? %)) ['guard ?form]) (->Guard form)
  (and (guard (seq? %)) ['leave ?form]) (->Leave form)
  (and (guard (seq? %)) ['and (& ((one-or-more pattern) ?patterns))]) (->And patterns)
  (and (guard (seq? %)) ['or (& ((one-or-more pattern) ?patterns))]) (->Or patterns)

  ;; IMPORTED MATCHES
  (and (guard (seq? %)) [?match (pattern ?pattern)]) (->Import match pattern))

(defmatch seq-pattern
  ;; & PATTERNS
  (and (guard (seq? %)) ['& (pattern ?pattern)]) pattern

  ;; ALL OTHER PATTERNS
  (pattern ?pattern) (->Head pattern)])
```

## License

Distributed under the GNU Lesser General Public License.
