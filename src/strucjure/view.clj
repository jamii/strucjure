(ns strucjure.view
  (:use clojure.test
        [slingshot.slingshot :only [throw+ try+]]))

(defrecord NoMatch [input])
(defrecord PartialMatch [input output rest])

(defn fail [input]
  (throw+ (NoMatch. input)))

(defn succeed [input output rest]
  (if (= nil rest)
    output
    (throw+ (PartialMatch. input output rest))))

(defn view
  ([this input] (view* this input succeed fail))
  ([this input true-case] (view* this input true-case fail))
  ([this input true-case false-case] (view* this input true-case false-case)))

(defrecord Fn [f]
  View
  (view* [this input true-case false-case]
    (true-case input (f input) nil)))

(defn case-view* [views input true-case false-case]
  (if-let [[view & views] views]
    (view* view input true-case
           (fn [_] (case-view* views input true-case false-case)))
    (false-case input)))

(defrecord Case [views]
  View
  (view* [this input true-case false-case]
    (case-view* (seq views) input true-case false-case)))

(extend-protocol View
  java.util.regex.Pattern
  (view* [this input true-case false-case]
    (if-let [output (re-find this input)]
      (true-case input output nil)
      (false-case input))))

(defn view-and-catch [view input]
  (try+
   (view* view input succeed fail)
   (catch (instance? NoMatch %) no-match no-match)
   (catch (instance? PartialMatch %) partial-match partial-match)))

(deftest fn-test
  (is (= 2 (view (->Fn inc) 1))))

(deftest regex-test
  (is (instance? NoMatch (view-and-catch #"foo" "bar"))
      (= ["foo" "oo"] (view #"f(o+)" "f foo foooo"))))

(deftest case-test
  (is (= 2 (view (->Case [(->Fn inc) (->Fn identity)]) 1)))
  (is (= "foo" (view (->Case [#"bar" #"baz" #"foo" #"fo"]) "foo"))))

; (run-tests)
