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
  [:throws (resolve (symbol (apply str exception)))]

  ?data
  [:returns (read-string (apply str data))])

(defview example
  [& (line ?input-first)
   & ((zero-or-more-prefix indented-line) ?input-rest)
   & ((one-or-more-prefix line) ?output-lines)]
  {:input (with-out-str (doseq [line (cons input-first input-rest)] (print (apply str line) \space)))
   :prints (with-out-str (doseq [line output-lines] (println (apply str line))))
   :result (run-view result (last output-lines))})

(defview prompt
  (prefix \u \s \e \r \> \space)
  :prompt)

(defview code-block-inner
  (and (prompt _)
       ((tokenise prompt) ?chunks))
  (map (partial run-view example) (filter #(not (empty? %)) chunks))

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
  (apply concat (map (partial run-view code-block) (take-nth 2 (rest chunks)))))

(defn run-example [{:keys [input prints result]}]
  (prn 'woot)
  (match result
         [:returns ?value]
         (do
           (is (= value (eval (read-string input))))
           (is (= prints (with-out-str (eval (read-string input))))))

         [:throws ?exception]
         (do
           (is (try+ (eval (read-string input))
                     false
                     (catch (instance? exception %) _ true)))
           (is (= prints (with-out-str
                           (try+ (eval (read-string input))
                                 (catch (instance? exception %) _ nil))))))))

(deftest run-readme
  (doall (map run-example (run-view readme (seq (slurp "README.md"))))))
