(ns strucjure
  (:use clojure.test
        [slingshot.slingshot :only [throw+ try+]])
  (:require clojure.set
            clojure.walk
            clojure.core.cache))

;; PEG parser / pattern matcher
;; (originally based on matchure)

;; A pattern takes an input and a set of bindings, consumes some or all of the input and returns new bindings
;; A view takes an input, consumes some or all of the input and returns a value
;; A view is constructed from a list of [pattern value] pairs, where the value forms have access to the patterns bindings

;; --- TODO ---
;; better error/failure reporting
;; provide syntax for matching record literals #user.Foo{} and set literals
;; allow optional keys?
;; think about extensibility and memoization
;; might want to truncate input/output/rest in error messages

;; --- VIEWS ---

(defrecord NoMatch [view input])
(defrecord PartialMatch [view input output rest])

(defn fail [view input]
  (throw+ (NoMatch. view input)))

(defn succeed [view input output rest]
  (if (= nil rest)
    output
    (throw+ (PartialMatch. view input output rest))))

(defrecord View [src fun]
  clojure.lang.IFn
  (invoke [this input]
    (fun input (partial succeed src input) (partial fail src input)))
  (invoke [this input true-cont]
    (fun input true-cont (partial fail src input)))
  (invoke [this input true-cont false-cont]
    (fun input true-cont false-cont)))

(defn matches? [view input]
  (view input (fn [_ _] true) (fn [] false)))

;; --- UTILS ---

(defn walk [inner form]
  (cond
   (instance? clojure.lang.IRecord form) (clojure.lang.Reflector/invokeConstructor (class form) (to-array (map inner (vals form))))
   (list? form) (apply list (map inner form))
   (instance? clojure.lang.IMapEntry form) (vec (map inner form))
   (seq? form) (doall (map inner form))
   (coll? form) (into (empty form) (map inner form))
   :else form))

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

;; --- THUNKS ---
;; Used to avoid exponential expansion of code in repeated branches

(defn symbols [form]
  (cond
   (symbol? form) #{form}
   (instance? clojure.lang.Seqable form) (apply clojure.set/union (map symbols form))
   :else #{}))

(defn filter-used [args form]
  (vec (filter (symbols form) args)))

;; shallow things are *roughly* no larger than the thunk that would replace them
(defn shallow? [form]
  (or (flat? form)
      (every? flat? (macroexpand form))))

(defn thunkify [thunks args form]
  (if (shallow? form)
      form
      (let [args (filter-used args form)
            name (gensym "thunk__")
            thunk `(~name ~@args)
            thunk-fn `(~name ~args ~form)]
        (swap! thunks conj thunk-fn)
        thunk)))

;; --- COMPILER STAGES ---

;; Low-level AST forms correspond directly to clj code

(defrecord State [input bindings thunks])

(defprotocol LAST
  "A low-level Abstract Syntax Tree for a pattern."
  (last->clj* [this state true-case false-case]
    "Output code which tests the pattern against input with the supplied bindings. May add new thunks to the list. The success branch should be ~(true-case rest new-bindings) and the failure branch should be ~false-case."))

(defn last->clj [last state true-case false-case]
  (last->clj* last state true-case false-case))

;; High-level AST forms behave like macros which expand to low-level AST forms

(defprotocol HAST
  "A high-level Abstract Syntax Tree for a pattern"
  (hast->last* [this]
    "Expands to a mixture of HASTs and LASTs. Called recursively, like macroexpansion."))

(defn hast->last [form]
  (if (instance? strucjure.HAST form)
    (hast->last (hast->last* form))
    (walk hast->last form)))

;; The pattern syntax is recognised directly by the pattern->hast view

(declare seq-pattern->hast)

(declare pattern->hast)

(declare case->hast) ; defined later using pattern->hast

;; --- COMPILER ---

(def input-sym '%)

(defn replace-input-sym [input form]
  (clojure.walk/prewalk-replace {input-sym input} form))

(defn compile-inline [hast input bindings wrapper]
  (let [thunks (atom [])
        state (->State input bindings thunks)
        unreachable ::unreachable ; this will only be reached if the hast has a branch without Succeed/Fail
        start (-> hast
                  hast->last
                  (last->clj state (fn [_ _] unreachable) unreachable))]
    `(letfn [~@@thunks] ~(wrapper start))))

(defn compile-view
  ([patterns&values src bindings wrapper]
     (compile-view (gensym "input") patterns&values src bindings wrapper))
  ([input patterns&values src bindings wrapper]
     (let [true-cont (gensym "true-cont")
           false-cont (gensym "false-cont")
           bindings (conj bindings input true-cont false-cont)
           true-case (fn [output rest] `(~true-cont ~output ~rest))
           false-case `(~false-cont)
           hast (case->hast patterns&values true-case false-case)
           wrapper (fn [start] (wrapper `(->View '~src (fn [~input ~true-cont ~false-cont] ~start))))]
       (compile-inline hast input bindings wrapper))))

(defmacro view [& patterns&values]
  (compile-view patterns&values `(view ~@patterns&values) #{} identity))

;; inserting ^:dynamic directly into a syntax-quote doesn't work, it seems to be applied at read-time
(defn dynamic [symbol]
  (vary-meta symbol assoc :dynamic true))

(defmacro defview [name & patterns&values]
  `(def ~(dynamic name)
     ~(compile-view patterns&values
                    `(defview ~name ~@patterns&values)
                    #{} identity)))

(defmacro defnview [name args & patterns&values]
  `(def ~(dynamic name)
     ~(compile-view patterns&values
                    `(defnview ~name ~args ~@patterns&values)
                    (set args) (fn [start] `(fn [~@args] ~start)))))

;; --- LOW-LEVEL AST ---

;; Always succeeds. Consumes and transforms input
(defrecord Leave [form]
  LAST
  (last->clj* [this {:keys [input bindings]} true-case false-case]
    (let [form (replace-input-sym input form)
          left (gensym "left")]
      (if (flat? form)
        (true-case form bindings)
        `(let [~left ~form]
           ~(true-case left (conj bindings left)))))))

;; Succeeds if form evaluates to true. Does not consume anything
(defrecord Guard [form]
  LAST
  (last->clj* [this {:keys [input bindings]} true-case false-case]
    `(if ~(replace-input-sym input form)
       ~(true-case input bindings)
       ~false-case)))

;; Same as (->GuardNil) but can sometimes be compiled away
(defrecord GuardNil []
  LAST
  (last->clj* [this {:keys [input bindings]} true-case false-case]
    (if (= nil input)
      ;; Hardcoded to nil, will always succeed
      (true-case nil bindings)
      ;; Otherwise, need to test at runtime
      `(if (= nil ~input)
         ~(true-case nil bindings)
         ~false-case))))

;; If symbol is already bound, tests for equality.
;; Otherwise binds input to symbol
;; Always consumes all input
(defrecord Bind [symbol]
  LAST
  (last->clj* [this {:keys [input bindings]} true-case false-case]
    (if (contains? bindings symbol)
      ;; Test for equality
      `(if (= ~symbol ~input)
         ~(true-case nil bindings)
         ~false-case)
      ;; Bind symbol
      `(let [~symbol ~input]
         ~(true-case nil (conj bindings symbol))))))

;; Calls the view with the current input and runs pattern on its output
;; The pattern must consume the whole output
(defrecord Import* [view pattern]
  LAST
  (last->clj* [this {:keys [input bindings thunks] :as state} true-case false-case]
    (let [import (gensym "import")
          rest (gensym "rest")
          pattern-true-case (fn [_ new-bindings]
                              (true-case rest (conj new-bindings rest)))
          pattern-false-case (thunkify thunks (conj bindings input) false-case)
          view-true-case  `(fn [~import ~rest]
                             ~(last->clj pattern (assoc state :input import) pattern-true-case pattern-false-case))
          view-false-case `(fn [] ~pattern-false-case)]
      `(~view ~input ~view-true-case ~view-false-case))))

;; All patterns get the same input
;; All bindings are exported
;; The output is the output from the last pattern
(defrecord And* [pattern-a pattern-b]
  LAST
  (last->clj* [this {:keys [bindings thunks] :as state} true-case false-case]
    (let [false-case (thunkify thunks bindings false-case)]
    (last->clj pattern-a state
               (fn [rest new-bindings]
                 (last->clj pattern-b
                            (assoc state :bindings new-bindings)
                            true-case false-case))
               false-case))))

;; Each pattern gets the remaining input from the last pattern
;; All bindings are exported
;; The output is the output from the last pattern
(defrecord Seq* [pattern-a pattern-b]
  LAST
  (last->clj* [this {:keys [bindings thunks] :as state} true-case false-case]
    (let [false-case (thunkify thunks bindings false-case)]
      (last->clj pattern-a state
                (fn [rest new-bindings]
                  (last->clj pattern-b
                             (assoc state :input rest :bindings (conj new-bindings rest))
                             true-case false-case))
                false-case))))

;; Each pattern gets the same input
;; No bindings are exported (TODO: allow exporting shared bindings)
;; The output is the output from the first successful pattern
(defrecord Or* [pattern-a pattern-b]
  LAST
  (last->clj* [this {:keys [bindings thunks] :as state} true-case false-case]
    (let [true-case-input (gensym "true-case-input__")
          true-case-bindings (conj bindings true-case-input)
          true-case-thunk (thunkify thunks true-case-bindings (true-case true-case-input true-case-bindings))
          true-case (fn [rest _] (clojure.walk/prewalk-replace {true-case-input rest} true-case-thunk))]
      (last->clj pattern-a state true-case
                (last->clj pattern-b state true-case false-case)))))

;; Fails if the inner pattern succeeds and vice versa
;; No bindings are exported (TODO: allow exporting not-bindings)
;; The input is consumed
(defrecord Not [pattern]
  LAST
  (last->clj* [this {:keys [bindings] :as state} true-case false-case]
    (last->clj pattern state
              (fn [_ _] false-case)
              (true-case nil bindings))))

;; Breaks out of the decision tree and returns a value
(defrecord Succeed [view-true-case]
  LAST
  (last->clj* [this {:keys [input]} true-case false-case]
    (view-true-case input)))

;; Does what it says on the tin
(defrecord Fail [view-false-case]
  LAST
  (last->clj* [this state true-case false-case]
    view-false-case))

;; Kind of hacky making this a full-blown LAST
(defrecord Doseq* [pattern seq]
  LAST
  (last->clj* [this {:keys [bindings] :as state} true-case false-case]
    (let [elem (gensym "elem")
          bindings (conj bindings elem)
          state (assoc state :input elem :bindings bindings)]
      `(doseq [~elem ~seq]
         ~(last->clj pattern state true-case nil)))))

;; --- HIGH-LEVEL AST ---

(defrecord Or [patterns]
  HAST
  (hast->last* [this]
    (reduce ->Or* patterns)))

(defrecord And [patterns]
  HAST
  (hast->last* [this]
    (reduce ->And* patterns)))

(defrecord Seq [patterns]
  HAST
  (hast->last* [this]
    (reduce ->Seq* patterns)))

(defn or-ast [& patterns] (->Or patterns))
(defn and-ast [& patterns] (->And patterns))
(defn seq-ast [& patterns] (->Seq patterns))

(defrecord Import [view pattern]
  HAST
  (hast->last* [this]
    (->Import* view (seq-ast pattern (->GuardNil)))))

(defrecord Literal [literal]
  HAST
  (hast->last* [this]
    (->Seq [(->Guard `(= ~literal ~input-sym))
            (->Leave nil)])))

(defrecord Head [pattern]
  HAST
  (hast->last* [this]
    (seq-ast (->Guard `(not= nil ~input-sym))
             (and-ast (seq-ast (->Leave `(first ~input-sym))
                               pattern
                               (->GuardNil))
                      (->Leave `(next ~input-sym))))))

(defrecord Instance [class-name]
  HAST
  (hast->last* [this]
    (seq-ast (->Guard `(instance? ~class-name ~input-sym))
             (->Leave nil))))

(defrecord Prefix [patterns]
  HAST
  (hast->last* [this]
    (->Seq
     (flatten
      [(->Guard `(or (instance? clojure.lang.Seqable ~input-sym) (nil? ~input-sym)))
       (->Leave `(seq ~input-sym))
       (into [] patterns)]))))

(defn prefix-ast [& patterns] (->Prefix patterns))

(defrecord Seqable [patterns]
  HAST
  (hast->last* [this]
    (seq-ast
     (->Prefix patterns)
     (->GuardNil))))

(defn seqable-ast [& patterns] (->Seqable patterns))

(defrecord Key [key pattern]
  HAST
  (hast->last* [this]
    (seq-ast
     (->Leave `(get ~input-sym ~key ::not-found))
     (->Guard `(not= ::not-found ~input-sym))
     pattern
     (->GuardNil))))

(defrecord Map [keys&patterns]
  HAST
  (hast->last* [this]
    (->And (cons
            (->Guard `(instance? clojure.lang.Associative ~input-sym))
            (for [[key pattern] keys&patterns]
              (->Key key pattern))))))

(defrecord Regex [regex]
  HAST
  (hast->last* [this]
    (and-ast
     (->Guard `(not= nil (re-find ~regex ~input-sym)))
     (->Leave nil))))

(defrecord Predicate [predicate]
  HAST
  (hast->last* [this]
    (and-ast
     (->Guard predicate)
     (->Leave nil))))

(defrecord Constructor [constructor arg-patterns]
  HAST
  (hast->last* [this]
    (and-ast
     (->Instance constructor)
     (seq-ast
      (->Leave `(vals ~input-sym))
      (->Seqable (map ->Head arg-patterns))))))

;; --- PARSER UTILS ---

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

;; --- BOOTSTRAP PARSER ---
;; We write HASTs directly to build up a basic parser and then use that to write the real parser

;; Temporary definition, until we have a basic parser
(defn case->hast [hasts&values true-case false-case]
  (assert (even? (count hasts&values)))
  (->Or
   (flatten
    [(for [[hast value] (partition 2 hasts&values)]
       (seq-ast (eval hast) (->Succeed (partial true-case value))))
     (->Fail false-case)])))

(defnview zero-or-more [elem]
  (prefix-ast (->Head (->Import 'elem (->Bind 'x)))
              (->Import '(zero-or-more elem) (->Bind 'xs)))
  (cons x xs)

  (prefix-ast)
  nil)

(defnview two-or-more [elem]
  (prefix-ast (->Head (->Import 'elem (->Bind 'x1)))
              (->Head (->Import 'elem (->Bind 'x2)))
              (->Import '(zero-or-more elem) (->Bind 'xs)))
  (cons x1 (cons x2 xs)))

(defnview zero-or-more-prefix [elem]
  (prefix-ast (->Import 'elem (->Bind 'x))
              (->Import '(zero-or-more-prefix elem) (->Bind 'xs)))
  (cons x xs)

  (prefix-ast)
  nil)

(defview seq-pattern->hast
  ;; & PATTERNS

  (prefix-ast (->Head (->Literal ''&))
              (->Head (->Import 'pattern->hast (->Bind 'pattern))))
  pattern

  ;; ESCAPED PATTERNS

  (prefix-ast (->Head (->Import 'pattern->hast (->Bind 'pattern))))
  (->Head pattern))

(defview pattern->hast
  ;; BINDINGS

  (and-ast (->Predicate `(binding? ~input-sym))
           (->Bind 'binding))
  (->Bind (binding-name binding))

  ;; LITERALS

  (and-ast (->Predicate `(primitive? ~input-sym))
           (->Bind 'primitive))
  (->Literal primitive)

  (and-ast (->Predicate `(class-name? ~input-sym))
           (->Bind 'class-name))
  (->Instance class-name)

  ;; PREDICATES

  (and-ast (->Predicate `(predicate? ~input-sym))
           (->Bind 'predicate))
  (->Predicate `(~predicate ~input-sym))

  ;; SEQUENCES

  (and-ast (->Predicate `(vector? ~input-sym))
           (seqable-ast (->Import '(zero-or-more-prefix seq-pattern->hast) (->Bind 'seq-patterns))))
  (->Seqable seq-patterns)

  (and-ast (->Predicate `(seq? ~input-sym))
           (seqable-ast (->Head (->Literal ''prefix))
                        (->Import '(zero-or-more-prefix seq-pattern->hast) (->Bind 'seq-patterns))))
  (->Prefix seq-patterns)

  ;; SPECIAL FORMS

  (and-ast (->Predicate `(seq? ~input-sym))
           (seqable-ast (->Head (->Literal ''quote))
                        (->Head (->Bind 'quoted))))
  (->Literal `(quote ~quoted))

  (and-ast (->Predicate `(seq? ~input-sym))
           (seqable-ast (->Head (->Literal ''and))
                        (->Import '(two-or-more pattern->hast) (->Bind 'patterns))))
  (->And patterns)

  (and-ast (->Predicate `(seq? ~input-sym))
           (seqable-ast (->Head (->Literal ''or))
                        (->Import '(two-or-more pattern->hast) (->Bind 'patterns))))
  (->Or patterns)

  ;; IMPORTED VIEWS

  (and-ast (->Predicate `(seq? ~input-sym))
           (seqable-ast (->Head (->Bind 'view))
                        (->Head (->Import 'pattern->hast (->Bind 'pattern)))))
  (->Import view pattern))

;; --- REAL PARSER ---

(defn case->hast [patterns&values true-case false-case]
  (assert (even? (count patterns&values)))
  (->Or
   (flatten
    [(for [[pattern value] (partition 2 patterns&values)]
       (seq-ast (pattern->hast pattern) (->Succeed (partial true-case value))))
     (->Fail false-case)])))

(defnview optional [elem]
  (prefix (elem ?x)) x
  (prefix) nil)

(defnview zero-or-more [elem]
  (prefix (elem ?x) & ((zero-or-more elem) ?xs)) (cons x xs)
  (prefix) nil)

(defnview one-or-more [elem]
  (prefix (elem ?x) & ((zero-or-more elem) ?xs)) (cons x xs))

(defnview two-or-more [elem]
  (prefix (elem ?x1) (elem ?x2) & ((zero-or-more elem) ?xs)) (cons x1 (cons x2 xs)))

(defnview zero-or-more-prefix [elem]
  (prefix & (elem ?x) & ((zero-or-more-prefix elem) ?xs)) (cons x xs)
  (prefix) nil)

(defnview one-or-more-prefix [elem]
  (prefix & (elem ?x) & ((zero-or-more-prefix elem) ?xs)) (cons x xs))

(defview key&pattern->hast
  [?key (pattern->hast ?pattern)] [key pattern])

(defview seq-pattern->hast
  ;; & PATTERNS
  (prefix '& (pattern->hast ?pattern)) pattern

  ;; ESCAPED PATTERNS
  (prefix (and seq? ['guard ?form])) (->Guard form)

  ;; ALL OTHER PATTERNS
  (prefix (pattern->hast ?pattern)) (->Head pattern))

(defview pattern->hast
  ;; BINDINGS
  '_ (->Leave nil)
  (and binding? ?binding) (->Bind (binding-name binding))

  ;; LITERALS
  (and primitive? ?primitive) (->Literal primitive) ; primitives evaluate to themselves, so don't need quoting
  (and class-name? ?class-name) (->Instance class-name)
  (and (or clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap) [& ((zero-or-more key&pattern->hast) ?keys&patterns)])
    (->Map keys&patterns)
  (and seq? [(and constructor? ?constructor) & ((zero-or-more pattern->hast) ?arg-patterns)])
    (->Constructor (constructor-name constructor) arg-patterns)

  ;; PREDICATES
  (and java.util.regex.Pattern ?regex) (->Regex regex)
  (and predicate? ?predicate) (->Predicate `(~predicate ~input-sym))
  (and seq? [(or 'fn 'fn*) [] & ?body]) (->Predicate `(do ~@body))
  (and seq? [(or 'fn 'fn*) [?arg] & ?body]) (->Predicate `(do ~@(clojure.walk/prewalk-replace {arg input-sym} body)))

  ;; SEQUENCES
  (and vector? [& ((zero-or-more-prefix seq-pattern->hast) ?seq-patterns)]) (->Seqable seq-patterns)
  (and seq? ['prefix & ((zero-or-more-prefix seq-pattern->hast) ?seq-patterns)]) (->Prefix seq-patterns)

  ;; SPECIAL FORMS
  (and seq? ['quote ?quoted]) (->Literal `(quote ~quoted))
  (and seq? ['guard ?form]) (->Guard form)
  (and seq? ['leave ?form]) (->Leave form)
  (and seq? ['and & ((two-or-more pattern->hast) ?patterns)]) (->And patterns)
  (and seq? ['seq & ((two-or-more pattern->hast) ?patterns)]) (->Seq patterns)
  (and seq? ['or & ((two-or-more pattern->hast) ?patterns)]) (->Or patterns)
  (and seq? ['not (pattern->hast ?pattern)]) (->Not pattern)

  ;; EXTERNAL VARIABLES
  (and symbol? ?variable) (->Literal variable)

  ;; IMPORTED VIEWS
  (and seq? [?view (pattern->hast ?pattern)]) (->Import view pattern))

;; --- MATCH FORMS ---

(defn succeed-inline [src input output rest]
  (if (= nil rest)
    output
    `(if (= nil ~rest)
       ~output
       (throw+ (->PartialMatch '~src ~input ~output ~rest)))))

(defn fail-inline [src input]
  `(throw+ (->NoMatch '~src ~input)))

(defn compile-match [value patterns&values]
  (let [src `(match ~value ~@patterns&values)
        input (gensym "input")
        true-case (partial succeed-inline src input)
        false-case (fail-inline src input)
        hast (case->hast patterns&values true-case false-case)
        wrapper (fn [start]
                      (if (flat? value)
                        (clojure.walk/prewalk-replace {input value} start)
                        `(let [~input ~value] ~start)))]
    (compile-inline hast input #{input} wrapper)))

(defmacro match [value & patterns&values]
  (compile-match value patterns&values))

;; TODO: we can give per-binding failure messages if we can insert a Fail without losing bindings

(defn let->hast [patterns&values body true-case false-case]
  (assert (even? (count patterns&values)))
  (or-ast
   (->Seq
    (flatten
     [(for [[pattern value] (partition 2 patterns&values)]
        (seq-ast
         (->Leave value)
         (pattern->hast pattern)
         (->GuardNil)))
      (->Succeed (partial true-case `(do ~@body)))]))
   (->Fail false-case)))

(defn compile-let [patterns&values body]
  (let [src `(let-match ~patterns&values ...)
        input (vec (take-nth 2 (rest patterns&values)))
        true-case (partial succeed-inline src input)
        false-case (fail-inline src input)
        hast (let->hast patterns&values body true-case false-case)]
    (compile-inline hast nil #{} identity)))

(defmacro let-match [patterns&values & body]
  (compile-let patterns&values body))

(defn doseq->hast [patterns&values body true-case]
  (assert (even? (count patterns&values)))
  (reduce
   (fn [hast [pattern value]]
     (->Doseq* (->Seq* (pattern->hast pattern) hast) value))
   (->Succeed (partial true-case `(do ~@body)))
   (reverse (partition 2 patterns&values))))

(defn compile-doseq [patterns&values body]
  (let [src `(doseq-match ~patterns&values ...)
        input (vec (take-nth 2 (rest patterns&values)))
        true-case (partial succeed-inline src input)
        hast (doseq->hast patterns&values body true-case)]
    (compile-inline hast nil #{} identity)))

(defmacro doseq-match [patterns&values & body]
  (compile-doseq patterns&values body))

;; A degenerate view
(defn compile-pattern [patterns src bindings wrapper]
  (let [input (gensym "input")
        patterns&values (apply concat (for [pattern patterns] [pattern input]))]
    (compile-view input patterns&values src bindings wrapper)))

(defmacro pattern [& patterns]
  (compile-pattern patterns `(pattern ~@patterns) #{} identity))

(defmacro defpattern [name & patterns]
  `(def ~(dynamic name)
     ~(compile-pattern patterns
                       `(defpattern ~name ~@patterns)
                       #{} identity)))

(defmacro defnpattern [name args patterns]
  `(def ~(dynamic name)
     ~(compile-pattern patterns
                       `(defnpattern ~name ~args ~@patterns)
                       (set args) (fn [start] `(fn [~@args] ~start)))))


;; --- GENERIC TRAVERSALS ---

;; memoisation
;; NOTE: to memoise recursive calls you need to rebind the var
;;       eg (binding [my-view (with-cache cache my-view)] ...)
(defn with-cache [cache {:keys [src fun]}]
  (let [cache-atom (atom cache)
        new-src `(with-cache* ~cache ~src)] ; TODO: this produces weird source for defnview
    (letfn [(new-fun [input true-case false-case]
              (if (clojure.core.cache/has? @cache-atom input)
                (do (swap! cache-atom clojure.core.cache/hit input)
                    (if-let [[output rest] (clojure.core.cache/lookup @cache-atom input)]
                      (true-case output rest)
                      (false-case)))
                (fun input
                     (fn [output rest]
                       (swap! cache-atom clojure.core.cache/miss input [output rest])
                       (true-case output rest))
                     (fn []
                       (swap! cache-atom clojure.core.cache/miss input nil)
                       false-case))))]
      (->View new-src new-fun))))

(defn binding* [vars&values body]
  (push-thread-bindings (apply hash-map vars&values))
  (try
    (body)
    (finally
      (pop-thread-bindings))))

(defn caching [cache view-vars body]
  (binding* (apply concat
                   (for [view-var view-vars]
                     [view-var (with-cache cache @view-var)]))
            body))

;; --- TESTS ---

(deftest self-describing
  (is (macroexpand-1 (:src seq-pattern->hast)))
  (is (macroexpand-1 (:src pattern->hast))))
