(ns strucjure
  (:use clojure.test)
  (:require [strucjure.view :as view]
            [strucjure.pattern :as pattern]
            [strucjure.parser :as parser]))

;; PEG parser / pattern matcher
;; (originally based on matchure)

;; --- TODO ---
;; better error/failure reporting
;;   on-view would help with debugging
;; provide syntax for matching record literals #user.Foo{} and set literals
;; allow optional keys?
;; think about extensibility - can copy graph library to make collection of late-bound views
;; might want to truncate input/output/rest in error messages
;; better handling of strings
;;   strings are not instances of clojure.lang.Seqable :(
;;     but support seq...
;;   string patterns should be able to parse prefixes

(def run-pattern pattern/run-or-throw)
(def run-view view/run-or-throw)

(defmacro pattern [& args]
  `(parser/pattern ~@args))
(defmacro defpattern [& args]
  `(parser/defpattern ~@args))
(defmacro defnpattern [& args]
  `(parser/defnpattern ~@args))

(defmacro view [& args]
  `(parser/view ~@args))
(defmacro defview [& args]
  `(parser/defview ~@args))
(defmacro defnview [& args]
  `(parser/defnview ~@args))

(defmacro match [input & rest]
  `(run-view (view ~@rest) ~input))

(def zero-or-more view/zero-or-more)
(def zero-or-more-prefix view/zero-or-more-prefix)

(defnview one-or-more [elem]
  (prefix (elem ?result) & ((zero-or-more elem) ?results)) (cons result results))

(defnview one-or-more-prefix [elem]
  (prefix & (elem ?result) & ((zero-or-more-prefix elem) ?results)) (cons result results))

(defnview optional [elem]
  (prefix (elem ?result)) result
  (prefix) nil)
