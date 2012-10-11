(ns strucjure.bootstrap)
(do
 (def
  optional
  '(clojure.core/letfn
    [(thunk__13889
      [true-cont__13885 false-cont__13886 input__13884]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13884)
        (clojure.core/nil? input__13884))
       (clojure.core/let
        [left__13888 (clojure.core/seq input__13884)]
        (.invoke true-cont__13885 nil left__13888))
       (.invoke false-cont__13886)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__13884 true-cont__13885 false-cont__13886]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13884)
         (clojure.core/nil? input__13884))
        (clojure.core/let
         [left__13890 (clojure.core/seq input__13884)]
         (if
          (clojure.core/not= nil left__13890)
          (clojure.core/let
           [left__13891 (clojure.core/first left__13890)]
           ((.view-fn elem)
            left__13891
            (clojure.core/fn
             [output__13892 rest__13893]
             (clojure.core/let
              [x output__13892]
              (if
               (clojure.core/= nil rest__13893)
               (clojure.core/let
                [left__13894 (clojure.core/next left__13890)]
                (.invoke true-cont__13885 x left__13894))
               (thunk__13889
                true-cont__13885
                false-cont__13886
                input__13884))))
            (clojure.core/fn
             []
             (thunk__13889
              true-cont__13885
              false-cont__13886
              input__13884))))
          (thunk__13889
           true-cont__13885
           false-cont__13886
           input__13884)))
        (thunk__13889
         true-cont__13885
         false-cont__13886
         input__13884)))))))
 (def
  zero-or-more
  '(clojure.core/letfn
    [(thunk__13900
      [false-cont__13897 input__13895 true-cont__13896]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13895)
        (clojure.core/nil? input__13895))
       (clojure.core/let
        [left__13899 (clojure.core/seq input__13895)]
        (.invoke true-cont__13896 nil left__13899))
       (.invoke false-cont__13897)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__13895 true-cont__13896 false-cont__13897]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13895)
         (clojure.core/nil? input__13895))
        (clojure.core/let
         [left__13901 (clojure.core/seq input__13895)]
         (if
          (clojure.core/not= nil left__13901)
          (clojure.core/let
           [left__13902 (clojure.core/first left__13901)]
           ((.view-fn elem)
            left__13902
            (clojure.core/fn
             [output__13903 rest__13904]
             (clojure.core/let
              [x output__13903]
              (if
               (clojure.core/= nil rest__13904)
               (clojure.core/let
                [left__13905 (clojure.core/next left__13901)]
                ((.view-fn (zero-or-more elem))
                 left__13905
                 (clojure.core/fn
                  [output__13906 rest__13907]
                  (clojure.core/let
                   [xs output__13906]
                   (.invoke true-cont__13896 (cons x xs) rest__13907)))
                 (clojure.core/fn
                  []
                  (thunk__13900
                   false-cont__13897
                   input__13895
                   true-cont__13896))))
               (thunk__13900
                false-cont__13897
                input__13895
                true-cont__13896))))
            (clojure.core/fn
             []
             (thunk__13900
              false-cont__13897
              input__13895
              true-cont__13896))))
          (thunk__13900
           false-cont__13897
           input__13895
           true-cont__13896)))
        (thunk__13900
         false-cont__13897
         input__13895
         true-cont__13896)))))))
 (def
  one-or-more
  '(clojure.core/letfn
    []
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__13908 true-cont__13909 false-cont__13910]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13908)
         (clojure.core/nil? input__13908))
        (clojure.core/let
         [left__13912 (clojure.core/seq input__13908)]
         (if
          (clojure.core/not= nil left__13912)
          (clojure.core/let
           [left__13913 (clojure.core/first left__13912)]
           ((.view-fn elem)
            left__13913
            (clojure.core/fn
             [output__13914 rest__13915]
             (clojure.core/let
              [x output__13914]
              (if
               (clojure.core/= nil rest__13915)
               (clojure.core/let
                [left__13916 (clojure.core/next left__13912)]
                ((.view-fn (zero-or-more elem))
                 left__13916
                 (clojure.core/fn
                  [output__13917 rest__13918]
                  (clojure.core/let
                   [xs output__13917]
                   (.invoke true-cont__13909 (cons x xs) rest__13918)))
                 (clojure.core/fn [] (.invoke false-cont__13910))))
               (.invoke false-cont__13910))))
            (clojure.core/fn [] (.invoke false-cont__13910))))
          (.invoke false-cont__13910)))
        (.invoke false-cont__13910)))))))
 (def
  key&pattern
  '(clojure.core/letfn
    []
    (strucjure/->View
     (clojure.core/fn
      [input__13919 true-cont__13920 false-cont__13921]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13919)
        (clojure.core/nil? input__13919))
       (clojure.core/let
        [left__13923 (clojure.core/seq input__13919)]
        (if
         (clojure.core/not= nil left__13923)
         (clojure.core/let
          [left__13924 (clojure.core/first left__13923)]
          (clojure.core/let
           [key left__13924]
           (clojure.core/let
            [left__13925 (clojure.core/next left__13923)]
            (if
             (clojure.core/not= nil left__13925)
             (clojure.core/let
              [left__13926 (clojure.core/first left__13925)]
              ((.view-fn pattern)
               left__13926
               (clojure.core/fn
                [output__13927 rest__13928]
                (clojure.core/let
                 [pattern output__13927]
                 (if
                  (clojure.core/= nil rest__13928)
                  (clojure.core/let
                   [left__13929 (clojure.core/next left__13925)]
                   (if
                    (clojure.core/= nil left__13929)
                    (.invoke true-cont__13920 [key pattern] nil)
                    (.invoke false-cont__13921)))
                  (.invoke false-cont__13921))))
               (clojure.core/fn [] (.invoke false-cont__13921))))
             (.invoke false-cont__13921)))))
         (.invoke false-cont__13921)))
       (.invoke false-cont__13921))))))
 (def
  pattern
  '(clojure.core/letfn
    [(thunk__13941
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13934 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13934)
          (clojure.core/let
           [left__13935 (clojure.core/first left__13934)]
           (clojure.core/let
            [view left__13935]
            (clojure.core/let
             [left__13936 (clojure.core/next left__13934)]
             (if
              (clojure.core/not= nil left__13936)
              (clojure.core/let
               [left__13937 (clojure.core/first left__13936)]
               ((.view-fn pattern)
                left__13937
                (clojure.core/fn
                 [output__13938 rest__13939]
                 (clojure.core/let
                  [pattern output__13938]
                  (if
                   (clojure.core/= nil rest__13939)
                   (clojure.core/let
                    [left__13940 (clojure.core/next left__13936)]
                    (if
                     (clojure.core/= nil left__13940)
                     (.invoke
                      true-cont__13931
                      (import-ast view pattern)
                      nil)
                     (.invoke false-cont__13932)))
                   (.invoke false-cont__13932))))
                (clojure.core/fn [] (.invoke false-cont__13932))))
              (.invoke false-cont__13932)))))
          (.invoke false-cont__13932)))
        (.invoke false-cont__13932))
       (.invoke false-cont__13932)))
     (thunk__13942
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (symbol? input__13930)
       (clojure.core/let
        [variable input__13930]
        (.invoke true-cont__13931 (literal-ast variable) nil))
       (thunk__13941 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13948
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13943 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13943)
          (clojure.core/let
           [left__13944 (clojure.core/first left__13943)]
           (if
            (clojure.core/= 'or left__13944)
            (clojure.core/let
             [left__13945 (clojure.core/next left__13943)]
             ((.view-fn (one-or-more pattern))
              left__13945
              (clojure.core/fn
               [output__13946 rest__13947]
               (clojure.core/let
                [patterns output__13946]
                (if
                 (clojure.core/= nil rest__13947)
                 (.invoke true-cont__13931 (apply or-ast patterns) nil)
                 (thunk__13942
                  false-cont__13932
                  input__13930
                  true-cont__13931))))
              (clojure.core/fn
               []
               (thunk__13942
                false-cont__13932
                input__13930
                true-cont__13931))))
            (thunk__13942
             false-cont__13932
             input__13930
             true-cont__13931)))
          (thunk__13942
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__13942 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13942 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13954
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13949 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13949)
          (clojure.core/let
           [left__13950 (clojure.core/first left__13949)]
           (if
            (clojure.core/= 'seq left__13950)
            (clojure.core/let
             [left__13951 (clojure.core/next left__13949)]
             ((.view-fn (one-or-more pattern))
              left__13951
              (clojure.core/fn
               [output__13952 rest__13953]
               (clojure.core/let
                [patterns output__13952]
                (if
                 (clojure.core/= nil rest__13953)
                 (.invoke
                  true-cont__13931
                  (apply seq-ast patterns)
                  nil)
                 (thunk__13948
                  false-cont__13932
                  input__13930
                  true-cont__13931))))
              (clojure.core/fn
               []
               (thunk__13948
                false-cont__13932
                input__13930
                true-cont__13931))))
            (thunk__13948
             false-cont__13932
             input__13930
             true-cont__13931)))
          (thunk__13948
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__13948 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13948 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13960
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13955 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13955)
          (clojure.core/let
           [left__13956 (clojure.core/first left__13955)]
           (if
            (clojure.core/= 'and left__13956)
            (clojure.core/let
             [left__13957 (clojure.core/next left__13955)]
             ((.view-fn (one-or-more pattern))
              left__13957
              (clojure.core/fn
               [output__13958 rest__13959]
               (clojure.core/let
                [patterns output__13958]
                (if
                 (clojure.core/= nil rest__13959)
                 (.invoke
                  true-cont__13931
                  (apply and-ast patterns)
                  nil)
                 (thunk__13954
                  false-cont__13932
                  input__13930
                  true-cont__13931))))
              (clojure.core/fn
               []
               (thunk__13954
                false-cont__13932
                input__13930
                true-cont__13931))))
            (thunk__13954
             false-cont__13932
             input__13930
             true-cont__13931)))
          (thunk__13954
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__13954 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13954 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13966
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13961 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13961)
          (clojure.core/let
           [left__13962 (clojure.core/first left__13961)]
           (if
            (clojure.core/= 'leave left__13962)
            (clojure.core/let
             [left__13963 (clojure.core/next left__13961)]
             (if
              (clojure.core/not= nil left__13963)
              (clojure.core/let
               [left__13964 (clojure.core/first left__13963)]
               (clojure.core/let
                [form left__13964]
                (clojure.core/let
                 [left__13965 (clojure.core/next left__13963)]
                 (if
                  (clojure.core/= nil left__13965)
                  (.invoke true-cont__13931 (->Leave form) nil)
                  (thunk__13960
                   false-cont__13932
                   input__13930
                   true-cont__13931)))))
              (thunk__13960
               false-cont__13932
               input__13930
               true-cont__13931)))
            (thunk__13960
             false-cont__13932
             input__13930
             true-cont__13931)))
          (thunk__13960
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__13960 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13960 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13972
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13967 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13967)
          (clojure.core/let
           [left__13968 (clojure.core/first left__13967)]
           (if
            (clojure.core/= 'guard left__13968)
            (clojure.core/let
             [left__13969 (clojure.core/next left__13967)]
             (if
              (clojure.core/not= nil left__13969)
              (clojure.core/let
               [left__13970 (clojure.core/first left__13969)]
               (clojure.core/let
                [form left__13970]
                (clojure.core/let
                 [left__13971 (clojure.core/next left__13969)]
                 (if
                  (clojure.core/= nil left__13971)
                  (.invoke true-cont__13931 (->Guard form) nil)
                  (thunk__13966
                   false-cont__13932
                   input__13930
                   true-cont__13931)))))
              (thunk__13966
               false-cont__13932
               input__13930
               true-cont__13931)))
            (thunk__13966
             false-cont__13932
             input__13930
             true-cont__13931)))
          (thunk__13966
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__13966 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13966 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13978
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13973 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13973)
          (clojure.core/let
           [left__13974 (clojure.core/first left__13973)]
           (if
            (clojure.core/= 'quote left__13974)
            (clojure.core/let
             [left__13975 (clojure.core/next left__13973)]
             (if
              (clojure.core/not= nil left__13975)
              (clojure.core/let
               [left__13976 (clojure.core/first left__13975)]
               (clojure.core/let
                [quoted left__13976]
                (clojure.core/let
                 [left__13977 (clojure.core/next left__13975)]
                 (if
                  (clojure.core/= nil left__13977)
                  (.invoke
                   true-cont__13931
                   (literal-ast
                    (clojure.core/seq
                     (clojure.core/concat
                      (clojure.core/list 'quote)
                      (clojure.core/list quoted))))
                   nil)
                  (thunk__13972
                   false-cont__13932
                   input__13930
                   true-cont__13931)))))
              (thunk__13972
               false-cont__13932
               input__13930
               true-cont__13931)))
            (thunk__13972
             false-cont__13932
             input__13930
             true-cont__13931)))
          (thunk__13972
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__13972 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13972 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13984
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13979 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13979)
          (clojure.core/let
           [left__13980 (clojure.core/first left__13979)]
           (if
            (clojure.core/= 'prefix left__13980)
            (clojure.core/let
             [left__13981 (clojure.core/next left__13979)]
             ((.view-fn (zero-or-more seq-pattern))
              left__13981
              (clojure.core/fn
               [output__13982 rest__13983]
               (clojure.core/let
                [seq-patterns output__13982]
                (if
                 (clojure.core/= nil rest__13983)
                 (.invoke
                  true-cont__13931
                  (apply prefix-ast seq-patterns)
                  nil)
                 (thunk__13978
                  false-cont__13932
                  input__13930
                  true-cont__13931))))
              (clojure.core/fn
               []
               (thunk__13978
                false-cont__13932
                input__13930
                true-cont__13931))))
            (thunk__13978
             false-cont__13932
             input__13930
             true-cont__13931)))
          (thunk__13978
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__13978 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13978 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13988
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (vector? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13985 (clojure.core/seq input__13930)]
         ((.view-fn (zero-or-more seq-pattern))
          left__13985
          (clojure.core/fn
           [output__13986 rest__13987]
           (clojure.core/let
            [seq-patterns output__13986]
            (if
             (clojure.core/= nil rest__13987)
             (.invoke
              true-cont__13931
              (apply seqable-ast seq-patterns)
              nil)
             (thunk__13984
              false-cont__13932
              input__13930
              true-cont__13931))))
          (clojure.core/fn
           []
           (thunk__13984
            false-cont__13932
            input__13930
            true-cont__13931))))
        (thunk__13984 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13984 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13998
      [true-case-input__13991
       false-cont__13932
       input__13930
       true-cont__13931
       left__13989]
      (if
       (clojure.core/= nil true-case-input__13991)
       (clojure.core/let
        [left__13992 (clojure.core/next left__13989)]
        (if
         (clojure.core/not= nil left__13992)
         (clojure.core/let
          [left__13993 (clojure.core/first left__13992)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__13993)
            (clojure.core/nil? left__13993))
           (clojure.core/let
            [left__13994 (clojure.core/seq left__13993)]
            (if
             (clojure.core/not= nil left__13994)
             (clojure.core/let
              [left__13995 (clojure.core/first left__13994)]
              (clojure.core/let
               [arg left__13995]
               (clojure.core/let
                [left__13996 (clojure.core/next left__13994)]
                (if
                 (clojure.core/= nil left__13996)
                 (clojure.core/let
                  [left__13997 (clojure.core/next left__13992)]
                  (clojure.core/let
                   [body left__13997]
                   (.invoke
                    true-cont__13931
                    (predicate-ast
                     (clojure.core/seq
                      (clojure.core/concat
                       (clojure.core/list 'do)
                       (clojure.walk/prewalk-replace
                        {arg input-sym}
                        body))))
                    nil)))
                 (thunk__13988
                  false-cont__13932
                  input__13930
                  true-cont__13931)))))
             (thunk__13988
              false-cont__13932
              input__13930
              true-cont__13931)))
           (thunk__13988
            false-cont__13932
            input__13930
            true-cont__13931)))
         (thunk__13988
          false-cont__13932
          input__13930
          true-cont__13931)))
       (thunk__13988 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__13999
      [false-cont__13932
       input__13930
       left__13990
       true-cont__13931
       left__13989]
      (if
       (clojure.core/= 'fn* left__13990)
       (thunk__13998
        nil
        false-cont__13932
        input__13930
        true-cont__13931
        left__13989)
       (thunk__13988 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14000
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__13989 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__13989)
          (clojure.core/let
           [left__13990 (clojure.core/first left__13989)]
           (if
            (clojure.core/= 'fn left__13990)
            (thunk__13998
             nil
             false-cont__13932
             input__13930
             true-cont__13931
             left__13989)
            (thunk__13999
             false-cont__13932
             input__13930
             left__13990
             true-cont__13931
             left__13989)))
          (thunk__13988
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__13988 false-cont__13932 input__13930 true-cont__13931))
       (thunk__13988 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14001
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (predicate? input__13930)
       (clojure.core/let
        [predicate input__13930]
        (.invoke
         true-cont__13931
         (predicate-ast
          (clojure.core/seq
           (clojure.core/concat
            (clojure.core/list predicate)
            (clojure.core/list input-sym))))
         nil))
       (thunk__14000 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14002
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (clojure.core/instance? java.util.regex.Pattern input__13930)
       (clojure.core/let
        [regex input__13930]
        (.invoke true-cont__13931 (regex-ast regex) nil))
       (thunk__14001 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14008
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (seq? input__13930)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__13930)
         (clojure.core/nil? input__13930))
        (clojure.core/let
         [left__14003 (clojure.core/seq input__13930)]
         (if
          (clojure.core/not= nil left__14003)
          (clojure.core/let
           [left__14004 (clojure.core/first left__14003)]
           (if
            (constructor? left__14004)
            (clojure.core/let
             [constructor left__14004]
             (clojure.core/let
              [left__14005 (clojure.core/next left__14003)]
              ((.view-fn (zero-or-more pattern))
               left__14005
               (clojure.core/fn
                [output__14006 rest__14007]
                (clojure.core/let
                 [arg-patterns output__14006]
                 (if
                  (clojure.core/= nil rest__14007)
                  (.invoke
                   true-cont__13931
                   (constructor-ast
                    (constructor-name constructor)
                    arg-patterns)
                   nil)
                  (thunk__14002
                   false-cont__13932
                   input__13930
                   true-cont__13931))))
               (clojure.core/fn
                []
                (thunk__14002
                 false-cont__13932
                 input__13930
                 true-cont__13931)))))
            (thunk__14002
             false-cont__13932
             input__13930
             true-cont__13931)))
          (thunk__14002
           false-cont__13932
           input__13930
           true-cont__13931)))
        (thunk__14002 false-cont__13932 input__13930 true-cont__13931))
       (thunk__14002 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14013
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__13930)
        (clojure.core/nil? input__13930))
       (clojure.core/let
        [left__14010 (clojure.core/seq input__13930)]
        ((.view-fn (zero-or-more key&pattern))
         left__14010
         (clojure.core/fn
          [output__14011 rest__14012]
          (clojure.core/let
           [keys&patterns output__14011]
           (if
            (clojure.core/= nil rest__14012)
            (.invoke true-cont__13931 (map-ast keys&patterns) nil)
            (thunk__14008
             false-cont__13932
             input__13930
             true-cont__13931))))
         (clojure.core/fn
          []
          (thunk__14008
           false-cont__13932
           input__13930
           true-cont__13931))))
       (thunk__14008 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14014
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentHashMap
        input__13930)
       (thunk__14013 false-cont__13932 input__13930 true-cont__13931)
       (thunk__14008 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14015
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentArrayMap
        input__13930)
       (thunk__14013 false-cont__13932 input__13930 true-cont__13931)
       (thunk__14014 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14016
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (class-name? input__13930)
       (clojure.core/let
        [class-name input__13930]
        (.invoke true-cont__13931 (class-ast class-name) nil))
       (thunk__14015 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14017
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (primitive? input__13930)
       (clojure.core/let
        [primitive input__13930]
        (.invoke true-cont__13931 (literal-ast primitive) nil))
       (thunk__14016 false-cont__13932 input__13930 true-cont__13931)))
     (thunk__14018
      [false-cont__13932 input__13930 true-cont__13931]
      (if
       (binding? input__13930)
       (clojure.core/let
        [binding input__13930]
        (.invoke true-cont__13931 (->Bind (binding-name binding)) nil))
       (thunk__14017
        false-cont__13932
        input__13930
        true-cont__13931)))]
    (strucjure/->View
     (clojure.core/fn
      [input__13930 true-cont__13931 false-cont__13932]
      (if
       (clojure.core/= '_ input__13930)
       (.invoke true-cont__13931 (->Leave nil) nil)
       (thunk__14018
        false-cont__13932
        input__13930
        true-cont__13931))))))
 (def
  seq-pattern
  '(clojure.core/letfn
    [(thunk__14025
      [false-cont__14021 true-cont__14020 input__14019]
      ((.view-fn pattern)
       input__14019
       (clojure.core/fn
        [output__14023 rest__14024]
        (clojure.core/let
         [pattern output__14023]
         (.invoke true-cont__14020 (head-ast pattern) rest__14024)))
       (clojure.core/fn [] (.invoke false-cont__14021))))
     (thunk__14031
      [false-cont__14021 true-cont__14020 input__14019]
      (if
       (seq? input__14019)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__14019)
         (clojure.core/nil? input__14019))
        (clojure.core/let
         [left__14026 (clojure.core/seq input__14019)]
         (if
          (clojure.core/not= nil left__14026)
          (clojure.core/let
           [left__14027 (clojure.core/first left__14026)]
           (if
            (clojure.core/= 'guard left__14027)
            (clojure.core/let
             [left__14028 (clojure.core/next left__14026)]
             (if
              (clojure.core/not= nil left__14028)
              (clojure.core/let
               [left__14029 (clojure.core/first left__14028)]
               (clojure.core/let
                [form left__14029]
                (clojure.core/let
                 [left__14030 (clojure.core/next left__14028)]
                 (if
                  (clojure.core/= nil left__14030)
                  (.invoke true-cont__14020 (->Guard form) nil)
                  (thunk__14025
                   false-cont__14021
                   true-cont__14020
                   input__14019)))))
              (thunk__14025
               false-cont__14021
               true-cont__14020
               input__14019)))
            (thunk__14025
             false-cont__14021
             true-cont__14020
             input__14019)))
          (thunk__14025
           false-cont__14021
           true-cont__14020
           input__14019)))
        (thunk__14025 false-cont__14021 true-cont__14020 input__14019))
       (thunk__14025
        false-cont__14021
        true-cont__14020
        input__14019)))]
    (strucjure/->View
     (clojure.core/fn
      [input__14019 true-cont__14020 false-cont__14021]
      (if
       (seq? input__14019)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__14019)
         (clojure.core/nil? input__14019))
        (clojure.core/let
         [left__14032 (clojure.core/seq input__14019)]
         (if
          (clojure.core/not= nil left__14032)
          (clojure.core/let
           [left__14033 (clojure.core/first left__14032)]
           (if
            (clojure.core/= '& left__14033)
            (clojure.core/let
             [left__14034 (clojure.core/next left__14032)]
             (if
              (clojure.core/not= nil left__14034)
              (clojure.core/let
               [left__14035 (clojure.core/first left__14034)]
               ((.view-fn pattern)
                left__14035
                (clojure.core/fn
                 [output__14036 rest__14037]
                 (clojure.core/let
                  [pattern output__14036]
                  (if
                   (clojure.core/= nil rest__14037)
                   (clojure.core/let
                    [left__14038 (clojure.core/next left__14034)]
                    (if
                     (clojure.core/= nil left__14038)
                     (.invoke true-cont__14020 pattern nil)
                     (thunk__14031
                      false-cont__14021
                      true-cont__14020
                      input__14019)))
                   (thunk__14031
                    false-cont__14021
                    true-cont__14020
                    input__14019))))
                (clojure.core/fn
                 []
                 (thunk__14031
                  false-cont__14021
                  true-cont__14020
                  input__14019))))
              (thunk__14031
               false-cont__14021
               true-cont__14020
               input__14019)))
            (thunk__14031
             false-cont__14021
             true-cont__14020
             input__14019)))
          (thunk__14031
           false-cont__14021
           true-cont__14020
           input__14019)))
        (thunk__14031 false-cont__14021 true-cont__14020 input__14019))
       (thunk__14031
        false-cont__14021
        true-cont__14020
        input__14019)))))))
