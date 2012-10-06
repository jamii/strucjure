(ns strucjure
  (:use clojure.test
        [slingshot.slingshot :only [throw+ try+]])
  (:require clojure.set
            clojure.walk))

;; PEG parser / pattern matcher
;; (originally based on matchure)

;; TODO
;; fix match indentation in emacs
;; better error/failure reporting
;; special-case zero-or-more, one-or-more, optional
;; provide syntax for matching dicts, records, classes, regexes
;; try to reduce allocation of false-thunks
;; replace true-thunk with (if-let [[...] (or ...)] ...)
;; add a README
;; think about extensibility and memoization
;; defmatch name [args] pattern -> def name (eval `(fn [~@args] ~(match* pattern)))
;; eventually can push fn [args] through as many lets as possible

;; A pattern takes an input and a set of bindings, consumes some or all of the input and returns new bindings
;; A match takes an input, consumes some or all of the input and returns a value
;; A match is constructed from a list of [pattern value] pairs

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

(defn if-nil [value true-path false-path]
  (if (= nil value)
    ;; hardcoded to nil, will always succeed
    true-path
    ;; otherwise, need to test at runtime
    `(if (= nil ~value)
       ~true-path
       ~false-path)))

;; PATTERN AST

(defprotocol Ast
  "An Abstract Syntax Tree for a pattern."
  (ast->clj* [this input bindings true-case false-case]
    "Output code which tests the pattern against input with the supplied bindings. If the pattern succeeds, call true case with the remaining input and the new bindings (fn [rest bindings] code). Otherwise call the false-case thunk."))

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
                                 (if-nil new-rest  ;; pattern must totally consume import
                                         (true-case rest new-bindings)
                                         `~false-case))
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
                              (fn [output new-bindings]
                                (if-nil output ;; pattern must completely consume head
                                   (true-case tail new-bindings)
                                   `~false-case))
                              false-case)
                   ~false-case)))))

(defrecord Literal [literal]
  Ast
  (ast->clj* [this input bindings true-case false-case]
    `(if (= ~input '~literal)
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

(defn seq-some [& patterns]
  (->Seq
   (concat
    [(->Guard '(or (sequential? %) (nil? %))) (->Leave `(seq ~input-sym))]
    patterns)))

(defn seq-all [& patterns]
  (->Seq
   (concat
    [(->Guard '(or (sequential? %) (nil? %))) (->Leave `(seq ~input-sym))]
    patterns
    [(->Guard '(nil? %))])))

(def zero-or-more
  (let [elem (gensym "elem__")]
    (eval
     `(fn [~elem]
        (->Match
         ~(compile
           [(seq-some (->Head (->Import elem (->Bind 'x))) (->Import `(zero-or-more ~elem) (->Bind 'xs))) '(cons x xs)
            (seq-some) nil]))))))

(def one-or-more
  (let [elem (gensym "elem__")]
    (eval
     `(fn [~elem]
        (->Match
         ~(compile
           [(seq-some (->Head (->Import elem (->Bind 'x))) (->Import `(zero-or-more ~elem) (->Bind 'xs))) '(cons x xs)]))))))

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

(def pattern-syntax
  '[;; BINDINGS
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
    (and (guard (seq? %)) [?match (pattern ?pattern)]) (->Import match pattern)])

(def seq-pattern-syntax
  '[;; & PATTERNS
    (and (guard (seq? %)) ['& (pattern ?pattern)]) pattern

    ;; ALL OTHER PATTERNS
    (pattern ?pattern) (->Head pattern)])

;; bootstrapped as (parse pattern-syntax)
(def pattern-ast '[#strucjure.Literal{:literal _} (->Bind (quote _)) #strucjure.And{:patterns [#strucjure.Guard{:form (binding? %)} #strucjure.Bind{:symbol binding}]} (->Bind (binding-name binding)) #strucjure.And{:patterns [#strucjure.Guard{:form (primitive? %)} #strucjure.Bind{:symbol literal}]} (->Literal literal) #strucjure.And{:patterns [#strucjure.Guard{:form (vector? %)} #strucjure.Seq{:patterns (#strucjure.Guard{:form (or (sequential? %) (nil? %))} #strucjure.Leave{:form (clojure.core/seq %)} #strucjure.Import{:match (zero-or-more seq-pattern), :pattern #strucjure.Bind{:symbol seq-patterns}} #strucjure.Guard{:form (nil? %)})}]} (apply seq-all seq-patterns) #strucjure.And{:patterns [#strucjure.Guard{:form (seq? %)} #strucjure.Seq{:patterns (#strucjure.Guard{:form (or (sequential? %) (nil? %))} #strucjure.Leave{:form (clojure.core/seq %)} #strucjure.Head{:pattern #strucjure.Literal{:literal quote}} #strucjure.Head{:pattern #strucjure.Bind{:symbol quoted}} #strucjure.Guard{:form (nil? %)})}]} (->Literal quoted) #strucjure.And{:patterns [#strucjure.Guard{:form (seq? %)} #strucjure.Seq{:patterns (#strucjure.Guard{:form (or (sequential? %) (nil? %))} #strucjure.Leave{:form (clojure.core/seq %)} #strucjure.Head{:pattern #strucjure.Literal{:literal guard}} #strucjure.Head{:pattern #strucjure.Bind{:symbol form}} #strucjure.Guard{:form (nil? %)})}]} (->Guard form) #strucjure.And{:patterns [#strucjure.Guard{:form (seq? %)} #strucjure.Seq{:patterns (#strucjure.Guard{:form (or (sequential? %) (nil? %))} #strucjure.Leave{:form (clojure.core/seq %)} #strucjure.Head{:pattern #strucjure.Literal{:literal leave}} #strucjure.Head{:pattern #strucjure.Bind{:symbol form}} #strucjure.Guard{:form (nil? %)})}]} (->Leave form) #strucjure.And{:patterns [#strucjure.Guard{:form (seq? %)} #strucjure.Seq{:patterns (#strucjure.Guard{:form (or (sequential? %) (nil? %))} #strucjure.Leave{:form (clojure.core/seq %)} #strucjure.Head{:pattern #strucjure.Literal{:literal and}} #strucjure.Import{:match (one-or-more pattern), :pattern #strucjure.Bind{:symbol patterns}} #strucjure.Guard{:form (nil? %)})}]} (->And patterns) #strucjure.And{:patterns [#strucjure.Guard{:form (seq? %)} #strucjure.Seq{:patterns (#strucjure.Guard{:form (or (sequential? %) (nil? %))} #strucjure.Leave{:form (clojure.core/seq %)} #strucjure.Head{:pattern #strucjure.Literal{:literal or}} #strucjure.Import{:match (one-or-more pattern), :pattern #strucjure.Bind{:symbol patterns}} #strucjure.Guard{:form (nil? %)})}]} (->Or patterns) #strucjure.And{:patterns [#strucjure.Guard{:form (seq? %)} #strucjure.Seq{:patterns (#strucjure.Guard{:form (or (sequential? %) (nil? %))} #strucjure.Leave{:form (clojure.core/seq %)} #strucjure.Head{:pattern #strucjure.Bind{:symbol match}} #strucjure.Head{:pattern #strucjure.Import{:match pattern, :pattern #strucjure.Bind{:symbol pattern}}} #strucjure.Guard{:form (nil? %)})}]} (->Import match pattern)])

;; bootstrapped as (parse seq-pattern-syntax)
(def seq-pattern-ast '[#strucjure.And{:patterns [#strucjure.Guard{:form (seq? %)} #strucjure.Seq{:patterns (#strucjure.Guard{:form (or (sequential? %) (nil? %))} #strucjure.Leave{:form (clojure.core/seq %)} #strucjure.Head{:pattern #strucjure.Literal{:literal &}} #strucjure.Head{:pattern #strucjure.Import{:match pattern, :pattern #strucjure.Bind{:symbol pattern}}} #strucjure.Guard{:form (nil? %)})}]} pattern #strucjure.Import{:match pattern, :pattern #strucjure.Bind{:symbol pattern}} (->Head pattern)])

(declare seq-pattern)
(def pattern (->Match (eval (compile pattern-ast))))
(def seq-pattern (->Match (eval (compile seq-pattern-ast))))

(defn parse [patterns&values]
  (assert (even? (count patterns&values)))
  (apply concat
         (for [[pattern-syntax value] (partition 2 patterns&values)]
           [(pattern pattern-syntax) value])))

;; tests

(deftest self-describing
  (= pattern-ast (vec (parse pattern-syntax)))
  (= seq-pattern-ast (vec (parse seq-pattern-syntax))))

;; TODO: construct random asts and test that they never throw an error
;; TODO: test that side effects are never repeated
