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
  ([this input] (run* this input (partial succeed view input) (partial fail view input)))
  ([this input true-case] (run* this input true-case (partial fail view input)))
  ([this input true-case false-case] (run* this input true-case false-case)))

(defn replace [view input]
  (run* view input (fn [output _] output) (fn [] input)))

(defrecord Empty []
  View
  (run* [this input true-case false-case]
    (false-case)))

(defrecord Fn [f]
  View
  (run* [this input true-case false-case]
    (true-case (f input) nil)))

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

(defmacro extend [view-var & views]
  `(alter-var-root (var ~view-var) (case ~@views)))

(defmacro extending [extensions & body]
  (assert (even? (count extensions)))
  (let [bindings (for [[var views] (partition 2 extensions)]
                   [var `(case ~@views)])]
    `(binding [~@(apply concat bindings)] ~@body)))

(extend-protocol View
  java.util.regex.Pattern
  (run* [this input true-case false-case]
    (if-let [output (re-find this input)]
      (true-case output nil)
      (false-case))))

;; TESTS

(defn run-and-catch [view input]
  (try+
   (run view input)
   (catch (instance? NoMatch %) no-match no-match)
   (catch (instance? PartialMatch %) partial-match partial-match)))

(deftest fn-test
  (is (= 2 (run (->Fn inc) 1))))

(deftest regex-test
  (is (instance? NoMatch (run-and-catch #"foo" "bar"))
      (= ["foo" "oo"] (run #"f(o+)" "f foo foooo"))))

(deftest case-test
  (is (= 2 (run (case (->Fn inc) (->Fn identity)) 1)))
  (is (= "foo" (run (case #"bar" #"baz" #"foo" #"fo") "foo")))
  (is (= (case 1 2 3 4) (case (case 1 2 3 4)) (case (case 1 2) 3 (case 4)))))

(declare ^:private ^:dynamic extendable)

(deftest extend-test
  (extending [extendable [#"bar"]]
             (is (= "bar" (run extendable "bar")))
             (extending [extendable [#"foo" extendable #"bartender"]]
                        (is (= "foo" (run extendable "foo")))
                        (is (= "bar" (run extendable "bar"))))))

(run-tests)
