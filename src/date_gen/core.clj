(ns date-gen.core
  "Utilities for generating randomized dates in various forms of structure."
  (:require [clojure.spec.alpha :as s]
            [net.cgrand.xforms :as x]
            [net.cgrand.xforms.io :as xio]
            [date-gen.domain :as domain]
            [date-gen.verify :as verify]
            [date-gen.util :as util]
            [date-gen.xform :as xform])
  (:import (java.util Random)))


(s/fdef random-dates-by-line
  :args (s/cat :n int?
               :formats ::domain/formats)
  :ret ::domain/lines
  :fn (s/and (util/complex [[:args :formats] :ret] verify/line-per-format?)
             (util/complex [[:args :n] :ret] verify/lines-contain-n-dates?)))

(defn random-dates-by-line
  "Generate `n` random dates per format as a sequence of data records."
  [n formats]
  (let [gen (new Random)]
    (sequence (xform/random-dates-by-line-xf gen n)
              formats)))


(s/fdef format->random-dates
  :args (s/cat :n int?
               :formats ::domain/formats)
  :ret ::domain/format->dates
  :fn (s/and (util/complex [[:args :formats] :ret] verify/association-per-format?)
             (util/complex [[:args :n] :ret] verify/n-dates-per-association?)))

(defn format->random-dates
  "Generate `n` random dates per format as a mapping from format to dates."
  [n formats]
  (let [gen (new Random)]
    (x/into {}
            (xform/random-dates-by-format-xf gen n)
            formats)))


(s/fdef random-dates-csv
  :args (s/cat :path string?
               :n int?
               :formats ::domain/formats))

(defn random-dates-csv
  "Generate `n` random dates per format, serializing output to a CSV file."
  [path n formats]
  (let [gen (new Random)]
    (xio/lines-out path
                   (xform/random-dates-by-line-xf gen n)
                   formats)))


(comment
  (set! *warn-on-reflection* true)

  (require '[orchestra.spec.test :as st])
  (st/instrument)

  (def xs ["yyyyMMdd"
           "MMMM dd, yyyy"
           "mmmm d, yyyy"
           "MM/dd/yyyy h:mm:ss a"
           "MM/dd/yyyy hh:mm:ss a"
           "d-M-yy"])

  (random-dates-by-line 4 xs)
  (format->random-dates 10 xs)
  (random-dates-csv "test.csv" 1000 xs)
  )
