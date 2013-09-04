(ns strucjure.regression.sandbox
  (:refer-clojure :exclude [with-meta or and * num])
  (:require [clojure.stacktrace :refer [e]]
            [plumbing.core :refer [map-vals fnk]]
            [strucjure.core :as core]
            [strucjure.pattern :refer :all :exclude [with-binding]]
            ;;[strucjure.graph :refer :all :exclude [with-binding]]
            [strucjure.view :refer :all]
            ;;[strucjure.sugar :refer :all]
            ))
