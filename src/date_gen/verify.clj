(ns date-gen.verify
  "Predicates for verifying the implementation."
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [date-gen.domain :as domain])
  (:import (java.util Date)
           (java.text SimpleDateFormat ParseException)
           (java.time Instant)))


(s/fdef formatted-date?
  :args (s/cat :format ::domain/format
               :str string?)
  :ret boolean?)

(defn formatted-date?
  "Does the string `str` represent a date in the specified `format`?"
  [format str]
  (try (instance? Date (.parse (new SimpleDateFormat format) str))
       (catch ParseException _ false)))


(s/fdef line-contains-n-dates?
  :args (s/cat :n int?
               :line ::domain/line)
  :ret boolean?)

(defn line-contains-n-dates?
  "Does the `line` contain `n` dates?"
  [n line]
  ;; NOTE: Search for comma-separated quoted literals (eg. "\"abc\", \"def\"")
  (let [re #", (?=([^\"]*\"[^\"]*\")*[^\"]*$)"]
    (= (inc n)
       (count (str/split line re)))))


(s/fdef lines-contain-n-dates?
  :args (s/cat :n int?
               :lines ::domain/lines)
  :ret boolean?)

(defn lines-contain-n-dates?
  "Does each line contain `n` dates?"
  [n lines]
  (every? #(line-contains-n-dates? n %)
          lines))


(s/fdef line-per-format?
  :args (s/cat :formats ::domain/formats
               :lines ::domain/lines)
  :ret boolean?)

(defn line-per-format?
  "Is there a line for every date format?"
  [formats lines]
  (= (count formats)
     (count lines)))


(s/fdef association-per-format?
  :args (s/cat :formats ::domain/formats
               :format->dates ::domain/format->dates)
  :ret boolean?)

(defn association-per-format?
  "Is there a grouping of dates for every format?"
  [formats format->dates]
  (= (count formats)
     (count format->dates)))


(s/fdef n-dates-per-association?
  :args (s/cat :n int?
               :format->dates ::domain/format->dates)
  :ret boolean?)

(defn n-dates-per-association?
  "Is there `n` amount of dates per format associated grouping?"
  [n format->dates]
  (every? (fn [[_ dates]]
            (= n (count dates)))
          format->dates))


(s/fdef wrapped-with?
  :args (s/cat :chr char?
               :str string?)
  :ret boolean?)

(defn wrapped-with?
  "Is the string `str` wrapped with the character `chr`?"
  [chr str]
  (= chr
     (first str)
     (last str)))
