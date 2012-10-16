(ns strucjure.view
  (:use clojure.test
        [slingshot.slingshot :only [throw+ try+]]))

(defprotocol View
  "A view takes an input and either fails or consumes some or all of the input and produces a value"
  (run* [this input true-case false-case]
    "Apply the view to input. If the view succeeds, call (true-case output rest). Otherwise call (false-case)."))

(defrecord NoMatch [view input])
(defrecord PartialMatch [view input output rest])

(defn fail [view input]
  (throw+ (NoMatch. view input)))

(defn succeed [view input output rest]
  (if (= nil rest)
    output
    (throw+ (PartialMatch. view input output rest))))

(defn run
  ([this input] (run* this input (partial succeed this input) (partial fail this input)))
  ([this input true-case] (run* this input true-case (partial fail this input)))
  ([this input true-case false-case] (run* this input true-case false-case)))

(defn matches? [this input]
  (run* this input (fn [_ _] true) (fn [] false)))

(defn replace [view input]
  (run* view input (fn [output _] output) (fn [] input)))

(defn expand [view input]
  (run* view input (fn [output _] (expand view output)) (fn [] input)))

(defrecord Fn [f]
  View
  (run* [this input true-case false-case]
    (f input true-case false-case)))

(extend-protocol View
  java.util.regex.Pattern
  (run* [this input true-case false-case]
    (if-let [output (and (instance? java.lang.CharSequence input)
                         (re-find this input))]
      (true-case output nil)
      (false-case))))

(defn case-view* [views input true-case false-case]
  (if-let [[view & views] views]
    (run* view input true-case
           (fn [] (case-view* views input true-case false-case)))
    (false-case)))

(defrecord Case [views]
  View
  (run* [this input true-case false-case]
    (case-view* (seq views) input true-case false-case)))

(defn case [& views]
  (->Case (flatten
           (for [view views]
             (if (instance? Case view)
               (:views view)
               view)))))

(defn extend* [view-var & views]
  (alter-var-root view-var (apply case views)))

(defmacro extend [view & views]
  `(extend* (var ~view) ~@views))

(defmacro post-extend [view & views]
  `(extend view view ~@views))

(defmacro pre-extend [view & views]
  `(extend view ~@views view))

(defmacro extending [extensions & body]
  (assert (even? (count extensions)))
  (let [bindings (for [[var views] (partition 2 extensions)]
                   [var `(case ~@views)])]
    `(binding [~@(apply concat bindings)] ~@body)))

;; TESTS

(defn wrap [f]
  (->Fn (fn [input true-case _]
          (true-case (f input) nil))))

(defn run-and-catch [view input]
  (try+
   (run view input)
   (catch (instance? NoMatch %) no-match no-match)
   (catch (instance? PartialMatch %) partial-match partial-match)))

(deftest fn-test
  (is (= 2 (run (wrap inc) 1))))

(deftest regex-test
  (is (instance? NoMatch (run-and-catch #"foo" "bar"))
      (= ["foo" "oo"] (run #"f(o+)" "f foo foooo"))))

(deftest case-test
  (is (= 2 (run (case (wrap inc) (wrap identity)) 1)))
  (is (= "foo" (run (case #"bar" #"baz" #"foo" #"fo") "foo")))
  (is (= (case 1 2 3 4) (case (case 1 2 3 4)) (case (case 1 2) 3 (case 4)))))

(declare ^:private ^:dynamic extendable)

(deftest extend-test
  (extending [extendable [#"bar"]]
             (is (= "bar" (run extendable "bar")))
             (extending [extendable [#"foo" extendable #"bartender"]]
                        (is (= "foo" (run extendable "foo")))
                        (is (= "bar" (run extendable "bar"))))))
