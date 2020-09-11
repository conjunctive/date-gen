(ns date-gen.xform
  "Internal xforms for structured groupings of randomized dates."
  (:require [clojure.spec.alpha :as s]
            [net.cgrand.xforms :as x]
            [net.cgrand.xforms.rfs :as rf]
            [date-gen.domain :as domain]
            [date-gen.verify :as verify]
            [date-gen.util :as util])
  (:import (java.util Date Random)
           (java.text SimpleDateFormat ParseException)
           (java.time Instant)))


(s/fdef random-dates-per-format-xf
  :args (s/cat :gen ::domain/gen
               :n int?)
  :ret fn?)

(defn random-dates-per-format-xf
  "Generate `n` random dates per format."
  [gen n]
  (x/by-key identity
            (x/for [format %
                    n (range n)]
              (util/random-date gen format))))


(s/fdef random-dates-by-format-xf
  :args (s/cat :gen ::domain/gen
               :n int?)
  :ret fn?)

(defn random-dates-by-format-xf
  "Generate and group `n` random quoted dates by format."
  [gen n]
  (comp (random-dates-per-format-xf gen n)
        (x/by-key first
                  (comp util/quotes second)
                  (x/into []))))


(s/fdef merge-line
  :args (s/cat :format+dates ::domain/format+dates)
  :ret ::domain/line
  :fn (util/complex [[:args :format+dates second count] :ret] verify/line-contains-n-dates?))

(defn merge-line
  "Merge a format and grouping of dates into a comma-delimited string, where both the format and dates are quoted."
  [format+dates]
  (let [format (first format+dates)
        dates  (nth format+dates 1)]
    (transduce (comp cat
                     (interpose ", "))
               rf/str
               [[(util/quotes format)]
                dates])))


(s/fdef random-dates-by-line-xf
  :args (s/cat :gen ::domain/gen
               :n int?)
  :ret fn?)

(defn random-dates-by-line-xf
  "Generate data records consisting of a date format and `n` random dates, where both the format and dates are quoted."
  [gen n]
  (comp (random-dates-by-format-xf gen n)
        (map merge-line)))
