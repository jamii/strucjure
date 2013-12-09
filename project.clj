(defproject strucjure "0.4.0"
  :description "Pattern-matching, parsing and generic traversals via PEGs"
  :url "http://github.com/jamii/strucjure"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :java-source-paths ["src"]
  :jvm-opts ^:replace ["-server"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [prismatic/plumbing "0.1.0"]
                 [proteus "0.1.4"]])
