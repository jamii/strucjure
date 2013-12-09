(ns strucjure.examples
  (:refer-clojure :exclude [with-meta * + or and name case])
  (require [plumbing.core :refer [for-map aconcat map-vals]]
           [strucjure.pattern :refer :all]
           [strucjure.view :as view]
           [strucjure.sugar :refer :all]))

;; READABILITY

;; A red-black tree is balanced when:
;; * every path from root to leaf has the same number of black nodes
;; * no red node has red children

;; java version - http://cs.lmu.edu/~ray/notes/redblacktrees/

(defrecord Red [value left right])
(defrecord Black [value left right])

(defn balance [tree]
  (match tree
         (or
          (Black. ^z _ (Red. ^y _ (Red. ^x _ ^a _ ^b _) ^c _) ^d _)
          (Black. ^z _ (Red. ^x _ ^a _ (Red. ^y _ ^b _ ^c _)) ^d _)
          (Black. ^x _ ^a _ (Red. ^z _ (Red. ^y _ ^b _ ^c _) ^d _))
          (Black. ^x _ ^a _ (Red. ^y _ ^b _ (Red. ^z _ ^c _ ^d _))))
         (Red. y (Black. x a b) (Black. z c d))

         ^other _
         other))

;; EASY EXTENSION

(match {:a 1 :b 2}
       {:a ^a _ :b ^b _} [a b])

(defn keys* [& symbols]
  (for-map [symbol symbols]
           (keyword (str symbol))
           (->Name symbol (->Any))))

(defmacro keys [& symbols]
  `(keys* ~@(for [symbol symbols] `'~symbol)))

(match {:a 1 :b 2}
       (keys a b) [a b])

;; FAIL EARLY
;; compare...

(defn f [{:keys [x y] :as z}]
  [x y z])

(f {:x 1 :y 2})

(f nil)

(f (list 1 2 3 4))

(defn g [input]
  (match input
         ^z (keys x y) [x y z]))

(g {:x 1 :y 2})

(g nil)

(g (list 1 2 3 4))

;; FIRST-CLASS SCOPES

(def expr
  (letp [num (or succ zero)
         succ (case ['succ num] (inc num))
         zero (case 'zero 0)
         expr (or num add)
         add (case ['add ^a expr ^b expr] (clojure.core/+ a b))]
        expr))

(match '(add (succ zero) (succ zero))
       ^result expr result)

(def expr-with-sub
  (-> expr
      (update-in [:refers 'expr] #(->Or [% (->Refer 'sub)]))
      (assoc-in [:refers 'sub] (case ['sub ^a expr ^b expr] (clojure.core/- a b)))))

(match '(sub (add (succ zero) (succ zero)) (succ zero))
       ^result expr-with-sub result)

;; USEFUL ERRORS
;; The Failure exception reports every point where the match backtracked

(match [1 2 3]
       [1 2] :nope
       [1 2 3 4] :nope
       [1 :x] :oh-noes)

(match '(add (sub (succ zero) (succ zero)) (succ zero))
       expr expr)

;; TRACING

(with-out-str
  (match-with trace-let '(add (add (succ zero) (succ zero)) (succ zero))
              expr expr))

(with-out-str
  (try
    (match-with trace-let '(add (sub (succ zero) (succ zero)) (succ zero))
                expr expr)
    (catch Exception _)))

;; PERFORMANCE
;; needs work...

(= {:a 1 :b 2}
   {:a 1 :b 2})
;; 160 ns

(match {:a 1 :b 2}
       {:a 1 :b 2} :ok)
;; 156 ns

(let [{:keys [a b]} {:a 1 :b 2}]
  (and (= a 1) (= b 2)))
;; 110 ns

(let [{:keys [a b]} {:a 1 :b 2}]
  [a b])
;; 117 ns

(match {:a 1 :b 2}
       (keys a b) [a b])
;; 657 ns - needs real mutable vars :(