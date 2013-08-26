(ns strucjure.regression
  (:require [clojure.test :refer [deftest is]]
            clojure.walk
            [plumbing.core :refer [map-vals]]
            strucjure.regression.tests))

(def results-file "./test/strucjure/regression/results.clj")

(defn repeatable* [form]
  (cond
   (instance? clojure.lang.Fn form) ::fn
   (clojure.core/and (symbol? form) (re-find #"\d$" (str form))) ::gensym
   :else form))

(defn repeatable [form]
  (if (instance? clojure.lang.IRecord form)
    (into form (map-vals repeatable form))
    (clojure.walk/walk repeatable repeatable* form)))

(defn eval-test [test]
  (binding [*ns* (find-ns 'strucjure.regression.tests)]
    (pr-str (repeatable (eval test)))))

(defn reset-results []
  (spit results-file (clojure.string/join "\n" (map eval-test strucjure.regression.tests/tests))))

;; (reset-results)

(let [this-ns *ns*]
  (deftest regression
    (let [tests strucjure.regression.tests/tests
          results (line-seq (clojure.java.io/reader results-file))]
      (doseq [[test result] (map vector tests results)]
        (is (= (eval-test test) result) test))
      (is (= (count tests) (count results)) "Should usually have the same number of tests and results - may want to (reset-results)"))))
