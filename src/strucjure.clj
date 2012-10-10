(ns strucjure
  (:use clojure.test
        [slingshot.slingshot :only [throw+ try+]])
  (:require strucjure.bootstrap
            clojure.set
            clojure.walk
            clojure.pprint))

;; PEG parser / pattern matcher
;; (originally based on matchure)

;; A pattern takes an input and a set of bindings, consumes some or all of the input and returns new bindings
;; A view takes an input, consumes some or all of the input and returns a value
;; A view is constructed from a list of [pattern value] pairs, where the value forms have access to the patterns bindings

;; TODO
;; fix view indentation in emacs
;; better error/failure reporting
;; provide syntax for matching records, predicates
;; allow optional keys?
;; think about extensibility and memoization

;; UTILS FOR CODEGEN

(def input-sym '%)

(defn replace-input-sym [input form]
  (clojure.walk/prewalk-replace {input-sym input} form))

;; Used to avoid exponential expansion of code in repeated branches
;; :name is a symbol which refers to the thunk fn
;; :args is a vector of symbols which must be passed to the thunk fn
(defrecord Thunk [name args])

(defn expand-thunks [body]
  (if (= Thunk (class body))
    `(~(:name body) ~@(:args body))
    (clojure.walk/walk expand-thunks identity body)))

(defn filter-nil [args]
  (vec (filter #(not (= nil %)) args)))

(defn filter-used [args form]
  (vec (filter
        (fn [arg]
          (let [found (atom false)]
            (clojure.walk/postwalk
             (fn [inner-form]
               (when (= arg inner-form) (swap! found (fn [_] true))))
             form)
            @found))
        args)))

;; thunkify is safe so long as we never reuse variable names
;; TODO check if matching thunk already exists (to optimise Or)
(defn thunkify [thunks form args]
  (let [args (filter-nil args)]
    (if (and (= Thunk (class form))
             (clojure.set/subset? (set (filter-nil (:args form))) (set args)))
      ;; already a thunk with a subset of these args, just reuse it
      form
      ;; otherwise, create a new thunk
      (let [args (filter-used args (expand-thunks form))
            name (gensym "thunk__")
            thunk (->Thunk name args)
            thunk-fn `(~name ~args ~form)]
        (swap! thunks conj thunk-fn)
        thunk))))

(defn replace-arg [thunk old-arg new-arg]
  (update-in thunk [:args] #(clojure.walk/prewalk-replace {old-arg new-arg} %)))

;; LOW-LEVEL AST

(defprotocol Ast
  "An Abstract Syntax Tree for a pattern."
  (ast->clj* [this input bindings thunks true-case false-case]
    "Output code which tests the pattern against input with the supplied bindings. May add new thunks to the list. If the pattern succeeds, call true case with the remaining input and the new bindings (true-case is (fn [rest bindings] code)). Otherwise call the false-case (false-case is code)."))

(defn ast->clj [ast input bindings thunks true-case false-case]
  (ast->clj* ast input bindings thunks true-case false-case))

;; Always succeeds. Consumes and transforms input
(defrecord Leave [form]
  Ast
  (ast->clj* [this input bindings thunks true-case false-case]
    (let [form (replace-input-sym input form)]
      (if (or (symbol? form) (nil? form))
        (true-case form bindings)
        (let [left (gensym "left__")]
          `(let [~left ~form]
             ~(true-case left (conj bindings left))))))))

;; Succeeds if form evaluates to true. Does not consume anything
(defrecord Guard [form]
  Ast
  (ast->clj* [this input bindings thunks true-case false-case]
    `(if ~(replace-input-sym input form)
       ~(true-case input bindings)
       ~false-case)))

;; Same as (->Guard '(= nil %)) but can sometimes be compiled away
(defrecord GuardNil []
  Ast
  (ast->clj* [this input bindings thunks true-case false-case]
    (if (= nil input)
      ;; hardcoded to nil, will always succeed
      (true-case nil bindings)
      ;; otherwise, need to test at runtime
      `(if (= nil ~input)
         ~(true-case nil bindings)
         ~false-case))))

;; If symbol is already bound, tests for equality.
;; Otherwise binds input to symbol
;; Always consumes all input
(defrecord Bind [symbol]
  Ast
  (ast->clj* [this input bindings thunks true-case false-case]
    (if (contains? bindings symbol)
      ;; test for equality
      `(if (= ~symbol ~input)
         ~(true-case nil bindings)
         ~false-case)
      ;; bind symbol
      `(let [~symbol ~input]
         ~(true-case nil (conj bindings symbol))))))

;; Calls the view with the current import and runs pattern on its output
;; The pattern must consume the whole output
(defrecord Import [view pattern]
  Ast
  (ast->clj* [this input bindings thunks true-case false-case]
    (let [false-case (thunkify thunks false-case (conj bindings input))
          output (gensym "output__")
          rest (gensym "rest__")]
      `((.view-fn ~view)
        ~input
        (fn [~output ~rest]
          ~(ast->clj pattern output bindings thunks
                     (fn [new-rest new-bindings]
                       (assert (= nil new-rest)) ;; pattern must totally consume import
                       (true-case rest (conj new-bindings rest)))
                     false-case))
        (fn [] ~false-case)))))

;; All patterns get the same input
;; All bindings are exported
;; The output is the output from the last pattern
(defrecord And [pattern-a pattern-b]
  Ast
  (ast->clj* [this input bindings thunks true-case false-case]
    (let [false-case (thunkify thunks false-case bindings)]
      (ast->clj pattern-a input bindings thunks
                (fn [rest new-bindings]
                  (ast->clj pattern-b input new-bindings thunks true-case false-case))
                false-case))))

;; Each pattern gets the remaining input from the last pattern
;; All bindings are exported
;; The output is the output from the last pattern
(defrecord Seq [pattern-a pattern-b]
  Ast
  (ast->clj* [this input bindings thunks true-case false-case]
    (let [false-case (thunkify thunks false-case bindings)]
      (ast->clj pattern-a input bindings thunks
                (fn [rest new-bindings]
                  (ast->clj pattern-b rest (conj new-bindings rest) thunks true-case false-case))
                false-case))))

;; Each pattern gets the same input
;; No bindings are exported (TODO: allow exporting bindings)
;; The output is the output from the first successful pattern
(defrecord Or [pattern-a pattern-b]
  Ast
  (ast->clj* [this input bindings thunks true-case false-case]
    (let [true-case-input (gensym "true-case-input__")
          true-case-thunk (thunkify thunks (true-case true-case-input bindings) (conj bindings true-case-input))
          true-case (fn [rest _] (replace-arg true-case-thunk true-case-input rest))]
      (ast->clj pattern-a input bindings thunks true-case
                (ast->clj pattern-b input bindings thunks true-case false-case)))))

;; HIGH-LEVEL AST

(defn and-ast [& patterns]
  (reduce ->And patterns))

(defn seq-ast [& patterns]
  (reduce ->Seq patterns))

(defn or-ast [& patterns]
  (reduce ->Or patterns))

(defn import-ast [view pattern]
  (->Import view (seq-ast pattern (->GuardNil))))

(defn literal-ast [literal]
  (seq-ast (->Guard `(= ~literal ~input-sym))
           (->Leave nil)))

(defn head-ast [pattern]
  (seq-ast (->Guard `(not= nil ~input-sym))
           (and-ast (seq-ast (->Leave `(first ~input-sym))
                             pattern
                             (->GuardNil))
                    (->Leave `(next ~input-sym)))))

(defn class-ast [class-name]
  (seq-ast (->Guard `(instance? ~class-name ~input-sym))
           (->Leave nil)))

(defn seqable-ast [& patterns]
  (apply seq-ast
   (flatten
    [(->Guard `(or (instance? clojure.lang.Seqable ~input-sym) (nil? ~input-sym)))
     (->Leave `(seq ~input-sym))
     patterns
     (->GuardNil)])))

(defn key-ast [key pattern]
  (seq-ast
   (->Leave `(get ~input-sym ~key ::not-found))
   (->Guard `(not= ::not-found ~input-sym))
   pattern
   (->GuardNil)))

(defn map-ast [keys&patterns]
  (apply and-ast
         (->Guard `(instance? clojure.lang.Associative ~input-sym))
         (for [[key pattern] keys&patterns]
           (key-ast key pattern))))

(defn regex-ast [regex]
  (and-ast
   (->Guard `(not= nil (re-find ~regex ~input-sym)))
   (->Leave nil)))

(defn predicate-ast [predicate]
  (and-ast
   (->Guard predicate)
   (->Leave nil)))

(defn constructor-ast [constructor arg-patterns]
  (and-ast
   (class-ast constructor)
   (seq-ast
    (->Leave `(vals ~input-sym))
    (apply seqable-ast
           (map head-ast arg-patterns)))))

;; VIEWS

(defn succeed [output rest]
  (if (= nil rest)
    output
    (throw+ ::remaining-input)))

(defn fail []
  (throw+ ::no-matching-pattern))

(defrecord View [view-fn]
  clojure.lang.IFn
  (invoke [this value]
    (view-fn value succeed fail))
  (invoke [this value true-cont]
    (view-fn value true-cont fail))
  (invoke [this value true-cont false-cont]
    (view-fn value true-cont false-cont)))

;; BOOTSTRAPPED PARSER

(defn primitive? [value]
  (or (#{nil true false} value)
      (number? value)
      (string? value)
      (char? value)
      (keyword? value)))

(defn binding? [value]
  (and (symbol? value)
       (re-find #"\?(.+)" (name value))))

(defn binding-name [value]
  (let [[_ string] (re-find #"\?(.+)" (name value))]
    (symbol string)))

(defn constructor? [value]
  (and (symbol? value)
       (re-find #"(.+)\." (name value))))

(defn constructor-name [value]
  (let [[_ string] (re-find #"(.+)\." (name value))]
    (symbol string)))

(defn class-name? [value]
  (and (symbol? value)
       (re-find #"\A(?:[a-z0-9\-]+\.)*[A-Z]\w*\Z" (name value))))

(defn predicate? [value]
  (and (symbol? value)
       (.endsWith (name value) "?")))

(def optional (eval strucjure.bootstrap/optional))
(def zero-or-more (eval strucjure.bootstrap/zero-or-more))
(def one-or-more (eval strucjure.bootstrap/one-or-more))
(declare pattern)
(declare seq-pattern)
(def key&pattern (eval strucjure.bootstrap/key&pattern))
(def pattern (eval strucjure.bootstrap/pattern))
(def seq-pattern (eval strucjure.bootstrap/seq-pattern))

(defn parse [patterns&values]
  (assert (even? (count patterns&values)))
  (apply concat
         (for [[pattern-code value] (partition 2 patterns&values)]
           [(pattern pattern-code) value])))

;; COMPILER API

;; Here true-case is (fn [output rest] code), false-case is code
(defn compile-inline [patterns&values input bindings true-case false-case wrapper]
  (assert (even? (count patterns&values)))
  (let [output (gensym "output__")
        thunks (atom [])
        start (reduce
               (fn [false-branch [pattern value]]
                 (ast->clj pattern input bindings thunks
                           (fn [rest _]
                             `(let [~output ~value]
                                ~(true-case output rest)))
                           false-branch))
               false-case
               (reverse (partition 2 patterns&values)))]
    (expand-thunks
     `(letfn [~@@thunks]
        ~(wrapper start)))))

(defn compile-view [patterns&values bindings wrapper]
  (let [input (gensym "input__")
        true-cont (gensym "true-cont__")
        false-cont (gensym "false-cont__")
        bindings (conj bindings input true-cont false-cont)
        true-case (fn [output rest] (->Thunk '.invoke [true-cont output rest]))
        false-case (->Thunk '.invoke [false-cont])
        wrapper (fn [start] (wrapper `(->View (fn [~input ~true-cont ~false-cont] ~start))))]
    (compile-inline patterns&values input bindings true-case false-case wrapper)))

;; USER API

(defn succeed-inline [output rest]
  (if (= nil rest)
    output
    `(if (= nil ~rest)
       ~output
       (throw+ ::input-remaining))))

(def fail-inline
  `(throw+ ::no-matching-pattern))

(defmacro match [input & patterns&values]
  (compile-inline (parse patterns&values) input [] succeed-inline fail-inline identity))

(defmacro view [& patterns&values]
  (compile-view (parse patterns&values) [] identity))

(defmacro defview [name & patterns&values]
  `(def ~name
     ~(compile-view (parse patterns&values) [] identity)))

(defmacro defnview [name args & patterns&values]
  `(def ~name
     ~(compile-view (parse patterns&values) args (fn [start] `(fn [~@args] ~start)))))

;; BOOTSTRAPPING

(def parser
  ['(defnview optional [elem]
      (and [(elem ?x) (& ?rest)] (leave rest)) x
      (and [(& ?rest)] (leave rest)) nil)

   '(defnview zero-or-more [elem]
      (and [(elem ?x) (& ((zero-or-more elem) ?xs)) (& ?rest)] (leave rest)) (cons x xs)
      (and [(& ?rest)] (leave rest)) nil)

   '(defnview one-or-more [elem]
      (and [(elem ?x) (& ((zero-or-more elem) ?xs)) (& ?rest)] (leave rest)) (cons x xs))

   '(defview key&pattern
      [?key (pattern ?pattern)] [key pattern])

   '(defview pattern
      ;; BINDINGS
      '_ (->Leave nil)
      (and binding? ?binding) (->Bind (binding-name binding))

      ;; LITERALS
      (and primitive? ?primitive) (literal-ast primitive) ; primitives evaluate to themselves, so don't need quoting
      (and class-name? ?class-name) (class-ast class-name)
      (and (or clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap) [(& ((zero-or-more key&pattern) ?keys&patterns))]) (map-ast keys&patterns)
      (and seq? [(and constructor? ?constructor) (& ((zero-or-more pattern) ?arg-patterns))]) (constructor-ast (constructor-name constructor) arg-patterns)

      ;; PREDICATES
      (and java.util.regex.Pattern ?regex) (regex-ast regex)
      (and predicate? ?predicate) (predicate-ast `(~predicate ~input-sym))
      (and seq? [(or 'fn 'fn*) [?arg] (& ?body)]) (predicate-ast `(do ~@(clojure.walk/prewalk-replace {arg input-sym} body)))

      ;; SEQUENCES
      (and vector? [(& ((zero-or-more seq-pattern) ?seq-patterns))]) (seqable-ast seq-patterns)

      ;; SPECIAL FORMS
      (and seq? ['quote ?quoted]) (literal-ast `(quote ~quoted))
      (and seq? ['guard ?form]) (->Guard form)
      (and seq? ['leave ?form]) (->Leave form)
      (and seq? ['and (& ((one-or-more pattern) ?patterns))]) (apply and-ast patterns)
      (and seq? ['seq (& ((one-or-more pattern) ?patterns))]) (apply seq-ast patterns)
      (and seq? ['or (& ((one-or-more pattern) ?patterns))]) (apply or-ast patterns)

      ;; EXTERNAL VARIABLES
      (and symbol? ?variable) (literal-ast variable)

      ;; IMPORTED VIEWS
      ;; (and seq? ['? %predicate (pattern ?pattern)]) (predicate-ast predicate pattern)
      (and seq? [?view (pattern ?pattern)]) (import-ast view pattern))

   '(defview seq-pattern
      ;; & PATTERNS
      (and seq? ['& (pattern ?pattern)]) pattern

      ;; ESCAPED PATTERNS
      (and seq? ['guard ?form]) (->Guard form)

      ;; ALL OTHER PATTERNS
      (pattern ?pattern) (head-ast pattern))])

(defn defview->clj [quoted-defview]
  (match (macroexpand-1 quoted-defview)
         ['def ?name ?value] `(def ~name '~value)))

(defn bootstrap []
  (spit "src/strucjure/bootstrap.clj"
        (with-out-str
          (clojure.pprint/pprint '(ns strucjure.bootstrap))
          (clojure.pprint/pprint `(do ~@(map defview->clj parser))))))

;; TESTS

;; TODO: construct random asts and test that they never throw an error
;; TODO: test that side effects are never repeated
