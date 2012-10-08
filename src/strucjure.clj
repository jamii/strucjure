(ns strucjure
  (:use clojure.test
        [slingshot.slingshot :only [throw+ try+]])
  (:require strucjure.bootstrap
            clojure.set
            clojure.walk))

;; PEG parser / pattern matcher
;; (originally based on matchure)

;; A pattern takes an input and a set of bindings, consumes some or all of the input and returns new bindings
;; A match takes an input, consumes some or all of the input and returns a value
;; A match is constructed from a list of [pattern value] pairs, where the value forms have access to the patterns bindings

;; TODO
;; fix match indentation in emacs
;; better error/failure reporting
;; provide syntax for matching dicts, records, regexes
;; try to reduce allocation of false-thunks
;; replace true-thunk with (if-let [[...] (or ...)] ...)
;; think about extensibility and memoization

;; pile of thunks codegen method
;; defnmatch can add args to bindings so not expensive

;; UTILS FOR CODEGEN

(def input-sym '%)

;; Used to avoid exponential expansion of code in repeated branches
(defrecord Thunk [name])

(defn unthunk [form]
  (if (= Thunk (class form))
    (:name form)
    form))

;; clojure.walk/prewalk-replace fails if it hits a thunk :(
(defn replace-thunk [thunk body]
  (if (= Thunk (class body))
    (if (= thunk body)
      `(~(:name thunk))
      body)
    (clojure.walk/walk (partial replace-thunk thunk) identity body)))

;; thunkify is safe so long as we never reuse variable names
(defn thunkify* [form body-fn]
  (if (= Thunk (class form))
     ;; already a thunk, just reuse and a higher up thunkify will deal with it
     (body-fn form)
     ;; otherwise, insert a new thunk
     (let [name (gensym "thunk__")
           thunk (->Thunk name)
           body (replace-thunk thunk (body-fn thunk))]
       `(let [~name (fn [] ~form)]
          ~body))))

(defmacro thunkify [sym body]
  `(thunkify* ~sym (fn [~sym] ~body)))

;; PATTERN AST

(defprotocol Ast
  "An Abstract Syntax Tree for a pattern."
  (ast->clj* [this input bindings true-case false-case]
    "Output code which tests the pattern against input with the supplied bindings. If the pattern succeeds, call true case with the remaining input and the new bindings (true-case is (fn [rest bindings] code)). Otherwise call the false-case (false-case is code referencing a thunk)."))

(defn ast->clj [ast input bindings true-case false-case]
  (ast->clj* ast input bindings true-case false-case))

;; Always succeeds. Consumes and transforms input
(defrecord Leave [form]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    (let [left (gensym "left__")]
      `(let [~left (let [~input-sym ~input] ~form)]
         ~(true-case left bindings)))))

;; Succeeds if form evaluates to true. Does not consume anything
(defrecord Guard [form]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    `(if (let [~input-sym ~input] ~form)
       ~(true-case input bindings)
       ~false-case)))

;; Same as (->Guard '(= nil %)) but can sometimes be compiled away
(defrecord GuardNil []
  Ast
  (ast->clj* [this input bindings true-case false-case]
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
  (ast->clj* [this input bindings true-case false-case]
    (cond
      ;; ignore binding
      (= '_ symbol) (true-case nil bindings)
      ;; test for equality
      (contains? bindings symbol) `(if (= ~symbol ~input)
                                     ~(true-case nil bindings)
                                     ~false-case)
      ;; bind symbol
      :else `(let [~symbol ~input]
               ~(true-case nil (conj bindings symbol))))))

;; Calls the match with the current import and runs pattern on its output
;; The pattern must consume the whole output
(defrecord Import [match pattern]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    (thunkify false-case
              (let [output (gensym "output__")
                    rest (gensym "rest__")]
                `((.match-fn ~match)
                  ~input
                  (fn [~output ~rest]
                    ~(ast->clj pattern output bindings
                               (fn [new-rest new-bindings]
                                 (assert (= nil new-rest)) ;; pattern must totally consume import
                                 (true-case rest new-bindings))
                               false-case))
                  ~(unthunk false-case))))))

;; TODO the lack of symmetry between And and Or bothers me
;; Suspect it indicates something is incorrect

;; Common logic for And and Seq - the only difference is how the input is threaded through
(defn sequential-ast [input-choice patterns input bindings true-case false-case]
  (thunkify false-case
              (letfn [(patterns->clj [patterns rest bindings]
                        (if-let [[pattern & patterns] patterns]
                          (ast->clj pattern (input-choice input rest) bindings
                                    (fn [new-rest new-bindings]
                                      (patterns->clj patterns new-rest new-bindings))
                                    false-case)
                          (true-case rest bindings)))]
                (patterns->clj patterns input bindings))))

;; All patterns get the same input
;; All bindings are exported
;; The output is the output from the last pattern
(defrecord And [patterns]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    (sequential-ast (fn [input _] input) patterns input bindings true-case false-case)))

;; Each pattern gets the remaining input from the last pattern
;; All bindings are exported
;; The output is the output from the last pattern
(defrecord Seq [patterns]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    (sequential-ast (fn [_ rest] rest) patterns input bindings true-case false-case)))

;; This is an ugly hack
;; Breaks if any branch does not call true-case exactly once
(defn or-bindings [patterns old-bindings]
  (let [branch-bindings (atom #{})]
      (doseq [pattern patterns]
        (ast->clj pattern :input old-bindings
                  (fn [_ new-bindings]
                    (swap! branch-bindings conj new-bindings))
                  ::false-case))
      (assert (apply = @branch-bindings))
      (if-let [[new-bindings & _] (seq @branch-bindings)]
        (clojure.set/difference new-bindings old-bindings)
        #{})))

;; All patterns get the same input
;; The bindings from each branch are exported and must be the same across all branches
;; The output is the output of the first successful pattern
(defrecord Or [patterns]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    (let [or-bindings (or-bindings patterns bindings)
          all-bindings (clojure.set/union bindings or-bindings)
          true-case-input (gensym "true-case-input__")
          true-case-thunk (gensym "true-case-thunk__")
          true-case-call (fn [branch-rest _]
                           `(~true-case-thunk ~branch-rest ~@or-bindings))]
      (letfn [(patterns->clj [patterns]
                (if-let [[pattern & patterns] patterns]
                  (ast->clj pattern input bindings
                            true-case-call
                            (patterns->clj patterns))
                  false-case))]
        `(let [~true-case-thunk (fn [~true-case-input ~@or-bindings]
                                  ~(true-case true-case-input all-bindings))]
           ~(patterns->clj patterns))))))

;; Runs pattern against the head of the input sequence
;; Outputs the rest of the input sequence
;; Fails if the pattern returns output
;; Does NOT check if input is seqable, will crash if used with non-seqable input
(defrecord Head [pattern]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    (thunkify false-case
              (let [head (gensym "head__")
                    tail (gensym "tail__")]
                `(if-let [[~head & ~tail] ~input]
                   ~(ast->clj pattern head bindings
                              (fn [rest new-bindings]
                                (assert (= nil rest)) ;; pattern must completely consume head
                                (true-case tail new-bindings))
                              false-case)
                   ~false-case)))))

(defrecord Literal [literal]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    `(if (= ~input ~literal)
      ~(true-case nil bindings)
      ~false-case)))

;; COMPILER API

(defn succeed [output rest]
  (if (= nil rest)
    output
    (throw+ ::remaining-input)))

(defn fail []
  (throw+ ::match-failure))

(defrecord Match [match-fn]
  clojure.lang.IFn
  (invoke [this value]
    (match-fn value succeed fail))
  (invoke [this value true-cont]
    (match-fn value true-cont fail))
  (invoke [this value true-cont false-cont]
    (match-fn value true-cont false-cont)))

;; Here true-case is (fn [output rest] code), false-case is code
(defn compile-inline [patterns&values input true-case false-case]
  (assert (even? (count patterns&values)))
  (let [output (gensym "output__")]
    (reduce
     (fn [false-branch [pattern value]]
       (ast->clj pattern input #{}
                 (fn [rest bindings]
                   `(let [~output (let [~input-sym ~rest] ~value)]
                      ~(true-case output rest)))
                 false-branch))
     false-case
     (reverse (partition 2 patterns&values)))))

(defn compile [patterns&values]
  (let [input (gensym "input__")
        true-cont (gensym "true-cont__")
        false-cont (gensym "false-cont__")
        true-case (fn [output rest] `(~true-cont ~output ~rest))
        false-thunk (->Thunk false-cont)]
    `(fn [~input ~true-cont ~false-cont]
       ~(replace-thunk false-thunk
                       (compile-inline patterns&values input true-case false-thunk)))))

;; PATTERN PARSER

(defn seq-ast [patterns]
  (->Seq
   (concat
    [(->Guard '(or (instance? clojure.lang.Seqable %) (nil? %))) (->Leave `(seq ~input-sym))]
    patterns
    [(->GuardNil)])))

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

(defn class-name? [value]
  (and (symbol? value)
       (re-find #"\A(?:[a-z0-9\-]+\.)*[A-Z]\w*\Z" (name value))))

(def optional-syntax
  '[(and [(elem ?x) (& ?rest)] (leave rest)) x
    (and [(& ?rest)] (leave rest)) nil])

(def zero-or-more-syntax
  '[(and [(elem ?x) (& ((zero-or-more elem) ?xs)) (& ?rest)] (leave rest)) (cons x xs)
    (and [(& ?rest)] (leave rest)) nil])

(def one-or-more-syntax
  '[(and [(elem ?x) (& ((zero-or-more elem) ?xs)) (& ?rest)] (leave rest)) (cons x xs)])

(def pattern-syntax
  '[;; BINDINGS
    '_ (->Bind '_)
    (and (guard (binding? %)) ?binding) (->Bind (binding-name binding))

    ;; LITERALS
    (and (guard (primitive? %)) ?literal) (->Literal literal) ; primitives evaluate to themselves, so don't need quoting
    (and (guard (class-name? %)) ?class) (->And [(->Guard `(instance? ~class ~input-sym)) (->Bind '_)])

    ;; SEQUENCES
    (and (guard (vector? %)) [(& ((zero-or-more seq-pattern) ?seq-patterns))]) (seq-ast seq-patterns)

    ;; SPECIAL FORMS
    (and (guard (seq? %)) ['quote ?quoted]) (->Literal `(quote ~quoted))
    (and (guard (seq? %)) ['guard ?form]) (->Guard form)
    (and (guard (seq? %)) ['leave ?form]) (->Leave form)
    (and (guard (seq? %)) ['and (& ((one-or-more pattern) ?patterns))]) (->And patterns)
    (and (guard (seq? %)) ['or (& ((one-or-more pattern) ?patterns))]) (->Or patterns)

    ;; EXTERNAL VARIABLES
    (and (guard (symbol? %)) ?variable) (->Literal variable)

    ;; IMPORTED MATCHES
    (and (guard (seq? %)) [?match (pattern ?pattern)]) (->Import match (->Seq [pattern (->GuardNil)]))])

(def seq-pattern-syntax
  '[;; & PATTERNS
    (and (guard (seq? %)) ['& (pattern ?pattern)]) pattern

    ;; ESCAPED PATTERNS
    (and (guard (seq? %)) ['guard ?form]) (->Guard form)

    ;; ALL OTHER PATTERNS
    (pattern ?pattern) (->Head (->Seq [pattern (->GuardNil)]))])

;; BOOTSTRAPPING

(declare seq-pattern)
(def optional (eval `(fn [~'elem] (->Match ~(compile (read-string strucjure.bootstrap/optional-ast))))))
(def zero-or-more (eval `(fn [~'elem] (->Match ~(compile (read-string strucjure.bootstrap/zero-or-more-ast))))))
(def one-or-more (eval `(fn [~'elem] (->Match ~(compile (read-string strucjure.bootstrap/one-or-more-ast))))))
(def pattern (eval `(->Match ~(compile (read-string strucjure.bootstrap/pattern-ast)))))
(def seq-pattern (eval `(->Match ~(compile (read-string strucjure.bootstrap/seq-pattern-ast)))))

(defn parse [patterns&values]
  (assert (even? (count patterns&values)))
  (apply concat
         (for [[pattern-syntax value] (partition 2 patterns&values)]
           [(pattern pattern-syntax) value])))

;; USER API

(defmacro match [& patterns&values]
  `(->Match ~(compile (parse patterns&values))))

(defmacro defmatch [name & patterns&values]
  `(def ~name
     (match ~@patterns&values)))

(defmacro defnmatch [name args & patterns&values]
  `(def ~name
     (fn [~@args]
       (match ~@patterns&values))))

;; tests

(deftest self-describing
  (is (= (read-string strucjure.bootstrap/optional-ast) (vec (parse optional-syntax))))
  (is (= (read-string strucjure.bootstrap/zero-or-more-ast) (vec (parse zero-or-more-syntax))))
  (is (= (read-string strucjure.bootstrap/one-or-more-ast) (vec (parse one-or-more-syntax))))
  (is (= (read-string strucjure.bootstrap/pattern-ast) (vec (parse pattern-syntax))))
  (is (= (read-string strucjure.bootstrap/seq-pattern-ast) (vec (parse seq-pattern-syntax)))))

;; TODO: construct random asts and test that they never throw an error
;; TODO: test that side effects are never repeated
