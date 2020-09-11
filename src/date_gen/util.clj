(ns date-gen.util
  "Utilities for function composition, string manipulation, and date generation."
  (:require [clojure.spec.alpha :as s]
            [date-gen.domain :as domain]
            [date-gen.verify :as verify])
  (:import (java.util Date Random)
           (java.text SimpleDateFormat ParseException)
           (java.time Instant)))


(s/fdef fold-comp
  :args (s/cat :xs (s/and vector?
                          (s/coll-of ifn?)))
  :ret fn?)

(defn fold-comp
  "Right-fold of composition over a vector of functions.

  (fold-comp [f g h])
  is equivalent to
  (comp h g f)"
  [xs]
  (loop [fns xs
         acc nil]
    (if (empty? fns)
      acc
      (recur (pop fns)
             (if acc
               (comp acc (peek fns))
               (peek fns))))))


(s/fdef complex
  :args (s/cat :xs (s/and vector?
                          (s/coll-of (s/or :f ifn?
                                           :v (s/and vector?
                                                     (s/coll-of ifn?)))))
               :f fn?)
  :ret fn?)

(defn complex
  "Multiplexed function composition.

  (complex [:args
            [:args :a]
            [:args :a :b]
            [:args :a :b :c]
            :ret]
           f)
  is equivalent to
  (fn [m]
    (f (:args m)
       (:a (:args m))
       (:b (:a (:args m)))
       (:c (:b (:a (:args m))))
       (:ret m)))"
  [xs f]
  (let [fns (mapv (fn [x]
                    (if (vector? x)
                      (fold-comp x)
                      x))
                  xs)
        g (apply juxt fns)]
    (fn [v]
      ;; NOTE: (f (a v) (b v) (c v) ...)
      (apply f (g v)))))


(s/fdef wrap
  :args (s/cat :chr char?
               :str string?)
  :ret string?
  :fn (complex [[:args :chr] :ret] verify/wrapped-with?))

(defn wrap
  "Surround a string with a character."
  [chr str]
  (.toString (doto (new StringBuilder)
               (.append chr)
               (.append str)
               (.append chr))))


(s/fdef quotes
  :args (s/cat :str string?)
  :ret string?
  :fn (complex [(constantly \") :ret] verify/wrapped-with?))

(defn quotes
  "Surround a string with quotes."
  [str]
  (wrap \" str))


(s/fdef random-date
  :args (s/cat :gen ::domain/gen
               :format ::domain/format)
  :ret ::domain/date
  :fn (complex [[:args :format] :ret] verify/formatted-date?))

(defn random-date
  "Generate a random date in a specified `format`."
  [^Random gen format]
  (let [^Long unix (mod (.nextLong gen)
                        (.toEpochMilli (Instant/now)))]
    (.format (new SimpleDateFormat format)
             (new Date unix))))
