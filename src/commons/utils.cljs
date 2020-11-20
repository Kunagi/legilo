(ns commons.utils
  (:require
   [clojure.spec.alpha :as s]))


;;; maps

(defn deep-merge
  [& maps]
  (apply merge-with (fn [& args]
                      (if (every? map? args)
                        (apply deep-merge args)
                        (last args)))
         maps))


(defn assoc-if-not=
  "Assoc if the new value `v` ist not `=` to the current value in `m`."
  [m k v]
  (if (= v (get m k))
    m
    (assoc m k v)))


(defn assoc-if-missing
  "Assoc if `m` is missing key `k`."
  [m k v]
  (if (= ::missing (get m k ::missing))
    (assoc m k v)
    m))


;;; promises

(defn transform>
  "Returns `js/Promise` which resolves the application of `transform` on the
  value of `promise`.

  Use this if you have a promise which value needs to be transformed."
  [promise transform]
  (js/Promise.
   (fn [resolve reject]
     (-> promise
         (.then (fn [result]
                  (let [transformed (transform result)]
                    (resolve transformed)))
                reject)))))


(defn p-transformed
  "Wraps promise function `f>` with `transform` function.

  Use this if you have a promise function which value needs to be transformed."
  [f> transform]
  (fn [& args]
    (transform> (apply f> args) transform)))


(defn chain-promise-fns> [input-value fns]
  (if-let [fn> (first fns)]
    (-> (fn> input-value)
        (.then #(chain-promise-fns> % (rest fns))))
    (js/Promise.resolve input-value)))


(defn apply>
  "Returns `js/Promise` with the result of applying `f` on `args` while
  resolving all promises in `args`."
  [f args]
  (s/assert vector? args)
  (-> (js/Promise.all args)
      (.then #(js/Promise.resolve (apply f %)))))


(comment
  (instance? js/Promise (js/Promise. (fn [_ _])))
  (let [sum (fn [a b c] (+ a b c))]
    (js/console.log "direct invocation:" (sum 1 2 3))
    (-> (apply> sum [1 2 3])
        (.then #(js/console.log "promise result #1:" %)))))
