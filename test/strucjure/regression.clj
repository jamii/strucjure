(ns strucjure.regression
  (:require [clojure.test :refer [deftest is]]
            clojure.walk
            [plumbing.core :refer [aconcat map-vals]]
            strucjure.regression.sandbox))

(def tests-file "./test/strucjure/regression/tests.clj")
(def recorded-tests-file "./test/strucjure/regression/recorded-tests.clj")
(def recorded-results-file "./test/strucjure/regression/recorded-results.clj")

(defn read-all [file]
  (let [reader (java.io.PushbackReader. (clojure.java.io/reader file))]
    (take-while #(not= ::eof %) (repeatedly #(read reader false ::eof)))))

(defn repeatable* [form]
  (cond
   (fn? form) ::fn
   (and (symbol? form) (re-find #"(\d#?|__auto__)$" (str form))) ::gensym
   (var? form) (do (alter-meta! form dissoc :line :column) form) ;; I don't know why these differ :(
   :else form))

(defn repeatable [form]
  (if (instance? clojure.lang.IRecord form)
    (into form (map-vals repeatable form))
    (clojure.walk/walk repeatable repeatable* form)))

(defn eval-test [test]
  (binding [*ns* (find-ns 'strucjure.regression.sandbox)
            *print-meta* true]
    (pr-str (repeatable (eval `(try ~test (catch Throwable e# e#)))))))

(defn reset-results []
  (binding [*print-meta* true]
    (let [tests (read-all tests-file)]
      (spit recorded-tests-file (clojure.string/join "\n" tests))
      (spit recorded-results-file (clojure.string/join "\n" (map eval-test tests))))))

;; (reset-results)

(deftest regression
  (let [tests (read-all tests-file)
        recorded-tests (read-all recorded-tests-file)
        recorded-results (line-seq (clojure.java.io/reader recorded-results-file))]
    (doseq [[test result] (map vector recorded-tests recorded-results)]
      (is (= (eval-test test) result) test))
    (is (= (map repeatable tests) (map repeatable recorded-tests)) "Tests should not have been changed since last (reset-results)")))
