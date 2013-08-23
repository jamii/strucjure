(ns strucjure.regression
  (:require [clojure.test :refer [deftest is]]
            clojure.walk
            [plumbing.core :refer [map-vals]]))

(def tests-file "./test/strucjure/regression/tests.clj")
(def results-file "./test/strucjure/regression/results.clj")

(defn read-all [filename]
  (with-open [reader (java.io.PushbackReader. (clojure.java.io/reader filename))]
    (loop [acc []]
      (let [form (read reader false ::eof)]
        (if (= ::eof form)
          acc
          (recur (conj acc form)))))))

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
  (spit results-file (clojure.string/join "\n" (map eval-test (read-all tests-file)))))

;; (reset-results)

(let [this-ns *ns*]
  (deftest regression
    (let [tests (read-all tests-file)
          results (line-seq (clojure.java.io/reader results-file))]
      (is (= (count tests) (count results)) "Use (reset-results) if some results are missing")
      (binding [*ns* this-ns]
        (doseq [[test result] (map vector tests results)]
          (is (= (eval-test test) result) test))))))
