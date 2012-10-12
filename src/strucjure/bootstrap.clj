(ns strucjure.bootstrap)
(do
 (def
  optional
  '(clojure.core/letfn
    [(thunk__13714
      [true-cont__13710 input__13709 false-cont__13711]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13709)
        (clojure.core/nil? input__13709))
       (clojure.core/let
        [left__13713 (clojure.core/seq input__13709)]
        (.invoke true-cont__13710 nil left__13713))
       (.invoke false-cont__13711)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__13709 true-cont__13710 false-cont__13711]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13709)
         (clojure.core/nil? input__13709))
        (clojure.core/let
         [left__13715 (clojure.core/seq input__13709)]
         (if
          (clojure.core/not= nil left__13715)
          (clojure.core/let
           [left__13716 (clojure.core/first left__13715)]
           ((.view-fn elem)
            left__13716
            (clojure.core/fn
             [output__13717 rest__13718]
             (clojure.core/let
              [x output__13717]
              (if
               (clojure.core/= nil rest__13718)
               (clojure.core/let
                [left__13719 (clojure.core/next left__13715)]
                (.invoke true-cont__13710 x left__13719))
               (thunk__13714
                true-cont__13710
                input__13709
                false-cont__13711))))
            (clojure.core/fn
             []
             (thunk__13714
              true-cont__13710
              input__13709
              false-cont__13711))))
          (thunk__13714
           true-cont__13710
           input__13709
           false-cont__13711)))
        (thunk__13714
         true-cont__13710
         input__13709
         false-cont__13711)))))))
 (def
  zero-or-more
  '(clojure.core/letfn
    [(thunk__13725
      [true-cont__13721 false-cont__13722 input__13720]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13720)
        (clojure.core/nil? input__13720))
       (clojure.core/let
        [left__13724 (clojure.core/seq input__13720)]
        (.invoke true-cont__13721 nil left__13724))
       (.invoke false-cont__13722)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__13720 true-cont__13721 false-cont__13722]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13720)
         (clojure.core/nil? input__13720))
        (clojure.core/let
         [left__13726 (clojure.core/seq input__13720)]
         (if
          (clojure.core/not= nil left__13726)
          (clojure.core/let
           [left__13727 (clojure.core/first left__13726)]
           ((.view-fn elem)
            left__13727
            (clojure.core/fn
             [output__13728 rest__13729]
             (clojure.core/let
              [x output__13728]
              (if
               (clojure.core/= nil rest__13729)
               (clojure.core/let
                [left__13730 (clojure.core/next left__13726)]
                ((.view-fn (zero-or-more elem))
                 left__13730
                 (clojure.core/fn
                  [output__13731 rest__13732]
                  (clojure.core/let
                   [xs output__13731]
                   (.invoke true-cont__13721 (cons x xs) rest__13732)))
                 (clojure.core/fn
                  []
                  (thunk__13725
                   true-cont__13721
                   false-cont__13722
                   input__13720))))
               (thunk__13725
                true-cont__13721
                false-cont__13722
                input__13720))))
            (clojure.core/fn
             []
             (thunk__13725
              true-cont__13721
              false-cont__13722
              input__13720))))
          (thunk__13725
           true-cont__13721
           false-cont__13722
           input__13720)))
        (thunk__13725
         true-cont__13721
         false-cont__13722
         input__13720)))))))
 (def
  one-or-more
  '(clojure.core/letfn
    []
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__13733 true-cont__13734 false-cont__13735]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13733)
         (clojure.core/nil? input__13733))
        (clojure.core/let
         [left__13737 (clojure.core/seq input__13733)]
         (if
          (clojure.core/not= nil left__13737)
          (clojure.core/let
           [left__13738 (clojure.core/first left__13737)]
           ((.view-fn elem)
            left__13738
            (clojure.core/fn
             [output__13739 rest__13740]
             (clojure.core/let
              [x output__13739]
              (if
               (clojure.core/= nil rest__13740)
               (clojure.core/let
                [left__13741 (clojure.core/next left__13737)]
                ((.view-fn (zero-or-more elem))
                 left__13741
                 (clojure.core/fn
                  [output__13742 rest__13743]
                  (clojure.core/let
                   [xs output__13742]
                   (.invoke true-cont__13734 (cons x xs) rest__13743)))
                 (clojure.core/fn [] (.invoke false-cont__13735))))
               (.invoke false-cont__13735))))
            (clojure.core/fn [] (.invoke false-cont__13735))))
          (.invoke false-cont__13735)))
        (.invoke false-cont__13735)))))))
 (def
  zero-or-more-prefix
  '(clojure.core/letfn
    [(thunk__13749
      [false-cont__13746 input__13744 true-cont__13745]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13744)
        (clojure.core/nil? input__13744))
       (clojure.core/let
        [left__13748 (clojure.core/seq input__13744)]
        (.invoke true-cont__13745 nil left__13748))
       (.invoke false-cont__13746)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__13744 true-cont__13745 false-cont__13746]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13744)
         (clojure.core/nil? input__13744))
        (clojure.core/let
         [left__13750 (clojure.core/seq input__13744)]
         ((.view-fn elem)
          left__13750
          (clojure.core/fn
           [output__13751 rest__13752]
           (clojure.core/let
            [x output__13751]
            ((.view-fn (zero-or-more-prefix elem))
             rest__13752
             (clojure.core/fn
              [output__13753 rest__13754]
              (clojure.core/let
               [xs output__13753]
               (.invoke true-cont__13745 (cons x xs) rest__13754)))
             (clojure.core/fn
              []
              (thunk__13749
               false-cont__13746
               input__13744
               true-cont__13745)))))
          (clojure.core/fn
           []
           (thunk__13749
            false-cont__13746
            input__13744
            true-cont__13745))))
        (thunk__13749
         false-cont__13746
         input__13744
         true-cont__13745)))))))
 (def
  one-or-more-prefix
  '(clojure.core/letfn
    []
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__13755 true-cont__13756 false-cont__13757]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13755)
         (clojure.core/nil? input__13755))
        (clojure.core/let
         [left__13759 (clojure.core/seq input__13755)]
         ((.view-fn elem)
          left__13759
          (clojure.core/fn
           [output__13760 rest__13761]
           (clojure.core/let
            [x output__13760]
            ((.view-fn (zero-or-more-prefix elem))
             rest__13761
             (clojure.core/fn
              [output__13762 rest__13763]
              (clojure.core/let
               [xs output__13762]
               (.invoke true-cont__13756 (cons x xs) rest__13763)))
             (clojure.core/fn [] (.invoke false-cont__13757)))))
          (clojure.core/fn [] (.invoke false-cont__13757))))
        (.invoke false-cont__13757)))))))
 (def
  key&pattern
  '(clojure.core/letfn
    []
    (strucjure/->View
     (clojure.core/fn
      [input__13764 true-cont__13765 false-cont__13766]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13764)
        (clojure.core/nil? input__13764))
       (clojure.core/let
        [left__13768 (clojure.core/seq input__13764)]
        (if
         (clojure.core/not= nil left__13768)
         (clojure.core/let
          [left__13769 (clojure.core/first left__13768)]
          (clojure.core/let
           [key left__13769]
           (clojure.core/let
            [left__13770 (clojure.core/next left__13768)]
            (if
             (clojure.core/not= nil left__13770)
             (clojure.core/let
              [left__13771 (clojure.core/first left__13770)]
              ((.view-fn pattern)
               left__13771
               (clojure.core/fn
                [output__13772 rest__13773]
                (clojure.core/let
                 [pattern output__13772]
                 (if
                  (clojure.core/= nil rest__13773)
                  (clojure.core/let
                   [left__13774 (clojure.core/next left__13770)]
                   (if
                    (clojure.core/= nil left__13774)
                    (.invoke true-cont__13765 [key pattern] nil)
                    (.invoke false-cont__13766)))
                  (.invoke false-cont__13766))))
               (clojure.core/fn [] (.invoke false-cont__13766))))
             (.invoke false-cont__13766)))))
         (.invoke false-cont__13766)))
       (.invoke false-cont__13766))))))
 (def
  pattern
  '(clojure.core/letfn
    [(thunk__13786
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13779 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13779)
          (clojure.core/let
           [left__13780 (clojure.core/first left__13779)]
           (clojure.core/let
            [view left__13780]
            (clojure.core/let
             [left__13781 (clojure.core/next left__13779)]
             (if
              (clojure.core/not= nil left__13781)
              (clojure.core/let
               [left__13782 (clojure.core/first left__13781)]
               ((.view-fn pattern)
                left__13782
                (clojure.core/fn
                 [output__13783 rest__13784]
                 (clojure.core/let
                  [pattern output__13783]
                  (if
                   (clojure.core/= nil rest__13784)
                   (clojure.core/let
                    [left__13785 (clojure.core/next left__13781)]
                    (if
                     (clojure.core/= nil left__13785)
                     (.invoke
                      true-cont__13776
                      (import-ast view pattern)
                      nil)
                     (.invoke false-cont__13777)))
                   (.invoke false-cont__13777))))
                (clojure.core/fn [] (.invoke false-cont__13777))))
              (.invoke false-cont__13777)))))
          (.invoke false-cont__13777)))
        (.invoke false-cont__13777))
       (.invoke false-cont__13777)))
     (thunk__13787
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (symbol? input__13775)
       (clojure.core/let
        [variable input__13775]
        (.invoke true-cont__13776 (literal-ast variable) nil))
       (thunk__13786 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13795
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13788 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13788)
          (clojure.core/let
           [left__13789 (clojure.core/first left__13788)]
           (if
            (clojure.core/= 'not left__13789)
            (clojure.core/let
             [left__13790 (clojure.core/next left__13788)]
             (if
              (clojure.core/not= nil left__13790)
              (clojure.core/let
               [left__13791 (clojure.core/first left__13790)]
               ((.view-fn pattern)
                left__13791
                (clojure.core/fn
                 [output__13792 rest__13793]
                 (clojure.core/let
                  [pattern output__13792]
                  (if
                   (clojure.core/= nil rest__13793)
                   (clojure.core/let
                    [left__13794 (clojure.core/next left__13790)]
                    (if
                     (clojure.core/= nil left__13794)
                     (.invoke true-cont__13776 (->Not pattern) nil)
                     (thunk__13787
                      false-cont__13777
                      input__13775
                      true-cont__13776)))
                   (thunk__13787
                    false-cont__13777
                    input__13775
                    true-cont__13776))))
                (clojure.core/fn
                 []
                 (thunk__13787
                  false-cont__13777
                  input__13775
                  true-cont__13776))))
              (thunk__13787
               false-cont__13777
               input__13775
               true-cont__13776)))
            (thunk__13787
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13787
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13787 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13787 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13801
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13796 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13796)
          (clojure.core/let
           [left__13797 (clojure.core/first left__13796)]
           (if
            (clojure.core/= 'or left__13797)
            (clojure.core/let
             [left__13798 (clojure.core/next left__13796)]
             ((.view-fn (one-or-more pattern))
              left__13798
              (clojure.core/fn
               [output__13799 rest__13800]
               (clojure.core/let
                [patterns output__13799]
                (if
                 (clojure.core/= nil rest__13800)
                 (.invoke true-cont__13776 (apply or-ast patterns) nil)
                 (thunk__13795
                  false-cont__13777
                  input__13775
                  true-cont__13776))))
              (clojure.core/fn
               []
               (thunk__13795
                false-cont__13777
                input__13775
                true-cont__13776))))
            (thunk__13795
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13795
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13795 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13795 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13807
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13802 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13802)
          (clojure.core/let
           [left__13803 (clojure.core/first left__13802)]
           (if
            (clojure.core/= 'seq left__13803)
            (clojure.core/let
             [left__13804 (clojure.core/next left__13802)]
             ((.view-fn (one-or-more pattern))
              left__13804
              (clojure.core/fn
               [output__13805 rest__13806]
               (clojure.core/let
                [patterns output__13805]
                (if
                 (clojure.core/= nil rest__13806)
                 (.invoke
                  true-cont__13776
                  (apply seq-ast patterns)
                  nil)
                 (thunk__13801
                  false-cont__13777
                  input__13775
                  true-cont__13776))))
              (clojure.core/fn
               []
               (thunk__13801
                false-cont__13777
                input__13775
                true-cont__13776))))
            (thunk__13801
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13801
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13801 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13801 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13813
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13808 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13808)
          (clojure.core/let
           [left__13809 (clojure.core/first left__13808)]
           (if
            (clojure.core/= 'and left__13809)
            (clojure.core/let
             [left__13810 (clojure.core/next left__13808)]
             ((.view-fn (one-or-more pattern))
              left__13810
              (clojure.core/fn
               [output__13811 rest__13812]
               (clojure.core/let
                [patterns output__13811]
                (if
                 (clojure.core/= nil rest__13812)
                 (.invoke
                  true-cont__13776
                  (apply and-ast patterns)
                  nil)
                 (thunk__13807
                  false-cont__13777
                  input__13775
                  true-cont__13776))))
              (clojure.core/fn
               []
               (thunk__13807
                false-cont__13777
                input__13775
                true-cont__13776))))
            (thunk__13807
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13807
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13807 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13807 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13819
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13814 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13814)
          (clojure.core/let
           [left__13815 (clojure.core/first left__13814)]
           (if
            (clojure.core/= 'leave left__13815)
            (clojure.core/let
             [left__13816 (clojure.core/next left__13814)]
             (if
              (clojure.core/not= nil left__13816)
              (clojure.core/let
               [left__13817 (clojure.core/first left__13816)]
               (clojure.core/let
                [form left__13817]
                (clojure.core/let
                 [left__13818 (clojure.core/next left__13816)]
                 (if
                  (clojure.core/= nil left__13818)
                  (.invoke true-cont__13776 (->Leave form) nil)
                  (thunk__13813
                   false-cont__13777
                   input__13775
                   true-cont__13776)))))
              (thunk__13813
               false-cont__13777
               input__13775
               true-cont__13776)))
            (thunk__13813
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13813
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13813 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13813 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13825
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13820 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13820)
          (clojure.core/let
           [left__13821 (clojure.core/first left__13820)]
           (if
            (clojure.core/= 'guard left__13821)
            (clojure.core/let
             [left__13822 (clojure.core/next left__13820)]
             (if
              (clojure.core/not= nil left__13822)
              (clojure.core/let
               [left__13823 (clojure.core/first left__13822)]
               (clojure.core/let
                [form left__13823]
                (clojure.core/let
                 [left__13824 (clojure.core/next left__13822)]
                 (if
                  (clojure.core/= nil left__13824)
                  (.invoke true-cont__13776 (->Guard form) nil)
                  (thunk__13819
                   false-cont__13777
                   input__13775
                   true-cont__13776)))))
              (thunk__13819
               false-cont__13777
               input__13775
               true-cont__13776)))
            (thunk__13819
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13819
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13819 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13819 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13831
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13826 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13826)
          (clojure.core/let
           [left__13827 (clojure.core/first left__13826)]
           (if
            (clojure.core/= 'quote left__13827)
            (clojure.core/let
             [left__13828 (clojure.core/next left__13826)]
             (if
              (clojure.core/not= nil left__13828)
              (clojure.core/let
               [left__13829 (clojure.core/first left__13828)]
               (clojure.core/let
                [quoted left__13829]
                (clojure.core/let
                 [left__13830 (clojure.core/next left__13828)]
                 (if
                  (clojure.core/= nil left__13830)
                  (.invoke
                   true-cont__13776
                   (literal-ast
                    (clojure.core/seq
                     (clojure.core/concat
                      (clojure.core/list 'quote)
                      (clojure.core/list quoted))))
                   nil)
                  (thunk__13825
                   false-cont__13777
                   input__13775
                   true-cont__13776)))))
              (thunk__13825
               false-cont__13777
               input__13775
               true-cont__13776)))
            (thunk__13825
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13825
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13825 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13825 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13837
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13832 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13832)
          (clojure.core/let
           [left__13833 (clojure.core/first left__13832)]
           (if
            (clojure.core/= 'prefix left__13833)
            (clojure.core/let
             [left__13834 (clojure.core/next left__13832)]
             ((.view-fn (zero-or-more-prefix seq-pattern))
              left__13834
              (clojure.core/fn
               [output__13835 rest__13836]
               (clojure.core/let
                [seq-patterns output__13835]
                (if
                 (clojure.core/= nil rest__13836)
                 (.invoke
                  true-cont__13776
                  (apply prefix-ast seq-patterns)
                  nil)
                 (thunk__13831
                  false-cont__13777
                  input__13775
                  true-cont__13776))))
              (clojure.core/fn
               []
               (thunk__13831
                false-cont__13777
                input__13775
                true-cont__13776))))
            (thunk__13831
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13831
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13831 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13831 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13841
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (vector? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13838 (clojure.core/seq input__13775)]
         ((.view-fn (zero-or-more-prefix seq-pattern))
          left__13838
          (clojure.core/fn
           [output__13839 rest__13840]
           (clojure.core/let
            [seq-patterns output__13839]
            (if
             (clojure.core/= nil rest__13840)
             (.invoke
              true-cont__13776
              (apply seqable-ast seq-patterns)
              nil)
             (thunk__13837
              false-cont__13777
              input__13775
              true-cont__13776))))
          (clojure.core/fn
           []
           (thunk__13837
            false-cont__13777
            input__13775
            true-cont__13776))))
        (thunk__13837 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13837 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13851
      [false-cont__13777
       left__13842
       input__13775
       true-cont__13776
       true-case-input__13844]
      (if
       (clojure.core/= nil true-case-input__13844)
       (clojure.core/let
        [left__13845 (clojure.core/next left__13842)]
        (if
         (clojure.core/not= nil left__13845)
         (clojure.core/let
          [left__13846 (clojure.core/first left__13845)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__13846)
            (clojure.core/nil? left__13846))
           (clojure.core/let
            [left__13847 (clojure.core/seq left__13846)]
            (if
             (clojure.core/not= nil left__13847)
             (clojure.core/let
              [left__13848 (clojure.core/first left__13847)]
              (clojure.core/let
               [arg left__13848]
               (clojure.core/let
                [left__13849 (clojure.core/next left__13847)]
                (if
                 (clojure.core/= nil left__13849)
                 (clojure.core/let
                  [left__13850 (clojure.core/next left__13845)]
                  (clojure.core/let
                   [body left__13850]
                   (.invoke
                    true-cont__13776
                    (predicate-ast
                     (clojure.core/seq
                      (clojure.core/concat
                       (clojure.core/list 'do)
                       (clojure.walk/prewalk-replace
                        {arg input-sym}
                        body))))
                    nil)))
                 (thunk__13841
                  false-cont__13777
                  input__13775
                  true-cont__13776)))))
             (thunk__13841
              false-cont__13777
              input__13775
              true-cont__13776)))
           (thunk__13841
            false-cont__13777
            input__13775
            true-cont__13776)))
         (thunk__13841
          false-cont__13777
          input__13775
          true-cont__13776)))
       (thunk__13841 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13852
      [false-cont__13777
       left__13842
       left__13843
       input__13775
       true-cont__13776]
      (if
       (clojure.core/= 'fn* left__13843)
       (thunk__13851
        false-cont__13777
        left__13842
        input__13775
        true-cont__13776
        nil)
       (thunk__13841 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13853
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13842 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13842)
          (clojure.core/let
           [left__13843 (clojure.core/first left__13842)]
           (if
            (clojure.core/= 'fn left__13843)
            (thunk__13851
             false-cont__13777
             left__13842
             input__13775
             true-cont__13776
             nil)
            (thunk__13852
             false-cont__13777
             left__13842
             left__13843
             input__13775
             true-cont__13776)))
          (thunk__13841
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13841 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13841 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13861
      [left__13854
       false-cont__13777
       true-case-input__13856
       input__13775
       true-cont__13776]
      (if
       (clojure.core/= nil true-case-input__13856)
       (clojure.core/let
        [left__13857 (clojure.core/next left__13854)]
        (if
         (clojure.core/not= nil left__13857)
         (clojure.core/let
          [left__13858 (clojure.core/first left__13857)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__13858)
            (clojure.core/nil? left__13858))
           (clojure.core/let
            [left__13859 (clojure.core/seq left__13858)]
            (if
             (clojure.core/= nil left__13859)
             (clojure.core/let
              [left__13860 (clojure.core/next left__13857)]
              (clojure.core/let
               [body left__13860]
               (.invoke
                true-cont__13776
                (predicate-ast
                 (clojure.core/seq
                  (clojure.core/concat (clojure.core/list 'do) body)))
                nil)))
             (thunk__13853
              false-cont__13777
              input__13775
              true-cont__13776)))
           (thunk__13853
            false-cont__13777
            input__13775
            true-cont__13776)))
         (thunk__13853
          false-cont__13777
          input__13775
          true-cont__13776)))
       (thunk__13853 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13862
      [left__13854
       left__13855
       false-cont__13777
       input__13775
       true-cont__13776]
      (if
       (clojure.core/= 'fn* left__13855)
       (thunk__13861
        left__13854
        false-cont__13777
        nil
        input__13775
        true-cont__13776)
       (thunk__13853 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13863
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13854 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13854)
          (clojure.core/let
           [left__13855 (clojure.core/first left__13854)]
           (if
            (clojure.core/= 'fn left__13855)
            (thunk__13861
             left__13854
             false-cont__13777
             nil
             input__13775
             true-cont__13776)
            (thunk__13862
             left__13854
             left__13855
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13853
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13853 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13853 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13864
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (predicate? input__13775)
       (clojure.core/let
        [predicate input__13775]
        (.invoke
         true-cont__13776
         (predicate-ast
          (clojure.core/seq
           (clojure.core/concat
            (clojure.core/list predicate)
            (clojure.core/list input-sym))))
         nil))
       (thunk__13863 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13865
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (clojure.core/instance? java.util.regex.Pattern input__13775)
       (clojure.core/let
        [regex input__13775]
        (.invoke true-cont__13776 (regex-ast regex) nil))
       (thunk__13864 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13871
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (seq? input__13775)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13775)
         (clojure.core/nil? input__13775))
        (clojure.core/let
         [left__13866 (clojure.core/seq input__13775)]
         (if
          (clojure.core/not= nil left__13866)
          (clojure.core/let
           [left__13867 (clojure.core/first left__13866)]
           (if
            (constructor? left__13867)
            (clojure.core/let
             [constructor left__13867]
             (clojure.core/let
              [left__13868 (clojure.core/next left__13866)]
              ((.view-fn (zero-or-more pattern))
               left__13868
               (clojure.core/fn
                [output__13869 rest__13870]
                (clojure.core/let
                 [arg-patterns output__13869]
                 (if
                  (clojure.core/= nil rest__13870)
                  (.invoke
                   true-cont__13776
                   (constructor-ast
                    (constructor-name constructor)
                    arg-patterns)
                   nil)
                  (thunk__13865
                   false-cont__13777
                   input__13775
                   true-cont__13776))))
               (clojure.core/fn
                []
                (thunk__13865
                 false-cont__13777
                 input__13775
                 true-cont__13776)))))
            (thunk__13865
             false-cont__13777
             input__13775
             true-cont__13776)))
          (thunk__13865
           false-cont__13777
           input__13775
           true-cont__13776)))
        (thunk__13865 false-cont__13777 input__13775 true-cont__13776))
       (thunk__13865 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13876
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13775)
        (clojure.core/nil? input__13775))
       (clojure.core/let
        [left__13873 (clojure.core/seq input__13775)]
        ((.view-fn (zero-or-more key&pattern))
         left__13873
         (clojure.core/fn
          [output__13874 rest__13875]
          (clojure.core/let
           [keys&patterns output__13874]
           (if
            (clojure.core/= nil rest__13875)
            (.invoke true-cont__13776 (map-ast keys&patterns) nil)
            (thunk__13871
             false-cont__13777
             input__13775
             true-cont__13776))))
         (clojure.core/fn
          []
          (thunk__13871
           false-cont__13777
           input__13775
           true-cont__13776))))
       (thunk__13871 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13877
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentHashMap
        input__13775)
       (thunk__13876 false-cont__13777 input__13775 true-cont__13776)
       (thunk__13871 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13878
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentArrayMap
        input__13775)
       (thunk__13876 false-cont__13777 input__13775 true-cont__13776)
       (thunk__13877 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13879
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (class-name? input__13775)
       (clojure.core/let
        [class-name input__13775]
        (.invoke true-cont__13776 (class-ast class-name) nil))
       (thunk__13878 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13880
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (primitive? input__13775)
       (clojure.core/let
        [primitive input__13775]
        (.invoke true-cont__13776 (literal-ast primitive) nil))
       (thunk__13879 false-cont__13777 input__13775 true-cont__13776)))
     (thunk__13881
      [false-cont__13777 input__13775 true-cont__13776]
      (if
       (binding? input__13775)
       (clojure.core/let
        [binding input__13775]
        (.invoke true-cont__13776 (->Bind (binding-name binding)) nil))
       (thunk__13880
        false-cont__13777
        input__13775
        true-cont__13776)))]
    (strucjure/->View
     (clojure.core/fn
      [input__13775 true-cont__13776 false-cont__13777]
      (if
       (clojure.core/= '_ input__13775)
       (.invoke true-cont__13776 (->Leave nil) nil)
       (thunk__13881
        false-cont__13777
        input__13775
        true-cont__13776))))))
 (def
  seq-pattern
  '(clojure.core/letfn
    [(thunk__13891
      [true-cont__13883 input__13882 false-cont__13884]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13882)
        (clojure.core/nil? input__13882))
       (clojure.core/let
        [left__13886 (clojure.core/seq input__13882)]
        (if
         (clojure.core/not= nil left__13886)
         (clojure.core/let
          [left__13887 (clojure.core/first left__13886)]
          ((.view-fn pattern)
           left__13887
           (clojure.core/fn
            [output__13888 rest__13889]
            (clojure.core/let
             [pattern output__13888]
             (if
              (clojure.core/= nil rest__13889)
              (clojure.core/let
               [left__13890 (clojure.core/next left__13886)]
               (.invoke
                true-cont__13883
                (head-ast pattern)
                left__13890))
              (.invoke false-cont__13884))))
           (clojure.core/fn [] (.invoke false-cont__13884))))
         (.invoke false-cont__13884)))
       (.invoke false-cont__13884)))
     (thunk__13900
      [true-cont__13883 input__13882 false-cont__13884]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13882)
        (clojure.core/nil? input__13882))
       (clojure.core/let
        [left__13892 (clojure.core/seq input__13882)]
        (if
         (clojure.core/not= nil left__13892)
         (clojure.core/let
          [left__13893 (clojure.core/first left__13892)]
          (if
           (seq? left__13893)
           (if
            (clojure.core/or
             (clojure.core/instance? clojure.lang.Seqable left__13893)
             (clojure.core/nil? left__13893))
            (clojure.core/let
             [left__13894 (clojure.core/seq left__13893)]
             (if
              (clojure.core/not= nil left__13894)
              (clojure.core/let
               [left__13895 (clojure.core/first left__13894)]
               (if
                (clojure.core/= 'guard left__13895)
                (clojure.core/let
                 [left__13896 (clojure.core/next left__13894)]
                 (if
                  (clojure.core/not= nil left__13896)
                  (clojure.core/let
                   [left__13897 (clojure.core/first left__13896)]
                   (clojure.core/let
                    [form left__13897]
                    (clojure.core/let
                     [left__13898 (clojure.core/next left__13896)]
                     (if
                      (clojure.core/= nil left__13898)
                      (clojure.core/let
                       [left__13899 (clojure.core/next left__13892)]
                       (.invoke
                        true-cont__13883
                        (->Guard form)
                        left__13899))
                      (thunk__13891
                       true-cont__13883
                       input__13882
                       false-cont__13884)))))
                  (thunk__13891
                   true-cont__13883
                   input__13882
                   false-cont__13884)))
                (thunk__13891
                 true-cont__13883
                 input__13882
                 false-cont__13884)))
              (thunk__13891
               true-cont__13883
               input__13882
               false-cont__13884)))
            (thunk__13891
             true-cont__13883
             input__13882
             false-cont__13884))
           (thunk__13891
            true-cont__13883
            input__13882
            false-cont__13884)))
         (thunk__13891
          true-cont__13883
          input__13882
          false-cont__13884)))
       (thunk__13891
        true-cont__13883
        input__13882
        false-cont__13884)))]
    (strucjure/->View
     (clojure.core/fn
      [input__13882 true-cont__13883 false-cont__13884]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13882)
        (clojure.core/nil? input__13882))
       (clojure.core/let
        [left__13901 (clojure.core/seq input__13882)]
        (if
         (clojure.core/not= nil left__13901)
         (clojure.core/let
          [left__13902 (clojure.core/first left__13901)]
          (if
           (clojure.core/= '& left__13902)
           (clojure.core/let
            [left__13903 (clojure.core/next left__13901)]
            (if
             (clojure.core/not= nil left__13903)
             (clojure.core/let
              [left__13904 (clojure.core/first left__13903)]
              ((.view-fn pattern)
               left__13904
               (clojure.core/fn
                [output__13905 rest__13906]
                (clojure.core/let
                 [pattern output__13905]
                 (if
                  (clojure.core/= nil rest__13906)
                  (clojure.core/let
                   [left__13907 (clojure.core/next left__13903)]
                   (.invoke true-cont__13883 pattern left__13907))
                  (thunk__13900
                   true-cont__13883
                   input__13882
                   false-cont__13884))))
               (clojure.core/fn
                []
                (thunk__13900
                 true-cont__13883
                 input__13882
                 false-cont__13884))))
             (thunk__13900
              true-cont__13883
              input__13882
              false-cont__13884)))
           (thunk__13900
            true-cont__13883
            input__13882
            false-cont__13884)))
         (thunk__13900
          true-cont__13883
          input__13882
          false-cont__13884)))
       (thunk__13900
        true-cont__13883
        input__13882
        false-cont__13884)))))))
