(ns strucjure.regression.sandbox
  (:refer-clojure :exclude [with-meta or and * + ? num])
  (:require [clojure.stacktrace :refer [e]]
            [plumbing.core :refer [map-vals fnk]]
            [strucjure.pattern :refer :all :exclude [with-binding]]
            [strucjure.graph :refer :all :exclude [with-binding]]
            ;;[strucjure.debug :refer :all]
            [strucjure.view :refer :all :exclude [view]]
            [strucjure.sugar :refer :all]))
