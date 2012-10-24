(ns strucjure.parser
  (:use [strucjure.pattern :only [->Or ->And ->Not ->Bind ->Seq ->Head ->Literal ->Guard ->Ignore ->Map ->Record ->Regex prefix seqable]])
  (:require [strucjure.view :as view]
            [strucjure.pattern :as pattern]
            [strucjure.util :as util]))

;; --- API ---

;; will redef this later when bootstrapping
(def parse-pattern-ast
  (view/->Raw
   (fn [pattern-src]
     [nil (eval pattern-src)])))

(defmacro pattern [pattern-src]
  (let [pattern-ast (view/run-or-throw parse-pattern-ast pattern-src)
        [pattern _] (pattern/with-scope pattern-ast #{})]
    pattern))

(defmacro defpattern [name pattern]
  `(def ~name (pattern ~pattern)))

(defmacro defnpattern [name args pattern]
  `(def ~name ~args (pattern ~pattern)))

;; will redef this later when bootstrapping
(def parse-view
  (view/->Raw
   (fn [pattern-srcs&result-srcs]
     (assert (even? (count pattern-srcs&result-srcs)))
     [nil `(view/->Or
            ~(vec (for [[pattern-src result-src] (partition 2 pattern-srcs&result-srcs)]
                    (let [pattern-ast (view/run-or-throw parse-pattern-ast pattern-src)
                          [pattern scope] (pattern/with-scope pattern-ast #{})
                          result-fun (util/src-with-scope result-src scope)]
                      `(view/->Match ~pattern ~result-fun)))))])))

(defmacro view [& pattern-srcs&result-srcs]
  (view/run-or-throw parse-view pattern-srcs&result-srcs))

(defmacro defview [name & pattern-srcs&result-srcs]
  `(def ~name (view ~@pattern-srcs&result-srcs)))

(defmacro defnview [name args & pattern-srcs&result-srcs]
  `(defn ~name ~args (view ~@pattern-srcs&result-srcs)))

;; --- PARSER UTILS ---

(defn primitive? [value]
  (or (nil? value)
      (true? value)
      (false? value)
      (number? value)
      (string? value)
      (char? value)
      (keyword? value)))

(defn flat? [value]
  (or (symbol? value)
      (primitive? value)))

(defn binding? [value]
  (and (symbol? value)
       (re-find #"^\?(.+)$" (name value))))

(defn binding-name [value]
  (let [[_ string] (re-find #"^\?(.+)$" (name value))]
    (symbol string)))

(defn constructor? [value]
  (and (symbol? value)
       (re-find #"^(.+)\.$" (name value))))

(defn constructor-name [value]
  (let [[_ string] (re-find #"^(.+)\.$" (name value))]
    (symbol string)))

(defn class-name? [value]
  (and (symbol? value)
       (re-find #"^\A(?:[a-z0-9\-]+\.)*[A-Z]\w*\Z$" (name value))))

(defn predicate? [value]
  (and (symbol? value)
       (.endsWith (name value) "?")))

;; --- BOOTSTRAPPED PARSER ---

(defnview two-or-more [elem]
  (prefix (->Head (view/->Import 'elem (->Bind 'x1)))
          (->Head (view/->Import 'elem (->Bind 'x2)))
          (view/->Import '(view/zero-or-more elem) (->Bind 'xs)))
  (cons x1 (cons x2 xs)))

(defview parse-seq-pattern-ast
  ;; & PATTERNS

  (prefix (->Head (->Literal ''&))
          (->Head (view/->Import 'parse-pattern-ast (->Bind 'pattern))))
  pattern

  ;; ESCAPED PATTERNS

  (prefix (->Head (view/->Import 'parse-pattern-ast (->Bind 'pattern))))
  (->Head pattern))

(defview parse-pattern-ast*
  ;; BINDINGS

  (->And [(->Guard `(binding? ~util/input-sym))
          (->Bind 'binding)])
  (->Bind (binding-name binding))

  ;; LITERALS

  (->And [(->Guard `(primitive? ~util/input-sym))
          (->Bind 'primitive)])
  (->Literal primitive)

  (->And [(->Guard `(class-name? ~util/input-sym))
          (->Bind 'class-name)])
  (->Guard `(instance? ~class-name ~util/input-sym))

  ;; PREDICATES

  (->And [(->Guard `(predicate? ~util/input-sym))
          (->Bind 'predicate)])
  (->Guard `(~predicate ~util/input-sym))

  ;; SEQUENCES

  (->And [(->Guard `(vector? ~util/input-sym))
          (seqable (view/->Import '(view/zero-or-more-prefix parse-seq-pattern-ast) (->Bind 'seq-patterns)))])
  (apply seqable seq-patterns)

  (->And [(->Guard `(seq? ~util/input-sym))
          (seqable (->Head (->Literal ''prefix))
                   (view/->Import '(view/zero-or-more-prefix parse-seq-pattern-ast) (->Bind 'seq-patterns)))])
  (apply prefix seq-patterns)

  ;; SPECIAL FORMS

  (->And [(->Guard `(seq? ~util/input-sym))
          (seqable (->Head (->Literal ''quote))
                   (->Head (->Bind 'quoted)))])
  (->Literal `(quote ~quoted))

  (->And [(->Guard `(seq? ~util/input-sym))
          (seqable (->Head (->Literal ''and))
                   (view/->Import '(two-or-more parse-pattern-ast) (->Bind 'patterns)))])
  (->And patterns)

  (->And [(->Guard `(seq? ~util/input-sym))
          (seqable (->Head (->Literal ''or))
                   (view/->Import '(two-or-more parse-pattern-ast) (->Bind 'patterns)))])
  (->Or patterns)

  ;; IMPORTED VIEWS

  (->And [(->Guard `(seq? ~util/input-sym))
          (seqable (->Head (->Bind 'view))
                   (->Head (view/->Import 'parse-pattern-ast (->Bind 'pattern))))])
  (view/->Import view pattern))

(defview parse-match
  (prefix (->Head (view/->Import 'parse-pattern-ast (->Bind 'pattern-ast)))
          (->Head (->Bind 'result-src)))
  (let [[pattern scope] (pattern/with-scope pattern-ast #{})
        result-fun (util/src-with-scope result-src scope)]
    `(view/->Match ~pattern ~result-fun)))

(defview parse-view*
  (view/->Import '(view/zero-or-more-prefix parse-match) (->Bind 'matches))
  `(view/->Or ~(vec matches)))

(def parse-pattern-ast parse-pattern-ast*)
(def parse-view parse-view*)

;; --- REAL PARSER ---

(defview parse-key&pattern-ast
  [?key (parse-pattern-ast ?pattern)] [key pattern])

(defview parse-import
  [?view (parse-pattern-ast ?pattern)] (view/->Import view pattern)
  [?view & (parse-import ?import)] (view/->Import view import))

(defview parse-seq-pattern-ast*
  ;; & PATTERNS
  (prefix '& (parse-pattern-ast ?pattern)) pattern

  ;; ALL OTHER PATTERNS
  (prefix (parse-pattern-ast ?pattern)) (->Head pattern))

(defview parse-pattern-ast*
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

(defview parse-match*
  (prefix (parse-pattern-ast ?pattern-ast) ?result-src)
  (let [[pattern scope] (pattern/with-scope pattern-ast #{})
        result-fun (util/src-with-scope result-src scope)]
    `(view/->Match ~pattern ~result-fun)))

(defview parse-view*
  ((view/zero-or-more-prefix parse-match) ?matches)
  `(view/->Or ~(vec matches)))

(def parse-seq-pattern-ast parse-seq-pattern-ast*)
(def parse-pattern-ast parse-pattern-ast*)
(def parse-match parse-match*)
(def parse-view parse-view*)
