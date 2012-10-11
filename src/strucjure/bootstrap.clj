(ns strucjure.bootstrap)
(do
 (def
  optional
  '(clojure.core/letfn
    [(thunk__6256
      [true-cont__6252 false-cont__6253 input__6251]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__6251)
        (clojure.core/nil? input__6251))
       (clojure.core/let
        [left__6255 (clojure.core/seq input__6251)]
        (clojure.core/let
         [rest left__6255]
         (clojure.core/let
          [output__6254 nil]
          (.invoke true-cont__6252 output__6254 rest))))
       (.invoke false-cont__6253)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__6251 true-cont__6252 false-cont__6253]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6251)
         (clojure.core/nil? input__6251))
        (clojure.core/let
         [left__6257 (clojure.core/seq input__6251)]
         (if
          (clojure.core/not= nil left__6257)
          (clojure.core/let
           [left__6258 (clojure.core/first left__6257)]
           ((.view-fn elem)
            left__6258
            (clojure.core/fn
             [output__6259 rest__6260]
             (clojure.core/let
              [x output__6259]
              (if
               (clojure.core/= nil rest__6260)
               (clojure.core/let
                [left__6261 (clojure.core/next left__6257)]
                (clojure.core/let
                 [rest left__6261]
                 (clojure.core/let
                  [output__6254 x]
                  (.invoke true-cont__6252 output__6254 rest))))
               (thunk__6256
                true-cont__6252
                false-cont__6253
                input__6251))))
            (clojure.core/fn
             []
             (thunk__6256
              true-cont__6252
              false-cont__6253
              input__6251))))
          (thunk__6256 true-cont__6252 false-cont__6253 input__6251)))
        (thunk__6256
         true-cont__6252
         false-cont__6253
         input__6251)))))))
 (def
  zero-or-more
  '(clojure.core/letfn
    [(thunk__6267
      [false-cont__6264 input__6262 true-cont__6263]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__6262)
        (clojure.core/nil? input__6262))
       (clojure.core/let
        [left__6266 (clojure.core/seq input__6262)]
        (clojure.core/let
         [rest left__6266]
         (clojure.core/let
          [output__6265 nil]
          (.invoke true-cont__6263 output__6265 rest))))
       (.invoke false-cont__6264)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__6262 true-cont__6263 false-cont__6264]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6262)
         (clojure.core/nil? input__6262))
        (clojure.core/let
         [left__6268 (clojure.core/seq input__6262)]
         (if
          (clojure.core/not= nil left__6268)
          (clojure.core/let
           [left__6269 (clojure.core/first left__6268)]
           ((.view-fn elem)
            left__6269
            (clojure.core/fn
             [output__6270 rest__6271]
             (clojure.core/let
              [x output__6270]
              (if
               (clojure.core/= nil rest__6271)
               (clojure.core/let
                [left__6272 (clojure.core/next left__6268)]
                ((.view-fn (zero-or-more elem))
                 left__6272
                 (clojure.core/fn
                  [output__6273 rest__6274]
                  (clojure.core/let
                   [xs output__6273]
                   (clojure.core/let
                    [rest rest__6274]
                    (clojure.core/let
                     [output__6265 (cons x xs)]
                     (.invoke true-cont__6263 output__6265 rest)))))
                 (clojure.core/fn
                  []
                  (thunk__6267
                   false-cont__6264
                   input__6262
                   true-cont__6263))))
               (thunk__6267
                false-cont__6264
                input__6262
                true-cont__6263))))
            (clojure.core/fn
             []
             (thunk__6267
              false-cont__6264
              input__6262
              true-cont__6263))))
          (thunk__6267 false-cont__6264 input__6262 true-cont__6263)))
        (thunk__6267
         false-cont__6264
         input__6262
         true-cont__6263)))))))
 (def
  one-or-more
  '(clojure.core/letfn
    []
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__6275 true-cont__6276 false-cont__6277]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6275)
         (clojure.core/nil? input__6275))
        (clojure.core/let
         [left__6279 (clojure.core/seq input__6275)]
         (if
          (clojure.core/not= nil left__6279)
          (clojure.core/let
           [left__6280 (clojure.core/first left__6279)]
           ((.view-fn elem)
            left__6280
            (clojure.core/fn
             [output__6281 rest__6282]
             (clojure.core/let
              [x output__6281]
              (if
               (clojure.core/= nil rest__6282)
               (clojure.core/let
                [left__6283 (clojure.core/next left__6279)]
                ((.view-fn (zero-or-more elem))
                 left__6283
                 (clojure.core/fn
                  [output__6284 rest__6285]
                  (clojure.core/let
                   [xs output__6284]
                   (clojure.core/let
                    [rest rest__6285]
                    (clojure.core/let
                     [output__6278 (cons x xs)]
                     (.invoke true-cont__6276 output__6278 rest)))))
                 (clojure.core/fn [] (.invoke false-cont__6277))))
               (.invoke false-cont__6277))))
            (clojure.core/fn [] (.invoke false-cont__6277))))
          (.invoke false-cont__6277)))
        (.invoke false-cont__6277)))))))
 (def
  key&pattern
  '(clojure.core/letfn
    []
    (strucjure/->View
     (clojure.core/fn
      [input__6286 true-cont__6287 false-cont__6288]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__6286)
        (clojure.core/nil? input__6286))
       (clojure.core/let
        [left__6290 (clojure.core/seq input__6286)]
        (if
         (clojure.core/not= nil left__6290)
         (clojure.core/let
          [left__6291 (clojure.core/first left__6290)]
          (clojure.core/let
           [key left__6291]
           (clojure.core/let
            [left__6292 (clojure.core/next left__6290)]
            (if
             (clojure.core/not= nil left__6292)
             (clojure.core/let
              [left__6293 (clojure.core/first left__6292)]
              ((.view-fn pattern)
               left__6293
               (clojure.core/fn
                [output__6294 rest__6295]
                (clojure.core/let
                 [pattern output__6294]
                 (if
                  (clojure.core/= nil rest__6295)
                  (clojure.core/let
                   [left__6296 (clojure.core/next left__6292)]
                   (if
                    (clojure.core/= nil left__6296)
                    (clojure.core/let
                     [output__6289 [key pattern]]
                     (.invoke true-cont__6287 output__6289 nil))
                    (.invoke false-cont__6288)))
                  (.invoke false-cont__6288))))
               (clojure.core/fn [] (.invoke false-cont__6288))))
             (.invoke false-cont__6288)))))
         (.invoke false-cont__6288)))
       (.invoke false-cont__6288))))))
 (def
  pattern
  '(clojure.core/letfn
    [(thunk__6308
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6301 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6301)
          (clojure.core/let
           [left__6302 (clojure.core/first left__6301)]
           (clojure.core/let
            [view left__6302]
            (clojure.core/let
             [left__6303 (clojure.core/next left__6301)]
             (if
              (clojure.core/not= nil left__6303)
              (clojure.core/let
               [left__6304 (clojure.core/first left__6303)]
               ((.view-fn pattern)
                left__6304
                (clojure.core/fn
                 [output__6305 rest__6306]
                 (clojure.core/let
                  [pattern output__6305]
                  (if
                   (clojure.core/= nil rest__6306)
                   (clojure.core/let
                    [left__6307 (clojure.core/next left__6303)]
                    (if
                     (clojure.core/= nil left__6307)
                     (clojure.core/let
                      [output__6300 (import-ast view pattern)]
                      (.invoke true-cont__6298 output__6300 nil))
                     (.invoke false-cont__6299)))
                   (.invoke false-cont__6299))))
                (clojure.core/fn [] (.invoke false-cont__6299))))
              (.invoke false-cont__6299)))))
          (.invoke false-cont__6299)))
        (.invoke false-cont__6299))
       (.invoke false-cont__6299)))
     (thunk__6309
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (symbol? input__6297)
       (clojure.core/let
        [variable input__6297]
        (clojure.core/let
         [output__6300 (literal-ast variable)]
         (.invoke true-cont__6298 output__6300 nil)))
       (thunk__6308 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6315
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6310 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6310)
          (clojure.core/let
           [left__6311 (clojure.core/first left__6310)]
           (if
            (clojure.core/= 'or left__6311)
            (clojure.core/let
             [left__6312 (clojure.core/next left__6310)]
             ((.view-fn (one-or-more pattern))
              left__6312
              (clojure.core/fn
               [output__6313 rest__6314]
               (clojure.core/let
                [patterns output__6313]
                (if
                 (clojure.core/= nil rest__6314)
                 (clojure.core/let
                  [output__6300 (apply or-ast patterns)]
                  (.invoke true-cont__6298 output__6300 nil))
                 (thunk__6309
                  true-cont__6298
                  false-cont__6299
                  input__6297))))
              (clojure.core/fn
               []
               (thunk__6309
                true-cont__6298
                false-cont__6299
                input__6297))))
            (thunk__6309
             true-cont__6298
             false-cont__6299
             input__6297)))
          (thunk__6309 true-cont__6298 false-cont__6299 input__6297)))
        (thunk__6309 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6309 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6321
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6316 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6316)
          (clojure.core/let
           [left__6317 (clojure.core/first left__6316)]
           (if
            (clojure.core/= 'seq left__6317)
            (clojure.core/let
             [left__6318 (clojure.core/next left__6316)]
             ((.view-fn (one-or-more pattern))
              left__6318
              (clojure.core/fn
               [output__6319 rest__6320]
               (clojure.core/let
                [patterns output__6319]
                (if
                 (clojure.core/= nil rest__6320)
                 (clojure.core/let
                  [output__6300 (apply seq-ast patterns)]
                  (.invoke true-cont__6298 output__6300 nil))
                 (thunk__6315
                  true-cont__6298
                  false-cont__6299
                  input__6297))))
              (clojure.core/fn
               []
               (thunk__6315
                true-cont__6298
                false-cont__6299
                input__6297))))
            (thunk__6315
             true-cont__6298
             false-cont__6299
             input__6297)))
          (thunk__6315 true-cont__6298 false-cont__6299 input__6297)))
        (thunk__6315 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6315 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6327
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6322 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6322)
          (clojure.core/let
           [left__6323 (clojure.core/first left__6322)]
           (if
            (clojure.core/= 'and left__6323)
            (clojure.core/let
             [left__6324 (clojure.core/next left__6322)]
             ((.view-fn (one-or-more pattern))
              left__6324
              (clojure.core/fn
               [output__6325 rest__6326]
               (clojure.core/let
                [patterns output__6325]
                (if
                 (clojure.core/= nil rest__6326)
                 (clojure.core/let
                  [output__6300 (apply and-ast patterns)]
                  (.invoke true-cont__6298 output__6300 nil))
                 (thunk__6321
                  true-cont__6298
                  false-cont__6299
                  input__6297))))
              (clojure.core/fn
               []
               (thunk__6321
                true-cont__6298
                false-cont__6299
                input__6297))))
            (thunk__6321
             true-cont__6298
             false-cont__6299
             input__6297)))
          (thunk__6321 true-cont__6298 false-cont__6299 input__6297)))
        (thunk__6321 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6321 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6333
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6328 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6328)
          (clojure.core/let
           [left__6329 (clojure.core/first left__6328)]
           (if
            (clojure.core/= 'leave left__6329)
            (clojure.core/let
             [left__6330 (clojure.core/next left__6328)]
             (if
              (clojure.core/not= nil left__6330)
              (clojure.core/let
               [left__6331 (clojure.core/first left__6330)]
               (clojure.core/let
                [form left__6331]
                (clojure.core/let
                 [left__6332 (clojure.core/next left__6330)]
                 (if
                  (clojure.core/= nil left__6332)
                  (clojure.core/let
                   [output__6300 (->Leave form)]
                   (.invoke true-cont__6298 output__6300 nil))
                  (thunk__6327
                   true-cont__6298
                   false-cont__6299
                   input__6297)))))
              (thunk__6327
               true-cont__6298
               false-cont__6299
               input__6297)))
            (thunk__6327
             true-cont__6298
             false-cont__6299
             input__6297)))
          (thunk__6327 true-cont__6298 false-cont__6299 input__6297)))
        (thunk__6327 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6327 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6339
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6334 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6334)
          (clojure.core/let
           [left__6335 (clojure.core/first left__6334)]
           (if
            (clojure.core/= 'guard left__6335)
            (clojure.core/let
             [left__6336 (clojure.core/next left__6334)]
             (if
              (clojure.core/not= nil left__6336)
              (clojure.core/let
               [left__6337 (clojure.core/first left__6336)]
               (clojure.core/let
                [form left__6337]
                (clojure.core/let
                 [left__6338 (clojure.core/next left__6336)]
                 (if
                  (clojure.core/= nil left__6338)
                  (clojure.core/let
                   [output__6300 (->Guard form)]
                   (.invoke true-cont__6298 output__6300 nil))
                  (thunk__6333
                   true-cont__6298
                   false-cont__6299
                   input__6297)))))
              (thunk__6333
               true-cont__6298
               false-cont__6299
               input__6297)))
            (thunk__6333
             true-cont__6298
             false-cont__6299
             input__6297)))
          (thunk__6333 true-cont__6298 false-cont__6299 input__6297)))
        (thunk__6333 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6333 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6345
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6340 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6340)
          (clojure.core/let
           [left__6341 (clojure.core/first left__6340)]
           (if
            (clojure.core/= 'quote left__6341)
            (clojure.core/let
             [left__6342 (clojure.core/next left__6340)]
             (if
              (clojure.core/not= nil left__6342)
              (clojure.core/let
               [left__6343 (clojure.core/first left__6342)]
               (clojure.core/let
                [quoted left__6343]
                (clojure.core/let
                 [left__6344 (clojure.core/next left__6342)]
                 (if
                  (clojure.core/= nil left__6344)
                  (clojure.core/let
                   [output__6300
                    (literal-ast
                     (clojure.core/seq
                      (clojure.core/concat
                       (clojure.core/list 'quote)
                       (clojure.core/list quoted))))]
                   (.invoke true-cont__6298 output__6300 nil))
                  (thunk__6339
                   true-cont__6298
                   false-cont__6299
                   input__6297)))))
              (thunk__6339
               true-cont__6298
               false-cont__6299
               input__6297)))
            (thunk__6339
             true-cont__6298
             false-cont__6299
             input__6297)))
          (thunk__6339 true-cont__6298 false-cont__6299 input__6297)))
        (thunk__6339 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6339 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6349
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (vector? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6346 (clojure.core/seq input__6297)]
         ((.view-fn (zero-or-more seq-pattern))
          left__6346
          (clojure.core/fn
           [output__6347 rest__6348]
           (clojure.core/let
            [seq-patterns output__6347]
            (if
             (clojure.core/= nil rest__6348)
             (clojure.core/let
              [output__6300 (seqable-ast seq-patterns)]
              (.invoke true-cont__6298 output__6300 nil))
             (thunk__6345
              true-cont__6298
              false-cont__6299
              input__6297))))
          (clojure.core/fn
           []
           (thunk__6345
            true-cont__6298
            false-cont__6299
            input__6297))))
        (thunk__6345 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6345 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6359
      [true-cont__6298
       left__6350
       false-cont__6299
       true-case-input__6352
       input__6297]
      (if
       (clojure.core/= nil true-case-input__6352)
       (clojure.core/let
        [left__6353 (clojure.core/next left__6350)]
        (if
         (clojure.core/not= nil left__6353)
         (clojure.core/let
          [left__6354 (clojure.core/first left__6353)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__6354)
            (clojure.core/nil? left__6354))
           (clojure.core/let
            [left__6355 (clojure.core/seq left__6354)]
            (if
             (clojure.core/not= nil left__6355)
             (clojure.core/let
              [left__6356 (clojure.core/first left__6355)]
              (clojure.core/let
               [arg left__6356]
               (clojure.core/let
                [left__6357 (clojure.core/next left__6355)]
                (if
                 (clojure.core/= nil left__6357)
                 (clojure.core/let
                  [left__6358 (clojure.core/next left__6353)]
                  (clojure.core/let
                   [body left__6358]
                   (clojure.core/let
                    [output__6300
                     (predicate-ast
                      (clojure.core/seq
                       (clojure.core/concat
                        (clojure.core/list 'do)
                        (clojure.walk/prewalk-replace
                         {arg input-sym}
                         body))))]
                    (.invoke true-cont__6298 output__6300 nil))))
                 (thunk__6349
                  true-cont__6298
                  false-cont__6299
                  input__6297)))))
             (thunk__6349
              true-cont__6298
              false-cont__6299
              input__6297)))
           (thunk__6349 true-cont__6298 false-cont__6299 input__6297)))
         (thunk__6349 true-cont__6298 false-cont__6299 input__6297)))
       (thunk__6349 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6360
      [left__6351
       true-cont__6298
       left__6350
       false-cont__6299
       input__6297]
      (if
       (clojure.core/= 'fn* left__6351)
       (thunk__6359
        true-cont__6298
        left__6350
        false-cont__6299
        nil
        input__6297)
       (thunk__6349 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6361
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6350 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6350)
          (clojure.core/let
           [left__6351 (clojure.core/first left__6350)]
           (if
            (clojure.core/= 'fn left__6351)
            (thunk__6359
             true-cont__6298
             left__6350
             false-cont__6299
             nil
             input__6297)
            (thunk__6360
             left__6351
             true-cont__6298
             left__6350
             false-cont__6299
             input__6297)))
          (thunk__6349 true-cont__6298 false-cont__6299 input__6297)))
        (thunk__6349 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6349 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6362
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (predicate? input__6297)
       (clojure.core/let
        [predicate input__6297]
        (clojure.core/let
         [output__6300
          (predicate-ast
           (clojure.core/seq
            (clojure.core/concat
             (clojure.core/list predicate)
             (clojure.core/list input-sym))))]
         (.invoke true-cont__6298 output__6300 nil)))
       (thunk__6361 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6363
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (clojure.core/instance? java.util.regex.Pattern input__6297)
       (clojure.core/let
        [regex input__6297]
        (clojure.core/let
         [output__6300 (regex-ast regex)]
         (.invoke true-cont__6298 output__6300 nil)))
       (thunk__6362 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6369
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (seq? input__6297)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6297)
         (clojure.core/nil? input__6297))
        (clojure.core/let
         [left__6364 (clojure.core/seq input__6297)]
         (if
          (clojure.core/not= nil left__6364)
          (clojure.core/let
           [left__6365 (clojure.core/first left__6364)]
           (if
            (constructor? left__6365)
            (clojure.core/let
             [constructor left__6365]
             (clojure.core/let
              [left__6366 (clojure.core/next left__6364)]
              ((.view-fn (zero-or-more pattern))
               left__6366
               (clojure.core/fn
                [output__6367 rest__6368]
                (clojure.core/let
                 [arg-patterns output__6367]
                 (if
                  (clojure.core/= nil rest__6368)
                  (clojure.core/let
                   [output__6300
                    (constructor-ast
                     (constructor-name constructor)
                     arg-patterns)]
                   (.invoke true-cont__6298 output__6300 nil))
                  (thunk__6363
                   true-cont__6298
                   false-cont__6299
                   input__6297))))
               (clojure.core/fn
                []
                (thunk__6363
                 true-cont__6298
                 false-cont__6299
                 input__6297)))))
            (thunk__6363
             true-cont__6298
             false-cont__6299
             input__6297)))
          (thunk__6363 true-cont__6298 false-cont__6299 input__6297)))
        (thunk__6363 true-cont__6298 false-cont__6299 input__6297))
       (thunk__6363 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6374
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__6297)
        (clojure.core/nil? input__6297))
       (clojure.core/let
        [left__6371 (clojure.core/seq input__6297)]
        ((.view-fn (zero-or-more key&pattern))
         left__6371
         (clojure.core/fn
          [output__6372 rest__6373]
          (clojure.core/let
           [keys&patterns output__6372]
           (if
            (clojure.core/= nil rest__6373)
            (clojure.core/let
             [output__6300 (map-ast keys&patterns)]
             (.invoke true-cont__6298 output__6300 nil))
            (thunk__6369
             true-cont__6298
             false-cont__6299
             input__6297))))
         (clojure.core/fn
          []
          (thunk__6369 true-cont__6298 false-cont__6299 input__6297))))
       (thunk__6369 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6375
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentHashMap
        input__6297)
       (thunk__6374 true-cont__6298 false-cont__6299 input__6297)
       (thunk__6369 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6376
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentArrayMap
        input__6297)
       (thunk__6374 true-cont__6298 false-cont__6299 input__6297)
       (thunk__6375 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6377
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (class-name? input__6297)
       (clojure.core/let
        [class-name input__6297]
        (clojure.core/let
         [output__6300 (class-ast class-name)]
         (.invoke true-cont__6298 output__6300 nil)))
       (thunk__6376 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6378
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (primitive? input__6297)
       (clojure.core/let
        [primitive input__6297]
        (clojure.core/let
         [output__6300 (literal-ast primitive)]
         (.invoke true-cont__6298 output__6300 nil)))
       (thunk__6377 true-cont__6298 false-cont__6299 input__6297)))
     (thunk__6379
      [true-cont__6298 false-cont__6299 input__6297]
      (if
       (binding? input__6297)
       (clojure.core/let
        [binding input__6297]
        (clojure.core/let
         [output__6300 (->Bind (binding-name binding))]
         (.invoke true-cont__6298 output__6300 nil)))
       (thunk__6378 true-cont__6298 false-cont__6299 input__6297)))]
    (strucjure/->View
     (clojure.core/fn
      [input__6297 true-cont__6298 false-cont__6299]
      (if
       (clojure.core/= '_ input__6297)
       (clojure.core/let
        [output__6300 (->Leave nil)]
        (.invoke true-cont__6298 output__6300 nil))
       (thunk__6379 true-cont__6298 false-cont__6299 input__6297))))))
 (def
  seq-pattern
  '(clojure.core/letfn
    [(thunk__6386
      [true-cont__6381 false-cont__6382 input__6380]
      ((.view-fn pattern)
       input__6380
       (clojure.core/fn
        [output__6384 rest__6385]
        (clojure.core/let
         [pattern output__6384]
         (clojure.core/let
          [output__6383 (head-ast pattern)]
          (.invoke true-cont__6381 output__6383 rest__6385))))
       (clojure.core/fn [] (.invoke false-cont__6382))))
     (thunk__6392
      [true-cont__6381 false-cont__6382 input__6380]
      (if
       (seq? input__6380)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6380)
         (clojure.core/nil? input__6380))
        (clojure.core/let
         [left__6387 (clojure.core/seq input__6380)]
         (if
          (clojure.core/not= nil left__6387)
          (clojure.core/let
           [left__6388 (clojure.core/first left__6387)]
           (if
            (clojure.core/= 'guard left__6388)
            (clojure.core/let
             [left__6389 (clojure.core/next left__6387)]
             (if
              (clojure.core/not= nil left__6389)
              (clojure.core/let
               [left__6390 (clojure.core/first left__6389)]
               (clojure.core/let
                [form left__6390]
                (clojure.core/let
                 [left__6391 (clojure.core/next left__6389)]
                 (if
                  (clojure.core/= nil left__6391)
                  (clojure.core/let
                   [output__6383 (->Guard form)]
                   (.invoke true-cont__6381 output__6383 nil))
                  (thunk__6386
                   true-cont__6381
                   false-cont__6382
                   input__6380)))))
              (thunk__6386
               true-cont__6381
               false-cont__6382
               input__6380)))
            (thunk__6386
             true-cont__6381
             false-cont__6382
             input__6380)))
          (thunk__6386 true-cont__6381 false-cont__6382 input__6380)))
        (thunk__6386 true-cont__6381 false-cont__6382 input__6380))
       (thunk__6386 true-cont__6381 false-cont__6382 input__6380)))]
    (strucjure/->View
     (clojure.core/fn
      [input__6380 true-cont__6381 false-cont__6382]
      (if
       (seq? input__6380)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__6380)
         (clojure.core/nil? input__6380))
        (clojure.core/let
         [left__6393 (clojure.core/seq input__6380)]
         (if
          (clojure.core/not= nil left__6393)
          (clojure.core/let
           [left__6394 (clojure.core/first left__6393)]
           (if
            (clojure.core/= '& left__6394)
            (clojure.core/let
             [left__6395 (clojure.core/next left__6393)]
             (if
              (clojure.core/not= nil left__6395)
              (clojure.core/let
               [left__6396 (clojure.core/first left__6395)]
               ((.view-fn pattern)
                left__6396
                (clojure.core/fn
                 [output__6397 rest__6398]
                 (clojure.core/let
                  [pattern output__6397]
                  (if
                   (clojure.core/= nil rest__6398)
                   (clojure.core/let
                    [left__6399 (clojure.core/next left__6395)]
                    (if
                     (clojure.core/= nil left__6399)
                     (clojure.core/let
                      [output__6383 pattern]
                      (.invoke true-cont__6381 output__6383 nil))
                     (thunk__6392
                      true-cont__6381
                      false-cont__6382
                      input__6380)))
                   (thunk__6392
                    true-cont__6381
                    false-cont__6382
                    input__6380))))
                (clojure.core/fn
                 []
                 (thunk__6392
                  true-cont__6381
                  false-cont__6382
                  input__6380))))
              (thunk__6392
               true-cont__6381
               false-cont__6382
               input__6380)))
            (thunk__6392
             true-cont__6381
             false-cont__6382
             input__6380)))
          (thunk__6392 true-cont__6381 false-cont__6382 input__6380)))
        (thunk__6392 true-cont__6381 false-cont__6382 input__6380))
       (thunk__6392 true-cont__6381 false-cont__6382 input__6380)))))))
