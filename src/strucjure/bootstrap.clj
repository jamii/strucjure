(ns strucjure.bootstrap)
(do
 (def
  optional
  '(clojure.core/letfn
    [(thunk__18250
      [input__18245 false-cont__18247 true-cont__18246]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__18245)
        (clojure.core/nil? input__18245))
       (clojure.core/let
        [left__18249 (clojure.core/seq input__18245)]
        (.invoke true-cont__18246 nil left__18249))
       (.invoke false-cont__18247)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__18245 true-cont__18246 false-cont__18247]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18245)
         (clojure.core/nil? input__18245))
        (clojure.core/let
         [left__18251 (clojure.core/seq input__18245)]
         (if
          (clojure.core/not= nil left__18251)
          (clojure.core/let
           [left__18252 (clojure.core/first left__18251)]
           ((.view-fn elem)
            left__18252
            (clojure.core/fn
             [output__18253 rest__18254]
             (clojure.core/let
              [x output__18253]
              (if
               (clojure.core/= nil rest__18254)
               (clojure.core/let
                [left__18255 (clojure.core/next left__18251)]
                (.invoke true-cont__18246 x left__18255))
               (thunk__18250
                input__18245
                false-cont__18247
                true-cont__18246))))
            (clojure.core/fn
             []
             (thunk__18250
              input__18245
              false-cont__18247
              true-cont__18246))))
          (thunk__18250
           input__18245
           false-cont__18247
           true-cont__18246)))
        (thunk__18250
         input__18245
         false-cont__18247
         true-cont__18246)))))))
 (def
  zero-or-more
  '(clojure.core/letfn
    [(thunk__18261
      [input__18256 true-cont__18257 false-cont__18258]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__18256)
        (clojure.core/nil? input__18256))
       (clojure.core/let
        [left__18260 (clojure.core/seq input__18256)]
        (.invoke true-cont__18257 nil left__18260))
       (.invoke false-cont__18258)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__18256 true-cont__18257 false-cont__18258]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18256)
         (clojure.core/nil? input__18256))
        (clojure.core/let
         [left__18262 (clojure.core/seq input__18256)]
         (if
          (clojure.core/not= nil left__18262)
          (clojure.core/let
           [left__18263 (clojure.core/first left__18262)]
           ((.view-fn elem)
            left__18263
            (clojure.core/fn
             [output__18264 rest__18265]
             (clojure.core/let
              [x output__18264]
              (if
               (clojure.core/= nil rest__18265)
               (clojure.core/let
                [left__18266 (clojure.core/next left__18262)]
                ((.view-fn (zero-or-more elem))
                 left__18266
                 (clojure.core/fn
                  [output__18267 rest__18268]
                  (clojure.core/let
                   [xs output__18267]
                   (.invoke true-cont__18257 (cons x xs) rest__18268)))
                 (clojure.core/fn
                  []
                  (thunk__18261
                   input__18256
                   true-cont__18257
                   false-cont__18258))))
               (thunk__18261
                input__18256
                true-cont__18257
                false-cont__18258))))
            (clojure.core/fn
             []
             (thunk__18261
              input__18256
              true-cont__18257
              false-cont__18258))))
          (thunk__18261
           input__18256
           true-cont__18257
           false-cont__18258)))
        (thunk__18261
         input__18256
         true-cont__18257
         false-cont__18258)))))))
 (def
  one-or-more
  '(clojure.core/letfn
    []
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__18269 true-cont__18270 false-cont__18271]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18269)
         (clojure.core/nil? input__18269))
        (clojure.core/let
         [left__18273 (clojure.core/seq input__18269)]
         (if
          (clojure.core/not= nil left__18273)
          (clojure.core/let
           [left__18274 (clojure.core/first left__18273)]
           ((.view-fn elem)
            left__18274
            (clojure.core/fn
             [output__18275 rest__18276]
             (clojure.core/let
              [x output__18275]
              (if
               (clojure.core/= nil rest__18276)
               (clojure.core/let
                [left__18277 (clojure.core/next left__18273)]
                ((.view-fn (zero-or-more elem))
                 left__18277
                 (clojure.core/fn
                  [output__18278 rest__18279]
                  (clojure.core/let
                   [xs output__18278]
                   (.invoke true-cont__18270 (cons x xs) rest__18279)))
                 (clojure.core/fn [] (.invoke false-cont__18271))))
               (.invoke false-cont__18271))))
            (clojure.core/fn [] (.invoke false-cont__18271))))
          (.invoke false-cont__18271)))
        (.invoke false-cont__18271)))))))
 (def
  key&pattern
  '(clojure.core/letfn
    []
    (strucjure/->View
     (clojure.core/fn
      [input__18280 true-cont__18281 false-cont__18282]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__18280)
        (clojure.core/nil? input__18280))
       (clojure.core/let
        [left__18284 (clojure.core/seq input__18280)]
        (if
         (clojure.core/not= nil left__18284)
         (clojure.core/let
          [left__18285 (clojure.core/first left__18284)]
          (clojure.core/let
           [key left__18285]
           (clojure.core/let
            [left__18286 (clojure.core/next left__18284)]
            (if
             (clojure.core/not= nil left__18286)
             (clojure.core/let
              [left__18287 (clojure.core/first left__18286)]
              ((.view-fn pattern)
               left__18287
               (clojure.core/fn
                [output__18288 rest__18289]
                (clojure.core/let
                 [pattern output__18288]
                 (if
                  (clojure.core/= nil rest__18289)
                  (clojure.core/let
                   [left__18290 (clojure.core/next left__18286)]
                   (if
                    (clojure.core/= nil left__18290)
                    (.invoke true-cont__18281 [key pattern] nil)
                    (.invoke false-cont__18282)))
                  (.invoke false-cont__18282))))
               (clojure.core/fn [] (.invoke false-cont__18282))))
             (.invoke false-cont__18282)))))
         (.invoke false-cont__18282)))
       (.invoke false-cont__18282))))))
 (def
  pattern
  '(clojure.core/letfn
    [(thunk__18302
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18295 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18295)
          (clojure.core/let
           [left__18296 (clojure.core/first left__18295)]
           (clojure.core/let
            [view left__18296]
            (clojure.core/let
             [left__18297 (clojure.core/next left__18295)]
             (if
              (clojure.core/not= nil left__18297)
              (clojure.core/let
               [left__18298 (clojure.core/first left__18297)]
               ((.view-fn pattern)
                left__18298
                (clojure.core/fn
                 [output__18299 rest__18300]
                 (clojure.core/let
                  [pattern output__18299]
                  (if
                   (clojure.core/= nil rest__18300)
                   (clojure.core/let
                    [left__18301 (clojure.core/next left__18297)]
                    (if
                     (clojure.core/= nil left__18301)
                     (.invoke
                      true-cont__18292
                      (import-ast view pattern)
                      nil)
                     (.invoke false-cont__18293)))
                   (.invoke false-cont__18293))))
                (clojure.core/fn [] (.invoke false-cont__18293))))
              (.invoke false-cont__18293)))))
          (.invoke false-cont__18293)))
        (.invoke false-cont__18293))
       (.invoke false-cont__18293)))
     (thunk__18303
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (symbol? input__18291)
       (clojure.core/let
        [variable input__18291]
        (.invoke true-cont__18292 (literal-ast variable) nil))
       (thunk__18302 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18311
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18304 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18304)
          (clojure.core/let
           [left__18305 (clojure.core/first left__18304)]
           (if
            (clojure.core/= 'not left__18305)
            (clojure.core/let
             [left__18306 (clojure.core/next left__18304)]
             (if
              (clojure.core/not= nil left__18306)
              (clojure.core/let
               [left__18307 (clojure.core/first left__18306)]
               ((.view-fn pattern)
                left__18307
                (clojure.core/fn
                 [output__18308 rest__18309]
                 (clojure.core/let
                  [pattern output__18308]
                  (if
                   (clojure.core/= nil rest__18309)
                   (clojure.core/let
                    [left__18310 (clojure.core/next left__18306)]
                    (if
                     (clojure.core/= nil left__18310)
                     (.invoke true-cont__18292 (->Not pattern) nil)
                     (thunk__18303
                      false-cont__18293
                      true-cont__18292
                      input__18291)))
                   (thunk__18303
                    false-cont__18293
                    true-cont__18292
                    input__18291))))
                (clojure.core/fn
                 []
                 (thunk__18303
                  false-cont__18293
                  true-cont__18292
                  input__18291))))
              (thunk__18303
               false-cont__18293
               true-cont__18292
               input__18291)))
            (thunk__18303
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18303
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18303 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18303 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18317
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18312 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18312)
          (clojure.core/let
           [left__18313 (clojure.core/first left__18312)]
           (if
            (clojure.core/= 'or left__18313)
            (clojure.core/let
             [left__18314 (clojure.core/next left__18312)]
             ((.view-fn (one-or-more pattern))
              left__18314
              (clojure.core/fn
               [output__18315 rest__18316]
               (clojure.core/let
                [patterns output__18315]
                (if
                 (clojure.core/= nil rest__18316)
                 (.invoke true-cont__18292 (apply or-ast patterns) nil)
                 (thunk__18311
                  false-cont__18293
                  true-cont__18292
                  input__18291))))
              (clojure.core/fn
               []
               (thunk__18311
                false-cont__18293
                true-cont__18292
                input__18291))))
            (thunk__18311
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18311
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18311 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18311 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18323
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18318 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18318)
          (clojure.core/let
           [left__18319 (clojure.core/first left__18318)]
           (if
            (clojure.core/= 'seq left__18319)
            (clojure.core/let
             [left__18320 (clojure.core/next left__18318)]
             ((.view-fn (one-or-more pattern))
              left__18320
              (clojure.core/fn
               [output__18321 rest__18322]
               (clojure.core/let
                [patterns output__18321]
                (if
                 (clojure.core/= nil rest__18322)
                 (.invoke
                  true-cont__18292
                  (apply seq-ast patterns)
                  nil)
                 (thunk__18317
                  false-cont__18293
                  true-cont__18292
                  input__18291))))
              (clojure.core/fn
               []
               (thunk__18317
                false-cont__18293
                true-cont__18292
                input__18291))))
            (thunk__18317
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18317
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18317 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18317 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18329
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18324 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18324)
          (clojure.core/let
           [left__18325 (clojure.core/first left__18324)]
           (if
            (clojure.core/= 'and left__18325)
            (clojure.core/let
             [left__18326 (clojure.core/next left__18324)]
             ((.view-fn (one-or-more pattern))
              left__18326
              (clojure.core/fn
               [output__18327 rest__18328]
               (clojure.core/let
                [patterns output__18327]
                (if
                 (clojure.core/= nil rest__18328)
                 (.invoke
                  true-cont__18292
                  (apply and-ast patterns)
                  nil)
                 (thunk__18323
                  false-cont__18293
                  true-cont__18292
                  input__18291))))
              (clojure.core/fn
               []
               (thunk__18323
                false-cont__18293
                true-cont__18292
                input__18291))))
            (thunk__18323
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18323
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18323 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18323 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18335
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18330 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18330)
          (clojure.core/let
           [left__18331 (clojure.core/first left__18330)]
           (if
            (clojure.core/= 'leave left__18331)
            (clojure.core/let
             [left__18332 (clojure.core/next left__18330)]
             (if
              (clojure.core/not= nil left__18332)
              (clojure.core/let
               [left__18333 (clojure.core/first left__18332)]
               (clojure.core/let
                [form left__18333]
                (clojure.core/let
                 [left__18334 (clojure.core/next left__18332)]
                 (if
                  (clojure.core/= nil left__18334)
                  (.invoke true-cont__18292 (->Leave form) nil)
                  (thunk__18329
                   false-cont__18293
                   true-cont__18292
                   input__18291)))))
              (thunk__18329
               false-cont__18293
               true-cont__18292
               input__18291)))
            (thunk__18329
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18329
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18329 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18329 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18341
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18336 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18336)
          (clojure.core/let
           [left__18337 (clojure.core/first left__18336)]
           (if
            (clojure.core/= 'guard left__18337)
            (clojure.core/let
             [left__18338 (clojure.core/next left__18336)]
             (if
              (clojure.core/not= nil left__18338)
              (clojure.core/let
               [left__18339 (clojure.core/first left__18338)]
               (clojure.core/let
                [form left__18339]
                (clojure.core/let
                 [left__18340 (clojure.core/next left__18338)]
                 (if
                  (clojure.core/= nil left__18340)
                  (.invoke true-cont__18292 (->Guard form) nil)
                  (thunk__18335
                   false-cont__18293
                   true-cont__18292
                   input__18291)))))
              (thunk__18335
               false-cont__18293
               true-cont__18292
               input__18291)))
            (thunk__18335
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18335
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18335 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18335 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18347
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18342 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18342)
          (clojure.core/let
           [left__18343 (clojure.core/first left__18342)]
           (if
            (clojure.core/= 'quote left__18343)
            (clojure.core/let
             [left__18344 (clojure.core/next left__18342)]
             (if
              (clojure.core/not= nil left__18344)
              (clojure.core/let
               [left__18345 (clojure.core/first left__18344)]
               (clojure.core/let
                [quoted left__18345]
                (clojure.core/let
                 [left__18346 (clojure.core/next left__18344)]
                 (if
                  (clojure.core/= nil left__18346)
                  (.invoke
                   true-cont__18292
                   (literal-ast
                    (clojure.core/seq
                     (clojure.core/concat
                      (clojure.core/list 'quote)
                      (clojure.core/list quoted))))
                   nil)
                  (thunk__18341
                   false-cont__18293
                   true-cont__18292
                   input__18291)))))
              (thunk__18341
               false-cont__18293
               true-cont__18292
               input__18291)))
            (thunk__18341
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18341
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18341 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18341 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18353
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18348 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18348)
          (clojure.core/let
           [left__18349 (clojure.core/first left__18348)]
           (if
            (clojure.core/= 'prefix left__18349)
            (clojure.core/let
             [left__18350 (clojure.core/next left__18348)]
             ((.view-fn (zero-or-more seq-pattern))
              left__18350
              (clojure.core/fn
               [output__18351 rest__18352]
               (clojure.core/let
                [seq-patterns output__18351]
                (if
                 (clojure.core/= nil rest__18352)
                 (.invoke
                  true-cont__18292
                  (apply prefix-ast seq-patterns)
                  nil)
                 (thunk__18347
                  false-cont__18293
                  true-cont__18292
                  input__18291))))
              (clojure.core/fn
               []
               (thunk__18347
                false-cont__18293
                true-cont__18292
                input__18291))))
            (thunk__18347
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18347
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18347 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18347 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18357
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (vector? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18354 (clojure.core/seq input__18291)]
         ((.view-fn (zero-or-more seq-pattern))
          left__18354
          (clojure.core/fn
           [output__18355 rest__18356]
           (clojure.core/let
            [seq-patterns output__18355]
            (if
             (clojure.core/= nil rest__18356)
             (.invoke
              true-cont__18292
              (apply seqable-ast seq-patterns)
              nil)
             (thunk__18353
              false-cont__18293
              true-cont__18292
              input__18291))))
          (clojure.core/fn
           []
           (thunk__18353
            false-cont__18293
            true-cont__18292
            input__18291))))
        (thunk__18353 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18353 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18367
      [false-cont__18293
       left__18358
       true-cont__18292
       input__18291
       true-case-input__18360]
      (if
       (clojure.core/= nil true-case-input__18360)
       (clojure.core/let
        [left__18361 (clojure.core/next left__18358)]
        (if
         (clojure.core/not= nil left__18361)
         (clojure.core/let
          [left__18362 (clojure.core/first left__18361)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__18362)
            (clojure.core/nil? left__18362))
           (clojure.core/let
            [left__18363 (clojure.core/seq left__18362)]
            (if
             (clojure.core/not= nil left__18363)
             (clojure.core/let
              [left__18364 (clojure.core/first left__18363)]
              (clojure.core/let
               [arg left__18364]
               (clojure.core/let
                [left__18365 (clojure.core/next left__18363)]
                (if
                 (clojure.core/= nil left__18365)
                 (clojure.core/let
                  [left__18366 (clojure.core/next left__18361)]
                  (clojure.core/let
                   [body left__18366]
                   (.invoke
                    true-cont__18292
                    (predicate-ast
                     (clojure.core/seq
                      (clojure.core/concat
                       (clojure.core/list 'do)
                       (clojure.walk/prewalk-replace
                        {arg input-sym}
                        body))))
                    nil)))
                 (thunk__18357
                  false-cont__18293
                  true-cont__18292
                  input__18291)))))
             (thunk__18357
              false-cont__18293
              true-cont__18292
              input__18291)))
           (thunk__18357
            false-cont__18293
            true-cont__18292
            input__18291)))
         (thunk__18357
          false-cont__18293
          true-cont__18292
          input__18291)))
       (thunk__18357 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18368
      [false-cont__18293
       left__18359
       left__18358
       true-cont__18292
       input__18291]
      (if
       (clojure.core/= 'fn* left__18359)
       (thunk__18367
        false-cont__18293
        left__18358
        true-cont__18292
        input__18291
        nil)
       (thunk__18357 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18369
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18358 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18358)
          (clojure.core/let
           [left__18359 (clojure.core/first left__18358)]
           (if
            (clojure.core/= 'fn left__18359)
            (thunk__18367
             false-cont__18293
             left__18358
             true-cont__18292
             input__18291
             nil)
            (thunk__18368
             false-cont__18293
             left__18359
             left__18358
             true-cont__18292
             input__18291)))
          (thunk__18357
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18357 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18357 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18370
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (predicate? input__18291)
       (clojure.core/let
        [predicate input__18291]
        (.invoke
         true-cont__18292
         (predicate-ast
          (clojure.core/seq
           (clojure.core/concat
            (clojure.core/list predicate)
            (clojure.core/list input-sym))))
         nil))
       (thunk__18369 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18371
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (clojure.core/instance? java.util.regex.Pattern input__18291)
       (clojure.core/let
        [regex input__18291]
        (.invoke true-cont__18292 (regex-ast regex) nil))
       (thunk__18370 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18377
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (seq? input__18291)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18291)
         (clojure.core/nil? input__18291))
        (clojure.core/let
         [left__18372 (clojure.core/seq input__18291)]
         (if
          (clojure.core/not= nil left__18372)
          (clojure.core/let
           [left__18373 (clojure.core/first left__18372)]
           (if
            (constructor? left__18373)
            (clojure.core/let
             [constructor left__18373]
             (clojure.core/let
              [left__18374 (clojure.core/next left__18372)]
              ((.view-fn (zero-or-more pattern))
               left__18374
               (clojure.core/fn
                [output__18375 rest__18376]
                (clojure.core/let
                 [arg-patterns output__18375]
                 (if
                  (clojure.core/= nil rest__18376)
                  (.invoke
                   true-cont__18292
                   (constructor-ast
                    (constructor-name constructor)
                    arg-patterns)
                   nil)
                  (thunk__18371
                   false-cont__18293
                   true-cont__18292
                   input__18291))))
               (clojure.core/fn
                []
                (thunk__18371
                 false-cont__18293
                 true-cont__18292
                 input__18291)))))
            (thunk__18371
             false-cont__18293
             true-cont__18292
             input__18291)))
          (thunk__18371
           false-cont__18293
           true-cont__18292
           input__18291)))
        (thunk__18371 false-cont__18293 true-cont__18292 input__18291))
       (thunk__18371 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18382
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__18291)
        (clojure.core/nil? input__18291))
       (clojure.core/let
        [left__18379 (clojure.core/seq input__18291)]
        ((.view-fn (zero-or-more key&pattern))
         left__18379
         (clojure.core/fn
          [output__18380 rest__18381]
          (clojure.core/let
           [keys&patterns output__18380]
           (if
            (clojure.core/= nil rest__18381)
            (.invoke true-cont__18292 (map-ast keys&patterns) nil)
            (thunk__18377
             false-cont__18293
             true-cont__18292
             input__18291))))
         (clojure.core/fn
          []
          (thunk__18377
           false-cont__18293
           true-cont__18292
           input__18291))))
       (thunk__18377 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18383
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentHashMap
        input__18291)
       (thunk__18382 false-cont__18293 true-cont__18292 input__18291)
       (thunk__18377 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18384
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentArrayMap
        input__18291)
       (thunk__18382 false-cont__18293 true-cont__18292 input__18291)
       (thunk__18383 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18385
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (class-name? input__18291)
       (clojure.core/let
        [class-name input__18291]
        (.invoke true-cont__18292 (class-ast class-name) nil))
       (thunk__18384 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18386
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (primitive? input__18291)
       (clojure.core/let
        [primitive input__18291]
        (.invoke true-cont__18292 (literal-ast primitive) nil))
       (thunk__18385 false-cont__18293 true-cont__18292 input__18291)))
     (thunk__18387
      [false-cont__18293 true-cont__18292 input__18291]
      (if
       (binding? input__18291)
       (clojure.core/let
        [binding input__18291]
        (.invoke true-cont__18292 (->Bind (binding-name binding)) nil))
       (thunk__18386
        false-cont__18293
        true-cont__18292
        input__18291)))]
    (strucjure/->View
     (clojure.core/fn
      [input__18291 true-cont__18292 false-cont__18293]
      (if
       (clojure.core/= '_ input__18291)
       (.invoke true-cont__18292 (->Leave nil) nil)
       (thunk__18387
        false-cont__18293
        true-cont__18292
        input__18291))))))
 (def
  seq-pattern
  '(clojure.core/letfn
    [(thunk__18394
      [true-cont__18389 input__18388 false-cont__18390]
      ((.view-fn pattern)
       input__18388
       (clojure.core/fn
        [output__18392 rest__18393]
        (clojure.core/let
         [pattern output__18392]
         (.invoke true-cont__18389 (head-ast pattern) rest__18393)))
       (clojure.core/fn [] (.invoke false-cont__18390))))
     (thunk__18400
      [true-cont__18389 input__18388 false-cont__18390]
      (if
       (seq? input__18388)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18388)
         (clojure.core/nil? input__18388))
        (clojure.core/let
         [left__18395 (clojure.core/seq input__18388)]
         (if
          (clojure.core/not= nil left__18395)
          (clojure.core/let
           [left__18396 (clojure.core/first left__18395)]
           (if
            (clojure.core/= 'guard left__18396)
            (clojure.core/let
             [left__18397 (clojure.core/next left__18395)]
             (if
              (clojure.core/not= nil left__18397)
              (clojure.core/let
               [left__18398 (clojure.core/first left__18397)]
               (clojure.core/let
                [form left__18398]
                (clojure.core/let
                 [left__18399 (clojure.core/next left__18397)]
                 (if
                  (clojure.core/= nil left__18399)
                  (.invoke true-cont__18389 (->Guard form) nil)
                  (thunk__18394
                   true-cont__18389
                   input__18388
                   false-cont__18390)))))
              (thunk__18394
               true-cont__18389
               input__18388
               false-cont__18390)))
            (thunk__18394
             true-cont__18389
             input__18388
             false-cont__18390)))
          (thunk__18394
           true-cont__18389
           input__18388
           false-cont__18390)))
        (thunk__18394 true-cont__18389 input__18388 false-cont__18390))
       (thunk__18394
        true-cont__18389
        input__18388
        false-cont__18390)))]
    (strucjure/->View
     (clojure.core/fn
      [input__18388 true-cont__18389 false-cont__18390]
      (if
       (seq? input__18388)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__18388)
         (clojure.core/nil? input__18388))
        (clojure.core/let
         [left__18401 (clojure.core/seq input__18388)]
         (if
          (clojure.core/not= nil left__18401)
          (clojure.core/let
           [left__18402 (clojure.core/first left__18401)]
           (if
            (clojure.core/= '& left__18402)
            (clojure.core/let
             [left__18403 (clojure.core/next left__18401)]
             (if
              (clojure.core/not= nil left__18403)
              (clojure.core/let
               [left__18404 (clojure.core/first left__18403)]
               ((.view-fn pattern)
                left__18404
                (clojure.core/fn
                 [output__18405 rest__18406]
                 (clojure.core/let
                  [pattern output__18405]
                  (if
                   (clojure.core/= nil rest__18406)
                   (clojure.core/let
                    [left__18407 (clojure.core/next left__18403)]
                    (if
                     (clojure.core/= nil left__18407)
                     (.invoke true-cont__18389 pattern nil)
                     (thunk__18400
                      true-cont__18389
                      input__18388
                      false-cont__18390)))
                   (thunk__18400
                    true-cont__18389
                    input__18388
                    false-cont__18390))))
                (clojure.core/fn
                 []
                 (thunk__18400
                  true-cont__18389
                  input__18388
                  false-cont__18390))))
              (thunk__18400
               true-cont__18389
               input__18388
               false-cont__18390)))
            (thunk__18400
             true-cont__18389
             input__18388
             false-cont__18390)))
          (thunk__18400
           true-cont__18389
           input__18388
           false-cont__18390)))
        (thunk__18400 true-cont__18389 input__18388 false-cont__18390))
       (thunk__18400
        true-cont__18389
        input__18388
        false-cont__18390)))))))
