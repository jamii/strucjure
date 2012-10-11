(ns strucjure.bootstrap)
(do
 (def
  optional
  '(clojure.core/letfn
    [(thunk__26791
      [true-cont__26787 input__26786 false-cont__26788]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__26786)
        (clojure.core/nil? input__26786))
       (clojure.core/let
        [left__26790 (clojure.core/seq input__26786)]
        (.invoke true-cont__26787 nil left__26790))
       (.invoke false-cont__26788)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__26786 true-cont__26787 false-cont__26788]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26786)
         (clojure.core/nil? input__26786))
        (clojure.core/let
         [left__26792 (clojure.core/seq input__26786)]
         (if
          (clojure.core/not= nil left__26792)
          (clojure.core/let
           [left__26793 (clojure.core/first left__26792)]
           ((.view-fn elem)
            left__26793
            (clojure.core/fn
             [output__26794 rest__26795]
             (clojure.core/let
              [x output__26794]
              (if
               (clojure.core/= nil rest__26795)
               (clojure.core/let
                [left__26796 (clojure.core/next left__26792)]
                (.invoke true-cont__26787 x left__26796))
               (thunk__26791
                true-cont__26787
                input__26786
                false-cont__26788))))
            (clojure.core/fn
             []
             (thunk__26791
              true-cont__26787
              input__26786
              false-cont__26788))))
          (thunk__26791
           true-cont__26787
           input__26786
           false-cont__26788)))
        (thunk__26791
         true-cont__26787
         input__26786
         false-cont__26788)))))))
 (def
  zero-or-more
  '(clojure.core/letfn
    [(thunk__26802
      [true-cont__26798 input__26797 false-cont__26799]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__26797)
        (clojure.core/nil? input__26797))
       (clojure.core/let
        [left__26801 (clojure.core/seq input__26797)]
        (.invoke true-cont__26798 nil left__26801))
       (.invoke false-cont__26799)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__26797 true-cont__26798 false-cont__26799]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26797)
         (clojure.core/nil? input__26797))
        (clojure.core/let
         [left__26803 (clojure.core/seq input__26797)]
         (if
          (clojure.core/not= nil left__26803)
          (clojure.core/let
           [left__26804 (clojure.core/first left__26803)]
           ((.view-fn elem)
            left__26804
            (clojure.core/fn
             [output__26805 rest__26806]
             (clojure.core/let
              [x output__26805]
              (if
               (clojure.core/= nil rest__26806)
               (clojure.core/let
                [left__26807 (clojure.core/next left__26803)]
                ((.view-fn (zero-or-more elem))
                 left__26807
                 (clojure.core/fn
                  [output__26808 rest__26809]
                  (clojure.core/let
                   [xs output__26808]
                   (.invoke true-cont__26798 (cons x xs) rest__26809)))
                 (clojure.core/fn
                  []
                  (thunk__26802
                   true-cont__26798
                   input__26797
                   false-cont__26799))))
               (thunk__26802
                true-cont__26798
                input__26797
                false-cont__26799))))
            (clojure.core/fn
             []
             (thunk__26802
              true-cont__26798
              input__26797
              false-cont__26799))))
          (thunk__26802
           true-cont__26798
           input__26797
           false-cont__26799)))
        (thunk__26802
         true-cont__26798
         input__26797
         false-cont__26799)))))))
 (def
  one-or-more
  '(clojure.core/letfn
    []
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__26810 true-cont__26811 false-cont__26812]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26810)
         (clojure.core/nil? input__26810))
        (clojure.core/let
         [left__26814 (clojure.core/seq input__26810)]
         (if
          (clojure.core/not= nil left__26814)
          (clojure.core/let
           [left__26815 (clojure.core/first left__26814)]
           ((.view-fn elem)
            left__26815
            (clojure.core/fn
             [output__26816 rest__26817]
             (clojure.core/let
              [x output__26816]
              (if
               (clojure.core/= nil rest__26817)
               (clojure.core/let
                [left__26818 (clojure.core/next left__26814)]
                ((.view-fn (zero-or-more elem))
                 left__26818
                 (clojure.core/fn
                  [output__26819 rest__26820]
                  (clojure.core/let
                   [xs output__26819]
                   (.invoke true-cont__26811 (cons x xs) rest__26820)))
                 (clojure.core/fn [] (.invoke false-cont__26812))))
               (.invoke false-cont__26812))))
            (clojure.core/fn [] (.invoke false-cont__26812))))
          (.invoke false-cont__26812)))
        (.invoke false-cont__26812)))))))
 (def
  key&pattern
  '(clojure.core/letfn
    []
    (strucjure/->View
     (clojure.core/fn
      [input__26821 true-cont__26822 false-cont__26823]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__26821)
        (clojure.core/nil? input__26821))
       (clojure.core/let
        [left__26825 (clojure.core/seq input__26821)]
        (if
         (clojure.core/not= nil left__26825)
         (clojure.core/let
          [left__26826 (clojure.core/first left__26825)]
          (clojure.core/let
           [key left__26826]
           (clojure.core/let
            [left__26827 (clojure.core/next left__26825)]
            (if
             (clojure.core/not= nil left__26827)
             (clojure.core/let
              [left__26828 (clojure.core/first left__26827)]
              ((.view-fn pattern)
               left__26828
               (clojure.core/fn
                [output__26829 rest__26830]
                (clojure.core/let
                 [pattern output__26829]
                 (if
                  (clojure.core/= nil rest__26830)
                  (clojure.core/let
                   [left__26831 (clojure.core/next left__26827)]
                   (if
                    (clojure.core/= nil left__26831)
                    (.invoke true-cont__26822 [key pattern] nil)
                    (.invoke false-cont__26823)))
                  (.invoke false-cont__26823))))
               (clojure.core/fn [] (.invoke false-cont__26823))))
             (.invoke false-cont__26823)))))
         (.invoke false-cont__26823)))
       (.invoke false-cont__26823))))))
 (def
  pattern
  '(clojure.core/letfn
    [(thunk__26843
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26836 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26836)
          (clojure.core/let
           [left__26837 (clojure.core/first left__26836)]
           (clojure.core/let
            [view left__26837]
            (clojure.core/let
             [left__26838 (clojure.core/next left__26836)]
             (if
              (clojure.core/not= nil left__26838)
              (clojure.core/let
               [left__26839 (clojure.core/first left__26838)]
               ((.view-fn pattern)
                left__26839
                (clojure.core/fn
                 [output__26840 rest__26841]
                 (clojure.core/let
                  [pattern output__26840]
                  (if
                   (clojure.core/= nil rest__26841)
                   (clojure.core/let
                    [left__26842 (clojure.core/next left__26838)]
                    (if
                     (clojure.core/= nil left__26842)
                     (.invoke
                      true-cont__26833
                      (import-ast view pattern)
                      nil)
                     (.invoke false-cont__26834)))
                   (.invoke false-cont__26834))))
                (clojure.core/fn [] (.invoke false-cont__26834))))
              (.invoke false-cont__26834)))))
          (.invoke false-cont__26834)))
        (.invoke false-cont__26834))
       (.invoke false-cont__26834)))
     (thunk__26844
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (symbol? input__26832)
       (clojure.core/let
        [variable input__26832]
        (.invoke true-cont__26833 (literal-ast variable) nil))
       (thunk__26843 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26852
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26845 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26845)
          (clojure.core/let
           [left__26846 (clojure.core/first left__26845)]
           (if
            (clojure.core/= 'not left__26846)
            (clojure.core/let
             [left__26847 (clojure.core/next left__26845)]
             (if
              (clojure.core/not= nil left__26847)
              (clojure.core/let
               [left__26848 (clojure.core/first left__26847)]
               ((.view-fn pattern)
                left__26848
                (clojure.core/fn
                 [output__26849 rest__26850]
                 (clojure.core/let
                  [pattern output__26849]
                  (if
                   (clojure.core/= nil rest__26850)
                   (clojure.core/let
                    [left__26851 (clojure.core/next left__26847)]
                    (if
                     (clojure.core/= nil left__26851)
                     (.invoke true-cont__26833 (->Not pattern) nil)
                     (thunk__26844
                      true-cont__26833
                      false-cont__26834
                      input__26832)))
                   (thunk__26844
                    true-cont__26833
                    false-cont__26834
                    input__26832))))
                (clojure.core/fn
                 []
                 (thunk__26844
                  true-cont__26833
                  false-cont__26834
                  input__26832))))
              (thunk__26844
               true-cont__26833
               false-cont__26834
               input__26832)))
            (thunk__26844
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26844
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26844 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26844 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26858
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26853 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26853)
          (clojure.core/let
           [left__26854 (clojure.core/first left__26853)]
           (if
            (clojure.core/= 'or left__26854)
            (clojure.core/let
             [left__26855 (clojure.core/next left__26853)]
             ((.view-fn (one-or-more pattern))
              left__26855
              (clojure.core/fn
               [output__26856 rest__26857]
               (clojure.core/let
                [patterns output__26856]
                (if
                 (clojure.core/= nil rest__26857)
                 (.invoke true-cont__26833 (apply or-ast patterns) nil)
                 (thunk__26852
                  true-cont__26833
                  false-cont__26834
                  input__26832))))
              (clojure.core/fn
               []
               (thunk__26852
                true-cont__26833
                false-cont__26834
                input__26832))))
            (thunk__26852
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26852
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26852 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26852 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26864
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26859 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26859)
          (clojure.core/let
           [left__26860 (clojure.core/first left__26859)]
           (if
            (clojure.core/= 'seq left__26860)
            (clojure.core/let
             [left__26861 (clojure.core/next left__26859)]
             ((.view-fn (one-or-more pattern))
              left__26861
              (clojure.core/fn
               [output__26862 rest__26863]
               (clojure.core/let
                [patterns output__26862]
                (if
                 (clojure.core/= nil rest__26863)
                 (.invoke
                  true-cont__26833
                  (apply seq-ast patterns)
                  nil)
                 (thunk__26858
                  true-cont__26833
                  false-cont__26834
                  input__26832))))
              (clojure.core/fn
               []
               (thunk__26858
                true-cont__26833
                false-cont__26834
                input__26832))))
            (thunk__26858
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26858
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26858 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26858 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26870
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26865 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26865)
          (clojure.core/let
           [left__26866 (clojure.core/first left__26865)]
           (if
            (clojure.core/= 'and left__26866)
            (clojure.core/let
             [left__26867 (clojure.core/next left__26865)]
             ((.view-fn (one-or-more pattern))
              left__26867
              (clojure.core/fn
               [output__26868 rest__26869]
               (clojure.core/let
                [patterns output__26868]
                (if
                 (clojure.core/= nil rest__26869)
                 (.invoke
                  true-cont__26833
                  (apply and-ast patterns)
                  nil)
                 (thunk__26864
                  true-cont__26833
                  false-cont__26834
                  input__26832))))
              (clojure.core/fn
               []
               (thunk__26864
                true-cont__26833
                false-cont__26834
                input__26832))))
            (thunk__26864
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26864
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26864 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26864 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26876
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26871 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26871)
          (clojure.core/let
           [left__26872 (clojure.core/first left__26871)]
           (if
            (clojure.core/= 'leave left__26872)
            (clojure.core/let
             [left__26873 (clojure.core/next left__26871)]
             (if
              (clojure.core/not= nil left__26873)
              (clojure.core/let
               [left__26874 (clojure.core/first left__26873)]
               (clojure.core/let
                [form left__26874]
                (clojure.core/let
                 [left__26875 (clojure.core/next left__26873)]
                 (if
                  (clojure.core/= nil left__26875)
                  (.invoke true-cont__26833 (->Leave form) nil)
                  (thunk__26870
                   true-cont__26833
                   false-cont__26834
                   input__26832)))))
              (thunk__26870
               true-cont__26833
               false-cont__26834
               input__26832)))
            (thunk__26870
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26870
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26870 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26870 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26882
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26877 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26877)
          (clojure.core/let
           [left__26878 (clojure.core/first left__26877)]
           (if
            (clojure.core/= 'guard left__26878)
            (clojure.core/let
             [left__26879 (clojure.core/next left__26877)]
             (if
              (clojure.core/not= nil left__26879)
              (clojure.core/let
               [left__26880 (clojure.core/first left__26879)]
               (clojure.core/let
                [form left__26880]
                (clojure.core/let
                 [left__26881 (clojure.core/next left__26879)]
                 (if
                  (clojure.core/= nil left__26881)
                  (.invoke true-cont__26833 (->Guard form) nil)
                  (thunk__26876
                   true-cont__26833
                   false-cont__26834
                   input__26832)))))
              (thunk__26876
               true-cont__26833
               false-cont__26834
               input__26832)))
            (thunk__26876
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26876
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26876 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26876 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26888
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26883 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26883)
          (clojure.core/let
           [left__26884 (clojure.core/first left__26883)]
           (if
            (clojure.core/= 'quote left__26884)
            (clojure.core/let
             [left__26885 (clojure.core/next left__26883)]
             (if
              (clojure.core/not= nil left__26885)
              (clojure.core/let
               [left__26886 (clojure.core/first left__26885)]
               (clojure.core/let
                [quoted left__26886]
                (clojure.core/let
                 [left__26887 (clojure.core/next left__26885)]
                 (if
                  (clojure.core/= nil left__26887)
                  (.invoke
                   true-cont__26833
                   (literal-ast
                    (clojure.core/seq
                     (clojure.core/concat
                      (clojure.core/list 'quote)
                      (clojure.core/list quoted))))
                   nil)
                  (thunk__26882
                   true-cont__26833
                   false-cont__26834
                   input__26832)))))
              (thunk__26882
               true-cont__26833
               false-cont__26834
               input__26832)))
            (thunk__26882
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26882
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26882 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26882 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26894
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26889 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26889)
          (clojure.core/let
           [left__26890 (clojure.core/first left__26889)]
           (if
            (clojure.core/= 'prefix left__26890)
            (clojure.core/let
             [left__26891 (clojure.core/next left__26889)]
             ((.view-fn (zero-or-more seq-pattern))
              left__26891
              (clojure.core/fn
               [output__26892 rest__26893]
               (clojure.core/let
                [seq-patterns output__26892]
                (if
                 (clojure.core/= nil rest__26893)
                 (.invoke
                  true-cont__26833
                  (apply prefix-ast seq-patterns)
                  nil)
                 (thunk__26888
                  true-cont__26833
                  false-cont__26834
                  input__26832))))
              (clojure.core/fn
               []
               (thunk__26888
                true-cont__26833
                false-cont__26834
                input__26832))))
            (thunk__26888
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26888
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26888 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26888 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26898
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (vector? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26895 (clojure.core/seq input__26832)]
         ((.view-fn (zero-or-more seq-pattern))
          left__26895
          (clojure.core/fn
           [output__26896 rest__26897]
           (clojure.core/let
            [seq-patterns output__26896]
            (if
             (clojure.core/= nil rest__26897)
             (.invoke
              true-cont__26833
              (apply seqable-ast seq-patterns)
              nil)
             (thunk__26894
              true-cont__26833
              false-cont__26834
              input__26832))))
          (clojure.core/fn
           []
           (thunk__26894
            true-cont__26833
            false-cont__26834
            input__26832))))
        (thunk__26894 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26894 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26908
      [left__26899
       true-case-input__26901
       true-cont__26833
       false-cont__26834
       input__26832]
      (if
       (clojure.core/= nil true-case-input__26901)
       (clojure.core/let
        [left__26902 (clojure.core/next left__26899)]
        (if
         (clojure.core/not= nil left__26902)
         (clojure.core/let
          [left__26903 (clojure.core/first left__26902)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__26903)
            (clojure.core/nil? left__26903))
           (clojure.core/let
            [left__26904 (clojure.core/seq left__26903)]
            (if
             (clojure.core/not= nil left__26904)
             (clojure.core/let
              [left__26905 (clojure.core/first left__26904)]
              (clojure.core/let
               [arg left__26905]
               (clojure.core/let
                [left__26906 (clojure.core/next left__26904)]
                (if
                 (clojure.core/= nil left__26906)
                 (clojure.core/let
                  [left__26907 (clojure.core/next left__26902)]
                  (clojure.core/let
                   [body left__26907]
                   (.invoke
                    true-cont__26833
                    (predicate-ast
                     (clojure.core/seq
                      (clojure.core/concat
                       (clojure.core/list 'do)
                       (clojure.walk/prewalk-replace
                        {arg input-sym}
                        body))))
                    nil)))
                 (thunk__26898
                  true-cont__26833
                  false-cont__26834
                  input__26832)))))
             (thunk__26898
              true-cont__26833
              false-cont__26834
              input__26832)))
           (thunk__26898
            true-cont__26833
            false-cont__26834
            input__26832)))
         (thunk__26898
          true-cont__26833
          false-cont__26834
          input__26832)))
       (thunk__26898 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26909
      [left__26899
       true-cont__26833
       false-cont__26834
       left__26900
       input__26832]
      (if
       (clojure.core/= 'fn* left__26900)
       (thunk__26908
        left__26899
        nil
        true-cont__26833
        false-cont__26834
        input__26832)
       (thunk__26898 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26910
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26899 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26899)
          (clojure.core/let
           [left__26900 (clojure.core/first left__26899)]
           (if
            (clojure.core/= 'fn left__26900)
            (thunk__26908
             left__26899
             nil
             true-cont__26833
             false-cont__26834
             input__26832)
            (thunk__26909
             left__26899
             true-cont__26833
             false-cont__26834
             left__26900
             input__26832)))
          (thunk__26898
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26898 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26898 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26918
      [true-cont__26833
       left__26911
       false-cont__26834
       true-case-input__26913
       input__26832]
      (if
       (clojure.core/= nil true-case-input__26913)
       (clojure.core/let
        [left__26914 (clojure.core/next left__26911)]
        (if
         (clojure.core/not= nil left__26914)
         (clojure.core/let
          [left__26915 (clojure.core/first left__26914)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__26915)
            (clojure.core/nil? left__26915))
           (clojure.core/let
            [left__26916 (clojure.core/seq left__26915)]
            (if
             (clojure.core/= nil left__26916)
             (clojure.core/let
              [left__26917 (clojure.core/next left__26914)]
              (clojure.core/let
               [body left__26917]
               (.invoke
                true-cont__26833
                (predicate-ast
                 (clojure.core/seq
                  (clojure.core/concat (clojure.core/list 'do) body)))
                nil)))
             (thunk__26910
              true-cont__26833
              false-cont__26834
              input__26832)))
           (thunk__26910
            true-cont__26833
            false-cont__26834
            input__26832)))
         (thunk__26910
          true-cont__26833
          false-cont__26834
          input__26832)))
       (thunk__26910 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26919
      [true-cont__26833
       left__26911
       left__26912
       false-cont__26834
       input__26832]
      (if
       (clojure.core/= 'fn* left__26912)
       (thunk__26918
        true-cont__26833
        left__26911
        false-cont__26834
        nil
        input__26832)
       (thunk__26910 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26920
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26911 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26911)
          (clojure.core/let
           [left__26912 (clojure.core/first left__26911)]
           (if
            (clojure.core/= 'fn left__26912)
            (thunk__26918
             true-cont__26833
             left__26911
             false-cont__26834
             nil
             input__26832)
            (thunk__26919
             true-cont__26833
             left__26911
             left__26912
             false-cont__26834
             input__26832)))
          (thunk__26910
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26910 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26910 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26921
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (predicate? input__26832)
       (clojure.core/let
        [predicate input__26832]
        (.invoke
         true-cont__26833
         (predicate-ast
          (clojure.core/seq
           (clojure.core/concat
            (clojure.core/list predicate)
            (clojure.core/list input-sym))))
         nil))
       (thunk__26920 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26922
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (clojure.core/instance? java.util.regex.Pattern input__26832)
       (clojure.core/let
        [regex input__26832]
        (.invoke true-cont__26833 (regex-ast regex) nil))
       (thunk__26921 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26928
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (seq? input__26832)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26832)
         (clojure.core/nil? input__26832))
        (clojure.core/let
         [left__26923 (clojure.core/seq input__26832)]
         (if
          (clojure.core/not= nil left__26923)
          (clojure.core/let
           [left__26924 (clojure.core/first left__26923)]
           (if
            (constructor? left__26924)
            (clojure.core/let
             [constructor left__26924]
             (clojure.core/let
              [left__26925 (clojure.core/next left__26923)]
              ((.view-fn (zero-or-more pattern))
               left__26925
               (clojure.core/fn
                [output__26926 rest__26927]
                (clojure.core/let
                 [arg-patterns output__26926]
                 (if
                  (clojure.core/= nil rest__26927)
                  (.invoke
                   true-cont__26833
                   (constructor-ast
                    (constructor-name constructor)
                    arg-patterns)
                   nil)
                  (thunk__26922
                   true-cont__26833
                   false-cont__26834
                   input__26832))))
               (clojure.core/fn
                []
                (thunk__26922
                 true-cont__26833
                 false-cont__26834
                 input__26832)))))
            (thunk__26922
             true-cont__26833
             false-cont__26834
             input__26832)))
          (thunk__26922
           true-cont__26833
           false-cont__26834
           input__26832)))
        (thunk__26922 true-cont__26833 false-cont__26834 input__26832))
       (thunk__26922 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26933
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__26832)
        (clojure.core/nil? input__26832))
       (clojure.core/let
        [left__26930 (clojure.core/seq input__26832)]
        ((.view-fn (zero-or-more key&pattern))
         left__26930
         (clojure.core/fn
          [output__26931 rest__26932]
          (clojure.core/let
           [keys&patterns output__26931]
           (if
            (clojure.core/= nil rest__26932)
            (.invoke true-cont__26833 (map-ast keys&patterns) nil)
            (thunk__26928
             true-cont__26833
             false-cont__26834
             input__26832))))
         (clojure.core/fn
          []
          (thunk__26928
           true-cont__26833
           false-cont__26834
           input__26832))))
       (thunk__26928 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26934
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentHashMap
        input__26832)
       (thunk__26933 true-cont__26833 false-cont__26834 input__26832)
       (thunk__26928 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26935
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentArrayMap
        input__26832)
       (thunk__26933 true-cont__26833 false-cont__26834 input__26832)
       (thunk__26934 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26936
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (class-name? input__26832)
       (clojure.core/let
        [class-name input__26832]
        (.invoke true-cont__26833 (class-ast class-name) nil))
       (thunk__26935 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26937
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (primitive? input__26832)
       (clojure.core/let
        [primitive input__26832]
        (.invoke true-cont__26833 (literal-ast primitive) nil))
       (thunk__26936 true-cont__26833 false-cont__26834 input__26832)))
     (thunk__26938
      [true-cont__26833 false-cont__26834 input__26832]
      (if
       (binding? input__26832)
       (clojure.core/let
        [binding input__26832]
        (.invoke true-cont__26833 (->Bind (binding-name binding)) nil))
       (thunk__26937
        true-cont__26833
        false-cont__26834
        input__26832)))]
    (strucjure/->View
     (clojure.core/fn
      [input__26832 true-cont__26833 false-cont__26834]
      (if
       (clojure.core/= '_ input__26832)
       (.invoke true-cont__26833 (->Leave nil) nil)
       (thunk__26938
        true-cont__26833
        false-cont__26834
        input__26832))))))
 (def
  seq-pattern
  '(clojure.core/letfn
    [(thunk__26945
      [true-cont__26940 false-cont__26941 input__26939]
      ((.view-fn pattern)
       input__26939
       (clojure.core/fn
        [output__26943 rest__26944]
        (clojure.core/let
         [pattern output__26943]
         (.invoke true-cont__26940 (head-ast pattern) rest__26944)))
       (clojure.core/fn [] (.invoke false-cont__26941))))
     (thunk__26951
      [true-cont__26940 false-cont__26941 input__26939]
      (if
       (seq? input__26939)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26939)
         (clojure.core/nil? input__26939))
        (clojure.core/let
         [left__26946 (clojure.core/seq input__26939)]
         (if
          (clojure.core/not= nil left__26946)
          (clojure.core/let
           [left__26947 (clojure.core/first left__26946)]
           (if
            (clojure.core/= 'guard left__26947)
            (clojure.core/let
             [left__26948 (clojure.core/next left__26946)]
             (if
              (clojure.core/not= nil left__26948)
              (clojure.core/let
               [left__26949 (clojure.core/first left__26948)]
               (clojure.core/let
                [form left__26949]
                (clojure.core/let
                 [left__26950 (clojure.core/next left__26948)]
                 (if
                  (clojure.core/= nil left__26950)
                  (.invoke true-cont__26940 (->Guard form) nil)
                  (thunk__26945
                   true-cont__26940
                   false-cont__26941
                   input__26939)))))
              (thunk__26945
               true-cont__26940
               false-cont__26941
               input__26939)))
            (thunk__26945
             true-cont__26940
             false-cont__26941
             input__26939)))
          (thunk__26945
           true-cont__26940
           false-cont__26941
           input__26939)))
        (thunk__26945 true-cont__26940 false-cont__26941 input__26939))
       (thunk__26945
        true-cont__26940
        false-cont__26941
        input__26939)))]
    (strucjure/->View
     (clojure.core/fn
      [input__26939 true-cont__26940 false-cont__26941]
      (if
       (seq? input__26939)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__26939)
         (clojure.core/nil? input__26939))
        (clojure.core/let
         [left__26952 (clojure.core/seq input__26939)]
         (if
          (clojure.core/not= nil left__26952)
          (clojure.core/let
           [left__26953 (clojure.core/first left__26952)]
           (if
            (clojure.core/= '& left__26953)
            (clojure.core/let
             [left__26954 (clojure.core/next left__26952)]
             (if
              (clojure.core/not= nil left__26954)
              (clojure.core/let
               [left__26955 (clojure.core/first left__26954)]
               ((.view-fn pattern)
                left__26955
                (clojure.core/fn
                 [output__26956 rest__26957]
                 (clojure.core/let
                  [pattern output__26956]
                  (if
                   (clojure.core/= nil rest__26957)
                   (clojure.core/let
                    [left__26958 (clojure.core/next left__26954)]
                    (if
                     (clojure.core/= nil left__26958)
                     (.invoke true-cont__26940 pattern nil)
                     (thunk__26951
                      true-cont__26940
                      false-cont__26941
                      input__26939)))
                   (thunk__26951
                    true-cont__26940
                    false-cont__26941
                    input__26939))))
                (clojure.core/fn
                 []
                 (thunk__26951
                  true-cont__26940
                  false-cont__26941
                  input__26939))))
              (thunk__26951
               true-cont__26940
               false-cont__26941
               input__26939)))
            (thunk__26951
             true-cont__26940
             false-cont__26941
             input__26939)))
          (thunk__26951
           true-cont__26940
           false-cont__26941
           input__26939)))
        (thunk__26951 true-cont__26940 false-cont__26941 input__26939))
       (thunk__26951
        true-cont__26940
        false-cont__26941
        input__26939)))))))
