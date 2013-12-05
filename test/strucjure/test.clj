(ns strucjure.test
  (:refer-clojure :exclude [case])
  (:use clojure.test)
  (require [plumbing.core :refer [for-map aconcat map-vals]]
           [strucjure.pattern :refer :all]
           [strucjure.view :as view]
           [strucjure.sugar :refer [pattern match match-with trace-let trace-all letp case]]))

;; (test-ns 'strucjure.test)

(defrecord Foo [x y])
(defrecord Bar [x y])

(deftest basics
  ;; equality
  (is (= (match 1
                :a :fail
                1 :ok
                'c :fail)
         :ok))

  ;; wildcard

  (is (= (match 'c
                :a :fail
                1 :fail
                _ :ok)
         :ok))

  ;; is

  (is (= (match :x
                (is integer?) :fail
                (is keyword?) :ok)
         :ok))

  ;; ordered choice

  (is (= (match :x
                (is integer?) :fail
                (is keyword?) :ok
                :x :not-reached)
         :ok))

  ;; lists

  (is (= (match (list 1 2 3)
                (list 1 2) :too-short
                (list 1 2 3 4) :too-long
                (list 1 2 3) :just-right)
         :just-right))

  ;; vecs

  (is (= (match [1 2 3]
                [1 2] :too-short
                [1 2 3 4] :too-long
                [1 2 3] :just-right)
         :just-right))

  ;; maps

  (is (= (match {:a 1 :b 2}
                {:a 2} :wrong
                {:a 1} :no-b
                {:a 1 :b 2} :exact)
         :no-b ;; missing keys are allowed
         ))

  (is (= (match {:a 1 :b 2}
                {:a 1 :c _} :huh?)
         :huh? ;; just matches on (:c input) and _ matches nil, so missing keys are allowed
         ))

  ;; records

  (is (= (match (Foo. 1 2)
                (Bar. 1 2) :fail
                (Foo. :a :b) :fail
                (Foo. 1 2) :ok)))

  (is (= (match (Foo. 1 2)
                (->Foo 1 2) :ok)))

  ;; TODO something is calling clojure.walk/walk on this record literal :(
  ;;  (is (= (match (Foo. 1 2)
  ;;                #strucjure.test.Foo{:x 1 :y 2} :ok)))

  (is (= (match (Foo. 1 2)
                {:x 1 :y 2} :ok)))

  ;; metadata

  (is (= (match (with-meta [1 2 3] {:foo true})
                (with-meta _ {:foo _}) :ok)
         :ok))

  ;;names

  (is (= (match [1 2 3]
                [1 ^x _ 3] x)
         2))

  (is (= (match [1 2 3]
                ^x [1 _ 3] x)
         (list 1 2 3) ;; names capture output, not input. vectors and lists both match any Seqable and always output a Seq.
         ))

  ;; guards

  (is (= (match [1 2 3]
                (guard [1 ^z _ ^y _] (= z y)) :fail
                [1 ^z _ ^y _] [z y])
         [2 3]))

  ;; booleans

  (is (= (match [1 2 3]
                (or [] [1] [1 2] [1 2 3]) :ok)
         :ok))

;; TODO figure out how to make this work with Refers
;;   (is (thrown? java.lang.AssertionError ;; may not have different names in different branches of `or`
;;                (macroexpand-1
;;                 '(match [1 2 3]
;;                         (or [] ^x [1] [1 2] [1 2 3]) x))))

  (is (= (match {:a 1 :b 2}
                (and [[:a 1] [:b 2]] {:a 1 :b 2}) :ok)
         :ok ;; maps are sequences too
         ))

  )

(deftest repetition

  ;; rest

  (is (= (match [1 2 3]
                [1 (& ^z _)] z)
         (list 2 3)))

  (is (= (match [1 2 3]
                [1 ^z (& _)] z)
         (list 2 3) ;; regression test - naming a Rest pattern used to not work
         ))

  ;; greedy

  (is (= (match [1 2 3 4 5]
                [1 ^x (& [_ _]) ^y (& [_ _])] [x y])
         [(list 2 3) (list 4 5)]))

  ;; repetition

  (is (= (match [1 2]
                ^z (* (is integer?)) z)
         (list 1 2)))

  (is (= (match [1 2 :x :y :z]
                [^ints (&* (is integer?)) ^keys (&* (is keyword?))] [ints keys])
         [(list 1 2) (list :x :y :z)]))

  (is (= (match [1 2 1 2 1 2]
                ^z (*& [1 2]) z)
         (list 1 2 1 2 1 2)))

  (is (= (match [1 2 1 2 1 2 3]
                (*& [1 2]) :fail
                _ :ok)
         :ok))

  (is (= (match [1 2 1 2 1 2 3]
                [^y (&*& [1 2]) ^z (& _)] [y z])
         [(list 1 2 1 2 1 2) (list 3)]))

  )

(deftest named-patterns

  ;; basic

  (is (= (match [[1 2 3] [1 2 3]]
                (letp [foo [1 2 3]]
                      [foo foo] :ok))
         :ok))

  ;; recursion

  (is (= (match '(succ (succ (succ zero)))
               (letp [num (or succ zero)
                      succ (case ['succ num] (inc num))
                      zero (case 'zero 0)]
                     num))
         3))

  ;; parsing

  (is (= (match [1 2 3 :x :y :z]
                (letp [ints (* (is integer?))
                       keys (* (is keyword?))]
                      ints :fail
                      [(& ints)] :fail
                      [(& ints) (& keys)] :ok))
         :ok ;; tests that let-bound patterns are called with the right parsing context
         ))

  )

(comment
  ;; first-class patterns

  (pattern [1 ^x _ 3])

  (pattern [1 ^x _ 3])

  ;; evaluation

  (match {:a 1 :b 2}
         {:a ^a _ :b ^b _} [a b])

  =>

  (match {:a 1 :b 2}
         (pattern {:a ^a _ :b ^b _}) [a b])

  =>

  (match {:a 1 :b 2}
         {:a (->Name 'a (->Any)) :b (->Name 'b (->Any))} [a b])

  =>

  (let [pattern (eval '(->Output {:a (->Name 'a (->Any)) :b (->Name 'b (->Any))} '[a b]))]
    `(let [~view/input {:a 1 :b 2}]
       ~(view/view-top pattern)))

  =>

  (clojure.core/let [input6214 {:a 1, :b 2}] (clojure.core/let [last-failure6220 (proteus.Containers$O. nil) remaining6238 (proteus.Containers$O. nil)] (clojure.core/let [a (proteus.Containers$O. nil) b (proteus.Containers$O. nil)] (do (do (strucjure.view/check (clojure.core/map? input6214) {:a #strucjure.pattern.Name{:name a, :pattern #strucjure.pattern.Any{}}, :b #strucjure.pattern.Name{:name b, :pattern #strucjure.pattern.Any{}}}) (do {:a #strucjure.pattern.Name{:name a, :pattern #strucjure.pattern.Any{}}, :b #strucjure.pattern.Name{:name b, :pattern #strucjure.pattern.Any{}}} :a (strucjure.view/let-input (clojure.core/get input6214 :a) (clojure.core/let [output__6350__auto__ input6214] (.set a output__6350__auto__) output__6350__auto__)) :b (strucjure.view/let-input (clojure.core/get input6214 :b) (clojure.core/let [output__6350__auto__ input6214] (.set b output__6350__auto__) output__6350__auto__)))) (strucjure.view/trap-failure (clojure.core/let [a (.x a) b (.x b)] [a b]))))))

  ;; open language

  (match {:a 1 :b 2}
         {:a ^a _ :b ^b _} [a b])

  (defn keys* [& symbols]
    (for-map [symbol symbols]
             (keyword (name symbol))
             (->Name symbol (->Any))))

  (defmacro keys [& symbols]
    `(keys* ~@(for [symbol symbols] `'~symbol)))

  (match {:a 1 :b 2}
         (keys a b) [a b])

  ;; first-class scopes

  (letp [a [0 b]
         b [1 a]]
        a)

  (letp [a [0 b]
         b [1 a]]
        a)

  (def cyclic
    (pattern (letp [a [0 b]
                    b [1 a]]
                   a)))

  (def finite
    (update-in cyclic [:refers 'b] #(->Or [% nil])))

  (match [0 [1 [0 nil]]]
         finite :ok)

  )

(comment
  ;; errors

  (match [1 2 3]
         [1 2] :nope
         [1 2 3 4] :nope
         [1 :x] :oh-noes)

  (match '(succ (succ (succ goose)))
         (letp [num (or succ zero)
                succ (case ['succ num] (inc num))
                zero (case 'zero 0)]
               num))

  ;; tracing
  (match-with trace-let '(succ (succ (succ zero)))
         (letp [num (or succ zero)
                succ (case ['succ num] (inc num))
                zero (case 'zero 0)]
               num))

  (try
    (match-with trace-let '(succ (succ (succ goose)))
                  (letp [num (or succ zero)
                         succ (case ['succ num] (inc num))
                         zero (case 'zero 0)]
                        num))
    (catch Exception _))
  )