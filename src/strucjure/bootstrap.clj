(ns strucjure.bootstrap)
(do
 (def
  optional
  '(clojure.core/letfn
    [(thunk__7212
      [input__7207 true-cont__7208 false-cont__7209]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__7207)
        (clojure.core/nil? input__7207))
       (clojure.core/let
        [left__7211 (clojure.core/seq input__7207)]
        (clojure.core/let
         [rest left__7211]
         (clojure.core/let
          [output__7210 nil]
          (.invoke true-cont__7208 output__7210 rest))))
       (.invoke false-cont__7209)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__7207 true-cont__7208 false-cont__7209]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7207)
         (clojure.core/nil? input__7207))
        (clojure.core/let
         [left__7213 (clojure.core/seq input__7207)]
         (if
          (clojure.core/not= nil left__7213)
          (clojure.core/let
           [left__7214 (clojure.core/first left__7213)]
           ((.view-fn elem)
            left__7214
            (clojure.core/fn
             [output__7215 rest__7216]
             (clojure.core/let
              [x output__7215]
              (if
               (clojure.core/= nil rest__7216)
               (clojure.core/let
                [left__7217 (clojure.core/next left__7213)]
                (clojure.core/let
                 [rest left__7217]
                 (clojure.core/let
                  [output__7210 x]
                  (.invoke true-cont__7208 output__7210 rest))))
               (thunk__7212
                input__7207
                true-cont__7208
                false-cont__7209))))
            (clojure.core/fn
             []
             (thunk__7212
              input__7207
              true-cont__7208
              false-cont__7209))))
          (thunk__7212 input__7207 true-cont__7208 false-cont__7209)))
        (thunk__7212
         input__7207
         true-cont__7208
         false-cont__7209)))))))
 (def
  zero-or-more
  '(clojure.core/letfn
    [(thunk__7236
      [input__7231 true-cont__7232 false-cont__7233]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__7231)
        (clojure.core/nil? input__7231))
       (clojure.core/let
        [left__7235 (clojure.core/seq input__7231)]
        (clojure.core/let
         [rest left__7235]
         (clojure.core/let
          [output__7234 nil]
          (.invoke true-cont__7232 output__7234 rest))))
       (.invoke false-cont__7233)))]
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__7231 true-cont__7232 false-cont__7233]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7231)
         (clojure.core/nil? input__7231))
        (clojure.core/let
         [left__7237 (clojure.core/seq input__7231)]
         (if
          (clojure.core/not= nil left__7237)
          (clojure.core/let
           [left__7238 (clojure.core/first left__7237)]
           ((.view-fn elem)
            left__7238
            (clojure.core/fn
             [output__7239 rest__7240]
             (clojure.core/let
              [x output__7239]
              (if
               (clojure.core/= nil rest__7240)
               (clojure.core/let
                [left__7241 (clojure.core/next left__7237)]
                ((.view-fn (zero-or-more elem))
                 left__7241
                 (clojure.core/fn
                  [output__7242 rest__7243]
                  (clojure.core/let
                   [xs output__7242]
                   (clojure.core/let
                    [rest rest__7243]
                    (clojure.core/let
                     [output__7234 (cons x xs)]
                     (.invoke true-cont__7232 output__7234 rest)))))
                 (clojure.core/fn
                  []
                  (thunk__7236
                   input__7231
                   true-cont__7232
                   false-cont__7233))))
               (thunk__7236
                input__7231
                true-cont__7232
                false-cont__7233))))
            (clojure.core/fn
             []
             (thunk__7236
              input__7231
              true-cont__7232
              false-cont__7233))))
          (thunk__7236 input__7231 true-cont__7232 false-cont__7233)))
        (thunk__7236
         input__7231
         true-cont__7232
         false-cont__7233)))))))
 (def
  one-or-more
  '(clojure.core/letfn
    []
    (clojure.core/fn
     [elem]
     (strucjure/->View
      (clojure.core/fn
       [input__7255 true-cont__7256 false-cont__7257]
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7255)
         (clojure.core/nil? input__7255))
        (clojure.core/let
         [left__7259 (clojure.core/seq input__7255)]
         (if
          (clojure.core/not= nil left__7259)
          (clojure.core/let
           [left__7260 (clojure.core/first left__7259)]
           ((.view-fn elem)
            left__7260
            (clojure.core/fn
             [output__7261 rest__7262]
             (clojure.core/let
              [x output__7261]
              (if
               (clojure.core/= nil rest__7262)
               (clojure.core/let
                [left__7263 (clojure.core/next left__7259)]
                ((.view-fn (zero-or-more elem))
                 left__7263
                 (clojure.core/fn
                  [output__7264 rest__7265]
                  (clojure.core/let
                   [xs output__7264]
                   (clojure.core/let
                    [rest rest__7265]
                    (clojure.core/let
                     [output__7258 (cons x xs)]
                     (.invoke true-cont__7256 output__7258 rest)))))
                 (clojure.core/fn [] (.invoke false-cont__7257))))
               (.invoke false-cont__7257))))
            (clojure.core/fn [] (.invoke false-cont__7257))))
          (.invoke false-cont__7257)))
        (.invoke false-cont__7257)))))))
 (def
  key&pattern
  '(clojure.core/letfn
    []
    (strucjure/->View
     (clojure.core/fn
      [input__7277 true-cont__7278 false-cont__7279]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__7277)
        (clojure.core/nil? input__7277))
       (clojure.core/let
        [left__7281 (clojure.core/seq input__7277)]
        (if
         (clojure.core/not= nil left__7281)
         (clojure.core/let
          [left__7282 (clojure.core/first left__7281)]
          (clojure.core/let
           [key left__7282]
           (clojure.core/let
            [left__7283 (clojure.core/next left__7281)]
            (if
             (clojure.core/not= nil left__7283)
             (clojure.core/let
              [left__7284 (clojure.core/first left__7283)]
              ((.view-fn pattern)
               left__7284
               (clojure.core/fn
                [output__7285 rest__7286]
                (clojure.core/let
                 [pattern output__7285]
                 (if
                  (clojure.core/= nil rest__7286)
                  (clojure.core/let
                   [left__7287 (clojure.core/next left__7283)]
                   (if
                    (clojure.core/= nil left__7287)
                    (clojure.core/let
                     [output__7280 [key pattern]]
                     (.invoke true-cont__7278 output__7280 nil))
                    (.invoke false-cont__7279)))
                  (.invoke false-cont__7279))))
               (clojure.core/fn [] (.invoke false-cont__7279))))
             (.invoke false-cont__7279)))))
         (.invoke false-cont__7279)))
       (.invoke false-cont__7279))))))
 (def
  pattern
  '(clojure.core/letfn
    [(thunk__7382
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7375 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7375)
          (clojure.core/let
           [left__7376 (clojure.core/first left__7375)]
           (clojure.core/let
            [view left__7376]
            (clojure.core/let
             [left__7377 (clojure.core/next left__7375)]
             (if
              (clojure.core/not= nil left__7377)
              (clojure.core/let
               [left__7378 (clojure.core/first left__7377)]
               ((.view-fn pattern)
                left__7378
                (clojure.core/fn
                 [output__7379 rest__7380]
                 (clojure.core/let
                  [pattern output__7379]
                  (if
                   (clojure.core/= nil rest__7380)
                   (clojure.core/let
                    [left__7381 (clojure.core/next left__7377)]
                    (if
                     (clojure.core/= nil left__7381)
                     (clojure.core/let
                      [output__7374 (import-ast view pattern)]
                      (.invoke true-cont__7372 output__7374 nil))
                     (.invoke false-cont__7373)))
                   (.invoke false-cont__7373))))
                (clojure.core/fn [] (.invoke false-cont__7373))))
              (.invoke false-cont__7373)))))
          (.invoke false-cont__7373)))
        (.invoke false-cont__7373))
       (.invoke false-cont__7373)))
     (thunk__7383
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (symbol? input__7371)
       (clojure.core/let
        [variable input__7371]
        (clojure.core/let
         [output__7374 (literal-ast variable)]
         (.invoke true-cont__7372 output__7374 nil)))
       (thunk__7382 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7389
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7384 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7384)
          (clojure.core/let
           [left__7385 (clojure.core/first left__7384)]
           (if
            (clojure.core/= 'or left__7385)
            (clojure.core/let
             [left__7386 (clojure.core/next left__7384)]
             ((.view-fn (one-or-more pattern))
              left__7386
              (clojure.core/fn
               [output__7387 rest__7388]
               (clojure.core/let
                [patterns output__7387]
                (if
                 (clojure.core/= nil rest__7388)
                 (clojure.core/let
                  [output__7374 (apply or-ast patterns)]
                  (.invoke true-cont__7372 output__7374 nil))
                 (thunk__7383
                  input__7371
                  true-cont__7372
                  false-cont__7373))))
              (clojure.core/fn
               []
               (thunk__7383
                input__7371
                true-cont__7372
                false-cont__7373))))
            (thunk__7383
             input__7371
             true-cont__7372
             false-cont__7373)))
          (thunk__7383 input__7371 true-cont__7372 false-cont__7373)))
        (thunk__7383 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7383 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7395
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7390 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7390)
          (clojure.core/let
           [left__7391 (clojure.core/first left__7390)]
           (if
            (clojure.core/= 'seq left__7391)
            (clojure.core/let
             [left__7392 (clojure.core/next left__7390)]
             ((.view-fn (one-or-more pattern))
              left__7392
              (clojure.core/fn
               [output__7393 rest__7394]
               (clojure.core/let
                [patterns output__7393]
                (if
                 (clojure.core/= nil rest__7394)
                 (clojure.core/let
                  [output__7374 (apply seq-ast patterns)]
                  (.invoke true-cont__7372 output__7374 nil))
                 (thunk__7389
                  input__7371
                  true-cont__7372
                  false-cont__7373))))
              (clojure.core/fn
               []
               (thunk__7389
                input__7371
                true-cont__7372
                false-cont__7373))))
            (thunk__7389
             input__7371
             true-cont__7372
             false-cont__7373)))
          (thunk__7389 input__7371 true-cont__7372 false-cont__7373)))
        (thunk__7389 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7389 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7401
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7396 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7396)
          (clojure.core/let
           [left__7397 (clojure.core/first left__7396)]
           (if
            (clojure.core/= 'and left__7397)
            (clojure.core/let
             [left__7398 (clojure.core/next left__7396)]
             ((.view-fn (one-or-more pattern))
              left__7398
              (clojure.core/fn
               [output__7399 rest__7400]
               (clojure.core/let
                [patterns output__7399]
                (if
                 (clojure.core/= nil rest__7400)
                 (clojure.core/let
                  [output__7374 (apply and-ast patterns)]
                  (.invoke true-cont__7372 output__7374 nil))
                 (thunk__7395
                  input__7371
                  true-cont__7372
                  false-cont__7373))))
              (clojure.core/fn
               []
               (thunk__7395
                input__7371
                true-cont__7372
                false-cont__7373))))
            (thunk__7395
             input__7371
             true-cont__7372
             false-cont__7373)))
          (thunk__7395 input__7371 true-cont__7372 false-cont__7373)))
        (thunk__7395 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7395 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7407
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7402 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7402)
          (clojure.core/let
           [left__7403 (clojure.core/first left__7402)]
           (if
            (clojure.core/= 'leave left__7403)
            (clojure.core/let
             [left__7404 (clojure.core/next left__7402)]
             (if
              (clojure.core/not= nil left__7404)
              (clojure.core/let
               [left__7405 (clojure.core/first left__7404)]
               (clojure.core/let
                [form left__7405]
                (clojure.core/let
                 [left__7406 (clojure.core/next left__7404)]
                 (if
                  (clojure.core/= nil left__7406)
                  (clojure.core/let
                   [output__7374 (->Leave form)]
                   (.invoke true-cont__7372 output__7374 nil))
                  (thunk__7401
                   input__7371
                   true-cont__7372
                   false-cont__7373)))))
              (thunk__7401
               input__7371
               true-cont__7372
               false-cont__7373)))
            (thunk__7401
             input__7371
             true-cont__7372
             false-cont__7373)))
          (thunk__7401 input__7371 true-cont__7372 false-cont__7373)))
        (thunk__7401 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7401 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7413
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7408 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7408)
          (clojure.core/let
           [left__7409 (clojure.core/first left__7408)]
           (if
            (clojure.core/= 'guard left__7409)
            (clojure.core/let
             [left__7410 (clojure.core/next left__7408)]
             (if
              (clojure.core/not= nil left__7410)
              (clojure.core/let
               [left__7411 (clojure.core/first left__7410)]
               (clojure.core/let
                [form left__7411]
                (clojure.core/let
                 [left__7412 (clojure.core/next left__7410)]
                 (if
                  (clojure.core/= nil left__7412)
                  (clojure.core/let
                   [output__7374 (->Guard form)]
                   (.invoke true-cont__7372 output__7374 nil))
                  (thunk__7407
                   input__7371
                   true-cont__7372
                   false-cont__7373)))))
              (thunk__7407
               input__7371
               true-cont__7372
               false-cont__7373)))
            (thunk__7407
             input__7371
             true-cont__7372
             false-cont__7373)))
          (thunk__7407 input__7371 true-cont__7372 false-cont__7373)))
        (thunk__7407 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7407 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7419
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7414 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7414)
          (clojure.core/let
           [left__7415 (clojure.core/first left__7414)]
           (if
            (clojure.core/= 'quote left__7415)
            (clojure.core/let
             [left__7416 (clojure.core/next left__7414)]
             (if
              (clojure.core/not= nil left__7416)
              (clojure.core/let
               [left__7417 (clojure.core/first left__7416)]
               (clojure.core/let
                [quoted left__7417]
                (clojure.core/let
                 [left__7418 (clojure.core/next left__7416)]
                 (if
                  (clojure.core/= nil left__7418)
                  (clojure.core/let
                   [output__7374
                    (literal-ast
                     (clojure.core/seq
                      (clojure.core/concat
                       (clojure.core/list 'quote)
                       (clojure.core/list quoted))))]
                   (.invoke true-cont__7372 output__7374 nil))
                  (thunk__7413
                   input__7371
                   true-cont__7372
                   false-cont__7373)))))
              (thunk__7413
               input__7371
               true-cont__7372
               false-cont__7373)))
            (thunk__7413
             input__7371
             true-cont__7372
             false-cont__7373)))
          (thunk__7413 input__7371 true-cont__7372 false-cont__7373)))
        (thunk__7413 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7413 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7423
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (vector? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7420 (clojure.core/seq input__7371)]
         ((.view-fn (zero-or-more seq-pattern))
          left__7420
          (clojure.core/fn
           [output__7421 rest__7422]
           (clojure.core/let
            [seq-patterns output__7421]
            (if
             (clojure.core/= nil rest__7422)
             (clojure.core/let
              [output__7374 (seqable-ast seq-patterns)]
              (.invoke true-cont__7372 output__7374 nil))
             (thunk__7419
              input__7371
              true-cont__7372
              false-cont__7373))))
          (clojure.core/fn
           []
           (thunk__7419
            input__7371
            true-cont__7372
            false-cont__7373))))
        (thunk__7419 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7419 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7433
      [input__7371
       true-cont__7372
       false-cont__7373
       input__7371
       left__7424
       left__7424
       left__7424
       true-case-input__7426]
      (if
       (clojure.core/= nil true-case-input__7426)
       (clojure.core/let
        [left__7427 (clojure.core/next left__7424)]
        (if
         (clojure.core/not= nil left__7427)
         (clojure.core/let
          [left__7428 (clojure.core/first left__7427)]
          (if
           (clojure.core/or
            (clojure.core/instance? clojure.lang.Seqable left__7428)
            (clojure.core/nil? left__7428))
           (clojure.core/let
            [left__7429 (clojure.core/seq left__7428)]
            (if
             (clojure.core/not= nil left__7429)
             (clojure.core/let
              [left__7430 (clojure.core/first left__7429)]
              (clojure.core/let
               [arg left__7430]
               (clojure.core/let
                [left__7431 (clojure.core/next left__7429)]
                (if
                 (clojure.core/= nil left__7431)
                 (clojure.core/let
                  [left__7432 (clojure.core/next left__7427)]
                  (clojure.core/let
                   [body left__7432]
                   (clojure.core/let
                    [output__7374
                     (predicate-ast
                      (clojure.core/seq
                       (clojure.core/concat
                        (clojure.core/list 'do)
                        (clojure.walk/prewalk-replace
                         {arg input-sym}
                         body))))]
                    (.invoke true-cont__7372 output__7374 nil))))
                 (thunk__7423
                  input__7371
                  true-cont__7372
                  false-cont__7373)))))
             (thunk__7423
              input__7371
              true-cont__7372
              false-cont__7373)))
           (thunk__7423 input__7371 true-cont__7372 false-cont__7373)))
         (thunk__7423 input__7371 true-cont__7372 false-cont__7373)))
       (thunk__7423 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7434
      [input__7371
       true-cont__7372
       false-cont__7373
       input__7371
       left__7424
       left__7424
       left__7424
       left__7425
       left__7425]
      (if
       (clojure.core/= 'fn* left__7425)
       (thunk__7433
        input__7371
        true-cont__7372
        false-cont__7373
        input__7371
        left__7424
        left__7424
        left__7424
        nil)
       (thunk__7423 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7435
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7424 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7424)
          (clojure.core/let
           [left__7425 (clojure.core/first left__7424)]
           (if
            (clojure.core/= 'fn left__7425)
            (thunk__7433
             input__7371
             true-cont__7372
             false-cont__7373
             input__7371
             left__7424
             left__7424
             left__7424
             nil)
            (thunk__7434
             input__7371
             true-cont__7372
             false-cont__7373
             input__7371
             left__7424
             left__7424
             left__7424
             left__7425
             left__7425)))
          (thunk__7423 input__7371 true-cont__7372 false-cont__7373)))
        (thunk__7423 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7423 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7436
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (predicate? input__7371)
       (clojure.core/let
        [predicate input__7371]
        (clojure.core/let
         [output__7374
          (predicate-ast
           (clojure.core/seq
            (clojure.core/concat
             (clojure.core/list predicate)
             (clojure.core/list input-sym))))]
         (.invoke true-cont__7372 output__7374 nil)))
       (thunk__7435 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7437
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (clojure.core/instance? java.util.regex.Pattern input__7371)
       (clojure.core/let
        [regex input__7371]
        (clojure.core/let
         [output__7374 (regex-ast regex)]
         (.invoke true-cont__7372 output__7374 nil)))
       (thunk__7436 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7443
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (seq? input__7371)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7371)
         (clojure.core/nil? input__7371))
        (clojure.core/let
         [left__7438 (clojure.core/seq input__7371)]
         (if
          (clojure.core/not= nil left__7438)
          (clojure.core/let
           [left__7439 (clojure.core/first left__7438)]
           (if
            (constructor? left__7439)
            (clojure.core/let
             [constructor left__7439]
             (clojure.core/let
              [left__7440 (clojure.core/next left__7438)]
              ((.view-fn (zero-or-more pattern))
               left__7440
               (clojure.core/fn
                [output__7441 rest__7442]
                (clojure.core/let
                 [arg-patterns output__7441]
                 (if
                  (clojure.core/= nil rest__7442)
                  (clojure.core/let
                   [output__7374
                    (constructor-ast
                     (constructor-name constructor)
                     arg-patterns)]
                   (.invoke true-cont__7372 output__7374 nil))
                  (thunk__7437
                   input__7371
                   true-cont__7372
                   false-cont__7373))))
               (clojure.core/fn
                []
                (thunk__7437
                 input__7371
                 true-cont__7372
                 false-cont__7373)))))
            (thunk__7437
             input__7371
             true-cont__7372
             false-cont__7373)))
          (thunk__7437 input__7371 true-cont__7372 false-cont__7373)))
        (thunk__7437 input__7371 true-cont__7372 false-cont__7373))
       (thunk__7437 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7448
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (clojure.core/or
        (clojure.core/instance? clojure.lang.Seqable input__7371)
        (clojure.core/nil? input__7371))
       (clojure.core/let
        [left__7445 (clojure.core/seq input__7371)]
        ((.view-fn (zero-or-more key&pattern))
         left__7445
         (clojure.core/fn
          [output__7446 rest__7447]
          (clojure.core/let
           [keys&patterns output__7446]
           (if
            (clojure.core/= nil rest__7447)
            (clojure.core/let
             [output__7374 (map-ast keys&patterns)]
             (.invoke true-cont__7372 output__7374 nil))
            (thunk__7443
             input__7371
             true-cont__7372
             false-cont__7373))))
         (clojure.core/fn
          []
          (thunk__7443 input__7371 true-cont__7372 false-cont__7373))))
       (thunk__7443 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7449
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentHashMap
        input__7371)
       (thunk__7448 input__7371 true-cont__7372 false-cont__7373)
       (thunk__7443 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7450
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (clojure.core/instance?
        clojure.lang.PersistentArrayMap
        input__7371)
       (thunk__7448 input__7371 true-cont__7372 false-cont__7373)
       (thunk__7449 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7451
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (class-name? input__7371)
       (clojure.core/let
        [class-name input__7371]
        (clojure.core/let
         [output__7374 (class-ast class-name)]
         (.invoke true-cont__7372 output__7374 nil)))
       (thunk__7450 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7452
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (primitive? input__7371)
       (clojure.core/let
        [primitive input__7371]
        (clojure.core/let
         [output__7374 (literal-ast primitive)]
         (.invoke true-cont__7372 output__7374 nil)))
       (thunk__7451 input__7371 true-cont__7372 false-cont__7373)))
     (thunk__7453
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (binding? input__7371)
       (clojure.core/let
        [binding input__7371]
        (clojure.core/let
         [output__7374 (->Bind (binding-name binding))]
         (.invoke true-cont__7372 output__7374 nil)))
       (thunk__7452 input__7371 true-cont__7372 false-cont__7373)))]
    (strucjure/->View
     (clojure.core/fn
      [input__7371 true-cont__7372 false-cont__7373]
      (if
       (clojure.core/= '_ input__7371)
       (clojure.core/let
        [output__7374 (->Leave nil)]
        (.invoke true-cont__7372 output__7374 nil))
       (thunk__7453 input__7371 true-cont__7372 false-cont__7373))))))
 (def
  seq-pattern
  '(clojure.core/letfn
    [(thunk__7480
      [input__7474 true-cont__7475 false-cont__7476]
      ((.view-fn pattern)
       input__7474
       (clojure.core/fn
        [output__7478 rest__7479]
        (clojure.core/let
         [pattern output__7478]
         (clojure.core/let
          [output__7477 (head-ast pattern)]
          (.invoke true-cont__7475 output__7477 rest__7479))))
       (clojure.core/fn [] (.invoke false-cont__7476))))
     (thunk__7486
      [input__7474 true-cont__7475 false-cont__7476]
      (if
       (seq? input__7474)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7474)
         (clojure.core/nil? input__7474))
        (clojure.core/let
         [left__7481 (clojure.core/seq input__7474)]
         (if
          (clojure.core/not= nil left__7481)
          (clojure.core/let
           [left__7482 (clojure.core/first left__7481)]
           (if
            (clojure.core/= 'guard left__7482)
            (clojure.core/let
             [left__7483 (clojure.core/next left__7481)]
             (if
              (clojure.core/not= nil left__7483)
              (clojure.core/let
               [left__7484 (clojure.core/first left__7483)]
               (clojure.core/let
                [form left__7484]
                (clojure.core/let
                 [left__7485 (clojure.core/next left__7483)]
                 (if
                  (clojure.core/= nil left__7485)
                  (clojure.core/let
                   [output__7477 (->Guard form)]
                   (.invoke true-cont__7475 output__7477 nil))
                  (thunk__7480
                   input__7474
                   true-cont__7475
                   false-cont__7476)))))
              (thunk__7480
               input__7474
               true-cont__7475
               false-cont__7476)))
            (thunk__7480
             input__7474
             true-cont__7475
             false-cont__7476)))
          (thunk__7480 input__7474 true-cont__7475 false-cont__7476)))
        (thunk__7480 input__7474 true-cont__7475 false-cont__7476))
       (thunk__7480 input__7474 true-cont__7475 false-cont__7476)))]
    (strucjure/->View
     (clojure.core/fn
      [input__7474 true-cont__7475 false-cont__7476]
      (if
       (seq? input__7474)
       (if
        (clojure.core/or
         (clojure.core/instance? clojure.lang.Seqable input__7474)
         (clojure.core/nil? input__7474))
        (clojure.core/let
         [left__7487 (clojure.core/seq input__7474)]
         (if
          (clojure.core/not= nil left__7487)
          (clojure.core/let
           [left__7488 (clojure.core/first left__7487)]
           (if
            (clojure.core/= '& left__7488)
            (clojure.core/let
             [left__7489 (clojure.core/next left__7487)]
             (if
              (clojure.core/not= nil left__7489)
              (clojure.core/let
               [left__7490 (clojure.core/first left__7489)]
               ((.view-fn pattern)
                left__7490
                (clojure.core/fn
                 [output__7491 rest__7492]
                 (clojure.core/let
                  [pattern output__7491]
                  (if
                   (clojure.core/= nil rest__7492)
                   (clojure.core/let
                    [left__7493 (clojure.core/next left__7489)]
                    (if
                     (clojure.core/= nil left__7493)
                     (clojure.core/let
                      [output__7477 pattern]
                      (.invoke true-cont__7475 output__7477 nil))
                     (thunk__7486
                      input__7474
                      true-cont__7475
                      false-cont__7476)))
                   (thunk__7486
                    input__7474
                    true-cont__7475
                    false-cont__7476))))
                (clojure.core/fn
                 []
                 (thunk__7486
                  input__7474
                  true-cont__7475
                  false-cont__7476))))
              (thunk__7486
               input__7474
               true-cont__7475
               false-cont__7476)))
            (thunk__7486
             input__7474
             true-cont__7475
             false-cont__7476)))
          (thunk__7486 input__7474 true-cont__7475 false-cont__7476)))
        (thunk__7486 input__7474 true-cont__7475 false-cont__7476))
       (thunk__7486 input__7474 true-cont__7475 false-cont__7476)))))))
