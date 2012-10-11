(ns strucjure.bootstrap)
(do
 (def
  optional
  '(clojure.core/letfn
    [(thunk__11472
      [true-cont__11468 false-cont__11469 input__11467]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__11467)
        (clojure.core/nil? input__11467))
       (clojure.core/let
        [left__11471 (clojure.core/seq input__11467)]
        (clojure.core/let
         [rest left__11471]
         (.invoke true-cont__11468 nil rest)))
       (.invoke false-cont__11469)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__11467 true-cont__11468 false-cont__11469]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11467)
         (clojure.core/nil? input__11467))
        (clojure.core/let
         [left__11473 (clojure.core/seq input__11467)]
         (if
          (clojure.core/not= nil left__11473)
          (clojure.core/let
           [left__11474 (clojure.core/first left__11473)]
           ((.view-fn elem)
            left__11474
            (clojure.core/fn
             [output__11475 rest__11476]
             (clojure.core/let
              [x output__11475]
              (if
               (clojure.core/= nil rest__11476)
               (clojure.core/let
                [left__11477 (clojure.core/next left__11473)]
                (clojure.core/let
                 [rest left__11477]
                 (.invoke true-cont__11468 x rest)))
               (thunk__11472
                true-cont__11468
                false-cont__11469
                input__11467))))
            (clojure.core/fn
             []
             (thunk__11472
              true-cont__11468
              false-cont__11469
              input__11467))))
          (thunk__11472
           true-cont__11468
           false-cont__11469
           input__11467)))
        (thunk__11472
         true-cont__11468
         false-cont__11469
         input__11467)))))))
 (def
  zero-or-more
  '(clojure.core/letfn
    [(thunk__11483
      [false-cont__11480 input__11478 true-cont__11479]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__11478)
        (clojure.core/nil? input__11478))
       (clojure.core/let
        [left__11482 (clojure.core/seq input__11478)]
        (clojure.core/let
         [rest left__11482]
         (.invoke true-cont__11479 nil rest)))
       (.invoke false-cont__11480)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__11478 true-cont__11479 false-cont__11480]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11478)
         (clojure.core/nil? input__11478))
        (clojure.core/let
         [left__11484 (clojure.core/seq input__11478)]
         (if
          (clojure.core/not= nil left__11484)
          (clojure.core/let
           [left__11485 (clojure.core/first left__11484)]
           ((.view-fn elem)
            left__11485
            (clojure.core/fn
             [output__11486 rest__11487]
             (clojure.core/let
              [x output__11486]
              (if
               (clojure.core/= nil rest__11487)
               (clojure.core/let
                [left__11488 (clojure.core/next left__11484)]
                ((.view-fn (zero-or-more elem))
                 left__11488
                 (clojure.core/fn
                  [output__11489 rest__11490]
                  (clojure.core/let
                   [xs output__11489]
                   (clojure.core/let
                    [rest rest__11490]
                    (.invoke true-cont__11479 (cons x xs) rest))))
                 (clojure.core/fn
                  []
                  (thunk__11483
                   false-cont__11480
                   input__11478
                   true-cont__11479))))
               (thunk__11483
                false-cont__11480
                input__11478
                true-cont__11479))))
            (clojure.core/fn
             []
             (thunk__11483
              false-cont__11480
              input__11478
              true-cont__11479))))
          (thunk__11483
           false-cont__11480
           input__11478
           true-cont__11479)))
        (thunk__11483
         false-cont__11480
         input__11478
         true-cont__11479)))))))
 (def
  one-or-more
  '(clojure.core/letfn
    []
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__11491 true-cont__11492 false-cont__11493]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11491)
         (clojure.core/nil? input__11491))
        (clojure.core/let
         [left__11495 (clojure.core/seq input__11491)]
         (if
          (clojure.core/not= nil left__11495)
          (clojure.core/let
           [left__11496 (clojure.core/first left__11495)]
           ((.view-fn elem)
            left__11496
            (clojure.core/fn
             [output__11497 rest__11498]
             (clojure.core/let
              [x output__11497]
              (if
               (clojure.core/= nil rest__11498)
               (clojure.core/let
                [left__11499 (clojure.core/next left__11495)]
                ((.view-fn (zero-or-more elem))
                 left__11499
                 (clojure.core/fn
                  [output__11500 rest__11501]
                  (clojure.core/let
                   [xs output__11500]
                   (clojure.core/let
                    [rest rest__11501]
                    (.invoke true-cont__11492 (cons x xs) rest))))
                 (clojure.core/fn [] (.invoke false-cont__11493))))
               (.invoke false-cont__11493))))
            (clojure.core/fn [] (.invoke false-cont__11493))))
          (.invoke false-cont__11493)))
        (.invoke false-cont__11493)))))))
 (def
  key&pattern
  '(clojure.core/letfn
    []
    (strucjure/->View
     (clojure.core/fn
      [input__11502 true-cont__11503 false-cont__11504]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__11502)
        (clojure.core/nil? input__11502))
       (clojure.core/let
        [left__11506 (clojure.core/seq input__11502)]
        (if
         (clojure.core/not= nil left__11506)
         (clojure.core/let
          [left__11507 (clojure.core/first left__11506)]
          (clojure.core/let
           [key left__11507]
           (clojure.core/let
            [left__11508 (clojure.core/next left__11506)]
            (if
             (clojure.core/not= nil left__11508)
             (clojure.core/let
              [left__11509 (clojure.core/first left__11508)]
              ((.view-fn pattern)
               left__11509
               (clojure.core/fn
                [output__11510 rest__11511]
                (clojure.core/let
                 [pattern output__11510]
                 (if
                  (clojure.core/= nil rest__11511)
                  (clojure.core/let
                   [left__11512 (clojure.core/next left__11508)]
                   (if
                    (clojure.core/= nil left__11512)
                    (.invoke true-cont__11503 [key pattern] nil)
                    (.invoke false-cont__11504)))
                  (.invoke false-cont__11504))))
               (clojure.core/fn [] (.invoke false-cont__11504))))
             (.invoke false-cont__11504)))))
         (.invoke false-cont__11504)))
       (.invoke false-cont__11504))))))
 (def
  pattern
  '(clojure.core/letfn
    [(thunk__11524
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11517 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11517)
          (clojure.core/let
           [left__11518 (clojure.core/first left__11517)]
           (clojure.core/let
            [view left__11518]
            (clojure.core/let
             [left__11519 (clojure.core/next left__11517)]
             (if
              (clojure.core/not= nil left__11519)
              (clojure.core/let
               [left__11520 (clojure.core/first left__11519)]
               ((.view-fn pattern)
                left__11520
                (clojure.core/fn
                 [output__11521 rest__11522]
                 (clojure.core/let
                  [pattern output__11521]
                  (if
                   (clojure.core/= nil rest__11522)
                   (clojure.core/let
                    [left__11523 (clojure.core/next left__11519)]
                    (if
                     (clojure.core/= nil left__11523)
                     (.invoke
                      true-cont__11514
                      (import-ast view pattern)
                      nil)
                     (.invoke false-cont__11515)))
                   (.invoke false-cont__11515))))
                (clojure.core/fn [] (.invoke false-cont__11515))))
              (.invoke false-cont__11515)))))
          (.invoke false-cont__11515)))
        (.invoke false-cont__11515))
       (.invoke false-cont__11515)))
     (thunk__11525
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (symbol? input__11513)
       (clojure.core/let
        [variable input__11513]
        (.invoke true-cont__11514 (literal-ast variable) nil))
       (thunk__11524 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11531
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11526 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11526)
          (clojure.core/let
           [left__11527 (clojure.core/first left__11526)]
           (if
            (clojure.core/= 'or left__11527)
            (clojure.core/let
             [left__11528 (clojure.core/next left__11526)]
             ((.view-fn (one-or-more pattern))
              left__11528
              (clojure.core/fn
               [output__11529 rest__11530]
               (clojure.core/let
                [patterns output__11529]
                (if
                 (clojure.core/= nil rest__11530)
                 (.invoke true-cont__11514 (apply or-ast patterns) nil)
                 (thunk__11525
                  false-cont__11515
                  input__11513
                  true-cont__11514))))
              (clojure.core/fn
               []
               (thunk__11525
                false-cont__11515
                input__11513
                true-cont__11514))))
            (thunk__11525
             false-cont__11515
             input__11513
             true-cont__11514)))
          (thunk__11525
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11525 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11525 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11537
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11532 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11532)
          (clojure.core/let
           [left__11533 (clojure.core/first left__11532)]
           (if
            (clojure.core/= 'seq left__11533)
            (clojure.core/let
             [left__11534 (clojure.core/next left__11532)]
             ((.view-fn (one-or-more pattern))
              left__11534
              (clojure.core/fn
               [output__11535 rest__11536]
               (clojure.core/let
                [patterns output__11535]
                (if
                 (clojure.core/= nil rest__11536)
                 (.invoke
                  true-cont__11514
                  (apply seq-ast patterns)
                  nil)
                 (thunk__11531
                  false-cont__11515
                  input__11513
                  true-cont__11514))))
              (clojure.core/fn
               []
               (thunk__11531
                false-cont__11515
                input__11513
                true-cont__11514))))
            (thunk__11531
             false-cont__11515
             input__11513
             true-cont__11514)))
          (thunk__11531
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11531 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11531 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11543
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11538 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11538)
          (clojure.core/let
           [left__11539 (clojure.core/first left__11538)]
           (if
            (clojure.core/= 'and left__11539)
            (clojure.core/let
             [left__11540 (clojure.core/next left__11538)]
             ((.view-fn (one-or-more pattern))
              left__11540
              (clojure.core/fn
               [output__11541 rest__11542]
               (clojure.core/let
                [patterns output__11541]
                (if
                 (clojure.core/= nil rest__11542)
                 (.invoke
                  true-cont__11514
                  (apply and-ast patterns)
                  nil)
                 (thunk__11537
                  false-cont__11515
                  input__11513
                  true-cont__11514))))
              (clojure.core/fn
               []
               (thunk__11537
                false-cont__11515
                input__11513
                true-cont__11514))))
            (thunk__11537
             false-cont__11515
             input__11513
             true-cont__11514)))
          (thunk__11537
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11537 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11537 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11549
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11544 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11544)
          (clojure.core/let
           [left__11545 (clojure.core/first left__11544)]
           (if
            (clojure.core/= 'leave left__11545)
            (clojure.core/let
             [left__11546 (clojure.core/next left__11544)]
             (if
              (clojure.core/not= nil left__11546)
              (clojure.core/let
               [left__11547 (clojure.core/first left__11546)]
               (clojure.core/let
                [form left__11547]
                (clojure.core/let
                 [left__11548 (clojure.core/next left__11546)]
                 (if
                  (clojure.core/= nil left__11548)
                  (.invoke true-cont__11514 (->Leave form) nil)
                  (thunk__11543
                   false-cont__11515
                   input__11513
                   true-cont__11514)))))
              (thunk__11543
               false-cont__11515
               input__11513
               true-cont__11514)))
            (thunk__11543
             false-cont__11515
             input__11513
             true-cont__11514)))
          (thunk__11543
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11543 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11543 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11555
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11550 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11550)
          (clojure.core/let
           [left__11551 (clojure.core/first left__11550)]
           (if
            (clojure.core/= 'guard left__11551)
            (clojure.core/let
             [left__11552 (clojure.core/next left__11550)]
             (if
              (clojure.core/not= nil left__11552)
              (clojure.core/let
               [left__11553 (clojure.core/first left__11552)]
               (clojure.core/let
                [form left__11553]
                (clojure.core/let
                 [left__11554 (clojure.core/next left__11552)]
                 (if
                  (clojure.core/= nil left__11554)
                  (.invoke true-cont__11514 (->Guard form) nil)
                  (thunk__11549
                   false-cont__11515
                   input__11513
                   true-cont__11514)))))
              (thunk__11549
               false-cont__11515
               input__11513
               true-cont__11514)))
            (thunk__11549
             false-cont__11515
             input__11513
             true-cont__11514)))
          (thunk__11549
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11549 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11549 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11561
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11556 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11556)
          (clojure.core/let
           [left__11557 (clojure.core/first left__11556)]
           (if
            (clojure.core/= 'quote left__11557)
            (clojure.core/let
             [left__11558 (clojure.core/next left__11556)]
             (if
              (clojure.core/not= nil left__11558)
              (clojure.core/let
               [left__11559 (clojure.core/first left__11558)]
               (clojure.core/let
                [quoted left__11559]
                (clojure.core/let
                 [left__11560 (clojure.core/next left__11558)]
                 (if
                  (clojure.core/= nil left__11560)
                  (.invoke
                   true-cont__11514
                   (literal-ast
                    (clojure.core/seq
                     (clojure.core/concat
                      (clojure.core/list 'quote)
                      (clojure.core/list quoted))))
                   nil)
                  (thunk__11555
                   false-cont__11515
                   input__11513
                   true-cont__11514)))))
              (thunk__11555
               false-cont__11515
               input__11513
               true-cont__11514)))
            (thunk__11555
             false-cont__11515
             input__11513
             true-cont__11514)))
          (thunk__11555
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11555 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11555 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11567
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11562 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11562)
          (clojure.core/let
           [left__11563 (clojure.core/first left__11562)]
           (if
            (clojure.core/= 'prefix left__11563)
            (clojure.core/let
             [left__11564 (clojure.core/next left__11562)]
             ((.view-fn (zero-or-more seq-pattern))
              left__11564
              (clojure.core/fn
               [output__11565 rest__11566]
               (clojure.core/let
                [seq-patterns output__11565]
                (if
                 (clojure.core/= nil rest__11566)
                 (.invoke
                  true-cont__11514
                  (apply prefix-ast seq-patterns)
                  nil)
                 (thunk__11561
                  false-cont__11515
                  input__11513
                  true-cont__11514))))
              (clojure.core/fn
               []
               (thunk__11561
                false-cont__11515
                input__11513
                true-cont__11514))))
            (thunk__11561
             false-cont__11515
             input__11513
             true-cont__11514)))
          (thunk__11561
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11561 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11561 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11571
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (vector? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11568 (clojure.core/seq input__11513)]
         ((.view-fn (zero-or-more seq-pattern))
          left__11568
          (clojure.core/fn
           [output__11569 rest__11570]
           (clojure.core/let
            [seq-patterns output__11569]
            (if
             (clojure.core/= nil rest__11570)
             (.invoke
              true-cont__11514
              (apply seqable-ast seq-patterns)
              nil)
             (thunk__11567
              false-cont__11515
              input__11513
              true-cont__11514))))
          (clojure.core/fn
           []
           (thunk__11567
            false-cont__11515
            input__11513
            true-cont__11514))))
        (thunk__11567 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11567 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11581
      [false-cont__11515
       true-case-input__11574
       input__11513
       true-cont__11514
       left__11572]
      (if
       (clojure.core/= nil true-case-input__11574)
       (clojure.core/let
        [left__11575 (clojure.core/next left__11572)]
        (if
         (clojure.core/not= nil left__11575)
         (clojure.core/let
          [left__11576 (clojure.core/first left__11575)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__11576)
            (clojure.core/nil? left__11576))
           (clojure.core/let
            [left__11577 (clojure.core/seq left__11576)]
            (if
             (clojure.core/not= nil left__11577)
             (clojure.core/let
              [left__11578 (clojure.core/first left__11577)]
              (clojure.core/let
               [arg left__11578]
               (clojure.core/let
                [left__11579 (clojure.core/next left__11577)]
                (if
                 (clojure.core/= nil left__11579)
                 (clojure.core/let
                  [left__11580 (clojure.core/next left__11575)]
                  (clojure.core/let
                   [body left__11580]
                   (.invoke
                    true-cont__11514
                    (predicate-ast
                     (clojure.core/seq
                      (clojure.core/concat
                       (clojure.core/list 'do)
                       (clojure.walk/prewalk-replace
                        {arg input-sym}
                        body))))
                    nil)))
                 (thunk__11571
                  false-cont__11515
                  input__11513
                  true-cont__11514)))))
             (thunk__11571
              false-cont__11515
              input__11513
              true-cont__11514)))
           (thunk__11571
            false-cont__11515
            input__11513
            true-cont__11514)))
         (thunk__11571
          false-cont__11515
          input__11513
          true-cont__11514)))
       (thunk__11571 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11582
      [false-cont__11515
       input__11513
       true-cont__11514
       left__11573
       left__11572]
      (if
       (clojure.core/= 'fn* left__11573)
       (thunk__11581
        false-cont__11515
        nil
        input__11513
        true-cont__11514
        left__11572)
       (thunk__11571 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11583
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11572 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11572)
          (clojure.core/let
           [left__11573 (clojure.core/first left__11572)]
           (if
            (clojure.core/= 'fn left__11573)
            (thunk__11581
             false-cont__11515
             nil
             input__11513
             true-cont__11514
             left__11572)
            (thunk__11582
             false-cont__11515
             input__11513
             true-cont__11514
             left__11573
             left__11572)))
          (thunk__11571
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11571 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11571 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11584
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (predicate? input__11513)
       (clojure.core/let
        [predicate input__11513]
        (.invoke
         true-cont__11514
         (predicate-ast
          (clojure.core/seq
           (clojure.core/concat
            (clojure.core/list predicate)
            (clojure.core/list input-sym))))
         nil))
       (thunk__11583 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11585
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (clojure.core/instance? java.util.regex.Pattern input__11513)
       (clojure.core/let
        [regex input__11513]
        (.invoke true-cont__11514 (regex-ast regex) nil))
       (thunk__11584 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11591
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (seq? input__11513)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11513)
         (clojure.core/nil? input__11513))
        (clojure.core/let
         [left__11586 (clojure.core/seq input__11513)]
         (if
          (clojure.core/not= nil left__11586)
          (clojure.core/let
           [left__11587 (clojure.core/first left__11586)]
           (if
            (constructor? left__11587)
            (clojure.core/let
             [constructor left__11587]
             (clojure.core/let
              [left__11588 (clojure.core/next left__11586)]
              ((.view-fn (zero-or-more pattern))
               left__11588
               (clojure.core/fn
                [output__11589 rest__11590]
                (clojure.core/let
                 [arg-patterns output__11589]
                 (if
                  (clojure.core/= nil rest__11590)
                  (.invoke
                   true-cont__11514
                   (constructor-ast
                    (constructor-name constructor)
                    arg-patterns)
                   nil)
                  (thunk__11585
                   false-cont__11515
                   input__11513
                   true-cont__11514))))
               (clojure.core/fn
                []
                (thunk__11585
                 false-cont__11515
                 input__11513
                 true-cont__11514)))))
            (thunk__11585
             false-cont__11515
             input__11513
             true-cont__11514)))
          (thunk__11585
           false-cont__11515
           input__11513
           true-cont__11514)))
        (thunk__11585 false-cont__11515 input__11513 true-cont__11514))
       (thunk__11585 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11596
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__11513)
        (clojure.core/nil? input__11513))
       (clojure.core/let
        [left__11593 (clojure.core/seq input__11513)]
        ((.view-fn (zero-or-more key&pattern))
         left__11593
         (clojure.core/fn
          [output__11594 rest__11595]
          (clojure.core/let
           [keys&patterns output__11594]
           (if
            (clojure.core/= nil rest__11595)
            (.invoke true-cont__11514 (map-ast keys&patterns) nil)
            (thunk__11591
             false-cont__11515
             input__11513
             true-cont__11514))))
         (clojure.core/fn
          []
          (thunk__11591
           false-cont__11515
           input__11513
           true-cont__11514))))
       (thunk__11591 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11597
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentHashMap
        input__11513)
       (thunk__11596 false-cont__11515 input__11513 true-cont__11514)
       (thunk__11591 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11598
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentArrayMap
        input__11513)
       (thunk__11596 false-cont__11515 input__11513 true-cont__11514)
       (thunk__11597 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11599
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (class-name? input__11513)
       (clojure.core/let
        [class-name input__11513]
        (.invoke true-cont__11514 (class-ast class-name) nil))
       (thunk__11598 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11600
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (primitive? input__11513)
       (clojure.core/let
        [primitive input__11513]
        (.invoke true-cont__11514 (literal-ast primitive) nil))
       (thunk__11599 false-cont__11515 input__11513 true-cont__11514)))
     (thunk__11601
      [false-cont__11515 input__11513 true-cont__11514]
      (if
       (binding? input__11513)
       (clojure.core/let
        [binding input__11513]
        (.invoke true-cont__11514 (->Bind (binding-name binding)) nil))
       (thunk__11600
        false-cont__11515
        input__11513
        true-cont__11514)))]
    (strucjure/->View
     (clojure.core/fn
      [input__11513 true-cont__11514 false-cont__11515]
      (if
       (clojure.core/= '_ input__11513)
       (.invoke true-cont__11514 (->Leave nil) nil)
       (thunk__11601
        false-cont__11515
        input__11513
        true-cont__11514))))))
 (def
  seq-pattern
  '(clojure.core/letfn
    [(thunk__11608
      [input__11602 true-cont__11603 false-cont__11604]
      ((.view-fn pattern)
       input__11602
       (clojure.core/fn
        [output__11606 rest__11607]
        (clojure.core/let
         [pattern output__11606]
         (.invoke true-cont__11603 (head-ast pattern) rest__11607)))
       (clojure.core/fn [] (.invoke false-cont__11604))))
     (thunk__11614
      [input__11602 true-cont__11603 false-cont__11604]
      (if
       (seq? input__11602)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11602)
         (clojure.core/nil? input__11602))
        (clojure.core/let
         [left__11609 (clojure.core/seq input__11602)]
         (if
          (clojure.core/not= nil left__11609)
          (clojure.core/let
           [left__11610 (clojure.core/first left__11609)]
           (if
            (clojure.core/= 'guard left__11610)
            (clojure.core/let
             [left__11611 (clojure.core/next left__11609)]
             (if
              (clojure.core/not= nil left__11611)
              (clojure.core/let
               [left__11612 (clojure.core/first left__11611)]
               (clojure.core/let
                [form left__11612]
                (clojure.core/let
                 [left__11613 (clojure.core/next left__11611)]
                 (if
                  (clojure.core/= nil left__11613)
                  (.invoke true-cont__11603 (->Guard form) nil)
                  (thunk__11608
                   input__11602
                   true-cont__11603
                   false-cont__11604)))))
              (thunk__11608
               input__11602
               true-cont__11603
               false-cont__11604)))
            (thunk__11608
             input__11602
             true-cont__11603
             false-cont__11604)))
          (thunk__11608
           input__11602
           true-cont__11603
           false-cont__11604)))
        (thunk__11608 input__11602 true-cont__11603 false-cont__11604))
       (thunk__11608
        input__11602
        true-cont__11603
        false-cont__11604)))]
    (strucjure/->View
     (clojure.core/fn
      [input__11602 true-cont__11603 false-cont__11604]
      (if
       (seq? input__11602)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__11602)
         (clojure.core/nil? input__11602))
        (clojure.core/let
         [left__11615 (clojure.core/seq input__11602)]
         (if
          (clojure.core/not= nil left__11615)
          (clojure.core/let
           [left__11616 (clojure.core/first left__11615)]
           (if
            (clojure.core/= '& left__11616)
            (clojure.core/let
             [left__11617 (clojure.core/next left__11615)]
             (if
              (clojure.core/not= nil left__11617)
              (clojure.core/let
               [left__11618 (clojure.core/first left__11617)]
               ((.view-fn pattern)
                left__11618
                (clojure.core/fn
                 [output__11619 rest__11620]
                 (clojure.core/let
                  [pattern output__11619]
                  (if
                   (clojure.core/= nil rest__11620)
                   (clojure.core/let
                    [left__11621 (clojure.core/next left__11617)]
                    (if
                     (clojure.core/= nil left__11621)
                     (.invoke true-cont__11603 pattern nil)
                     (thunk__11614
                      input__11602
                      true-cont__11603
                      false-cont__11604)))
                   (thunk__11614
                    input__11602
                    true-cont__11603
                    false-cont__11604))))
                (clojure.core/fn
                 []
                 (thunk__11614
                  input__11602
                  true-cont__11603
                  false-cont__11604))))
              (thunk__11614
               input__11602
               true-cont__11603
               false-cont__11604)))
            (thunk__11614
             input__11602
             true-cont__11603
             false-cont__11604)))
          (thunk__11614
           input__11602
           true-cont__11603
           false-cont__11604)))
        (thunk__11614 input__11602 true-cont__11603 false-cont__11604))
       (thunk__11614
        input__11602
        true-cont__11603
        false-cont__11604)))))))
