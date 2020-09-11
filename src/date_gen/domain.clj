(ns date-gen.domain
  "Core specs describing the problem space."
  (:require [clojure.spec.alpha :as s])
  (:import (java.text SimpleDateFormat)
           (java.util Random)))

(s/def ::gen #(instance? Random %))

(s/def ::format
  (s/and string?
         #(try (instance? SimpleDateFormat (new SimpleDateFormat %))
               (catch IllegalArgumentException _ false))))

(s/def ::formats (s/coll-of ::format))

(s/def ::date string?)

(s/def ::dates (s/coll-of ::date))

(s/def ::format+date (s/tuple ::format ::date))

(s/def ::format+dates (s/tuple ::format ::dates))

(s/def ::format->dates (s/map-of ::format ::dates))

(s/def ::line string?)

(s/def ::lines (s/coll-of ::line))
