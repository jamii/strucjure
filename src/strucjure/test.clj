(ns strucjure.test
  (:use strucjure
        clojure.test
        [slingshot.slingshot :only [throw+ try+]])
  (:require [strucjure.view :as view]
            [strucjure.pattern :as pattern]))

(defn tokenise [sep]
  (view/->Raw
   (fn [input]
     (when-let [elems (seq input)]
       (loop [elems elems
              token-acc nil
              tokens-acc nil]
         (if-let [[remaining _] (view/run* sep elems)]
           (recur remaining nil (cons (reverse token-acc) tokens-acc))
           (if-let [[elem & elems] elems]
             (recur elems (cons elem token-acc) tokens-acc)
             [nil (reverse (cons (reverse token-acc) tokens-acc))])))))))

(defview space
  \space %)

(defview newline
  \newline %)

(defview not-newline
  (not \newline) %)

(defview line
  (and (not []) ; have to consume at least one char
       (prefix & ((zero-or-more not-newline) ?line)
               & ((optional newline) ?end)))
  line)

(defview indented-line
  (prefix & ((one-or-more space) _) & (line ?line))
  line)

(defview exception-chars
  (or \.
      #(<= (int \a) (int %) (int \z))
      #(<= (int \A) (int %) (int \Z)))
  %)

(defview result
  [\E \x \c \e \p \t \i \o \n \I \n \f \o \space
   \t \h \r \o \w \+ \: \space
   \# & ((one-or-more exception-chars) ?exception)
   & _]
  [:throws (apply str exception)]

  ?data
  [:returns (apply str data)])

(defview example
  [& (line ?input-first)
   & ((zero-or-more-prefix indented-line) ?input-rest)
   & ((one-or-more-prefix line) ?output-lines)]
  {:input (with-out-str (doseq [line (cons input-first input-rest)] (print (apply str line) \space)))
   :prints (with-out-str (doseq [line (butlast output-lines)] (println (apply str line))))
   :result (run result (last output-lines))})

(defview prompt
  (prefix \u \s \e \r \> \space)
  :prompt)

(defview code-block-inner
  (and (prompt _)
       ((tokenise prompt) ?chunks))
  (map (partial run example) (filter #(not (empty? %)) chunks))

  _ ;; TODO would be nice to just eval other blocks and check for exceptions
  nil)

(defview code-block
  [\c \l \o \j \u \r \e \newline & (code-block-inner ?result)]
  result)

(defview code-delim
  (prefix \` \` \`)
  :code-delim)

(defview readme
  ((tokenise code-delim) ?chunks)
  (apply concat (map (partial run code-block) (take-nth 2 (rest chunks)))))

(defn replace-fun [unread-form]
  (.replaceAll unread-form "#<[^>]*>" "#<fun>"))

(defn prints-as [string form]
  (= (replace-fun string) (replace-fun (with-out-str (pr form)))))

(defn example-test [input prints result]
  (match result
         [:returns ?value]
         (do
           (is (prints-as value (input)))
           (is (= prints (with-out-str (input)))))

         [:throws ?exception]
         (do
           (is (try+ (input)
                     nil
                     (catch java.lang.Object thrown
                       (prints-as exception (class thrown)))))
           (is (= prints (with-out-str
                           (try+ (input)
                                 (catch java.lang.Object _ nil))))))))

(defmacro insert-example-test [{:keys [input prints result]}]
  `(example-test (fn [] (eval '(do (use '~'strucjure) ~(read-string input)))) ~prints '~result))

(defmacro insert-readme-test [file]
  `(do
     ~@(for [example (run readme (seq (slurp (eval file))))]
         `(insert-example-test ~example))))

(deftest readme-test
  (insert-readme-test "README.md"))
