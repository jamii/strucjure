(ns strucjure
  (:use clojure.test
        [slingshot.slingshot :only [throw+ try+]])
  (:require clojure.set
            clojure.walk
            clojure.core.cache
            [strucjure.view :as view]
            [strucjure.pattern :as pattern]))

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

;; --- API ---

;; will redef this later when bootstrapping
(defn parse-pattern [pattern-src]
  (eval pattern-src))

(defmacro view [& pattern-srcs&result-srcs]
  (assert (even? (count pattern-srcs&result-srcs)))
  `(view/or
    ~@(for [[pattern-src result-src] (partition 2 pattern-srcs&result-srcs)]
        (let [pattern-ast (parse-pattern pattern-src)
              [pattern scope] (pattern/with-scope pattern-ast {})
              result-fun (util/src-with-scope result-src scope)]
          `(view/->Match ~pattern ~result-fun)))))

(defmacro defview [name & pattern-srcs&result-srcs]
  `)
