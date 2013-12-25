(ns strucjure.test
  (:refer-clojure :exclude [with-meta * + or and name case])
  (require [clojure.test :refer [deftest] :as t]
           [plumbing.core :refer [for-map aconcat map-vals]]
           [strucjure.pattern :refer :all]
           [strucjure.view :as view]
           [strucjure.sugar :refer :all]))

;; (t/test-ns 'strucjure.test)

(defrecord Foo [x y])
(defrecord Bar [x y])

(deftest basics
  ;; equality

  (t/is (= (match 1
                :a :fail
                1 :ok
                'c :fail)
         :ok))

  ;; wildcard

  (t/is (= (match 'c
                :a :fail
                1 :fail
                _ :ok)
         :ok))

  ;; is

  (t/is (= (match :x
                (is integer?) :fail
                (is keyword?) :ok)
         :ok))

  ;; ordered choice

  (t/is (= (match :x
                (is integer?) :fail
                (is keyword?) :ok
                :x :not-reached)
         :ok))

  ;; lists

  (t/is (= (match (list 1 2 3)
                (list 1 2) :too-short
                (list 1 2 3 4) :too-long
                (list 1 2 3) :just-right)
         :just-right))

  ;; vecs

  (t/is (= (match [1 2 3]
                [1 2] :too-short
                [1 2 3 4] :too-long
                [1 2 3] :just-right)
         :just-right))

  ;; maps

  (t/is (= (match {:a 1 :b 2}
                {:a 2} :wrong
                {:a 1} :no-b
                {:a 1 :b 2} :exact)
         :no-b ;; map patterns ignore extra keys
         ))

  (t/is (= (match {:a 1 :b 2}
                  {:a 1 :c _} :huh?)
         :huh? ;; missing keys return nil which matches _. this is a deliberate choice to match destructuring semantics
         ))

  (t/is (= (match {:a 1 :b 2}
                  {:a 1 :c not-nil} :fail
                  _ :ok)
         :ok
         ))

  ;; records

  ;; TODO: this matches to (Bar. 1 2) instead of expected (Foo. 1 2)
  ;;  (t/is (= (match (Foo. 1 2)
  ;;                (Bar. 1 2) :fail
  ;;                (Foo. :a :b) :fail
  ;;                (Foo. 1 2) :ok)
  ;;         :ok
  ;;         ))

  (t/is (= (match (Foo. 1 2)
                (->Foo 1 2) :ok)
         :ok
         ))

  ;; TODO something is calling clojure.walk/walk on this record literal :(
  ;;  (t/is (= (match (Foo. 1 2)
  ;;                #strucjure.test.Foo{:x 1 :y 2} :ok)))

  (t/is (= (match (Foo. 1 2)
                {:x 1 :y 2} :ok)))

  ;; metadata

  (t/is (= (match (clojure.core/with-meta [1 2 3] {:foo true})
                (with-meta _ {:foo _}) :ok)
         :ok))

  ;;names

  (t/is (= (match [1 2 3]
                [1 ^x _ 3] x)
         2))

  (t/is (= (match [1 2 3]
                ^x [1 _ 3] x)
         (list 1 2 3) ;; names capture output, not input. vectors and lists both match any Seqable and always output a Seq.
         ))

  ;; guards

  (t/is (= (match [1 2 3]
                (guard [1 ^z _ ^y _] (= z y)) :fail
                [1 ^z _ ^y _] [z y])
         [2 3]))

  ;; booleans

  (t/is (= (match [1 2 3]
                (or [] [1] [1 2] [1 2 3]) :ok)
         :ok))

;; TODO figure out how to make this work with Refers
;;   (t/is (thrown? java.lang.AssertionError ;; may not have different names in different branches of `or`
;;                (macroexpand-1
;;                 '(match [1 2 3]
;;                         (or [] ^x [1] [1 2] [1 2 3]) x))))

  (t/is (= (match {:a 1 :b 2}
                (and [[:a 1] [:b 2]] {:a 1 :b 2}) :ok)
         :ok ;; maps are sequences too
         ))

  )

(deftest repetition

  ;; rest

  (t/is (= (match [1 2 3]
                [1 (& ^z _)] z)
         (list 2 3)))

  (t/is (= (match [1 2 3]
                [1 ^z (& _)] z)
         (list 2 3) ;; regression test - naming a Rest pattern used to not work
         ))

  ;; greedy

  (t/is (= (match [1 2 3 4 5]
                [1 ^x (& [_ _]) ^y (& [_ _])] [x y])
         [(list 2 3) (list 4 5)]))

  ;; repetition

  (t/is (= (match [1 2]
                ^z (* (is integer?)) z)
         (list 1 2)))

  (t/is (= (match [1 2 :x :y :z]
                [^ints (&* (is integer?)) ^keys (&* (is keyword?))] [ints keys])
         [(list 1 2) (list :x :y :z)]))

  (t/is (= (match [1 2 1 2 1 2]
                ^z (*& [1 2]) z)
         (list 1 2 1 2 1 2)))

  (t/is (= (match [1 2 1 2 1 2 3]
                (*& [1 2]) :fail
                _ :ok)
         :ok))

  (t/is (= (match [1 2 1 2 1 2 3]
                [^y (&*& [1 2]) ^z (& _)] [y z])
         [(list 1 2 1 2 1 2) (list 3)]))

  )

(deftest named-patterns

  ;; basic

  (t/is (= (match [[1 2 3] [1 2 3]]
                (letp [foo [1 2 3]]
                      [foo foo] :ok))
         :ok))

  ;; recursion

  (t/is (= (match '(succ (succ (succ zero)))
               (letp [num (or succ zero)
                      succ (case ['succ num] (inc num))
                      zero (case 'zero 0)]
                     num))
         3))

  ;; parsing

  (t/is (= (match [1 2 3 :x :y :z]
                (letp [ints (* (is integer?))
                       keys (* (is keyword?))]
                      ints :fail
                      [(& ints)] :fail
                      [(& ints) (& keys)] :ok))
         :ok ;; tests that let-bound patterns are called with the right parsing context
         ))

  )




