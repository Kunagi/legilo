(ns legilo.utils)


;;; time & date

(defn current-time-millis []
  (-> (js/Date.) .getTime))


(defn prefix-with-0 [i]
  (if (< i 10)
    (str "0" i)
    (str i)))

(defn date->date-iso [date]
  (when date
    (str (-> date .getFullYear)
         "-"
         (-> date .getMonth inc prefix-with-0)
         "-"
         (-> date .getDate prefix-with-0))))

(defn today-as-iso []
  (date->date-iso (js/Date.)))

(def workday-start-hour 5)

(defn workday-as-iso
  "Liefert das Datum des aktuellen Arbeitstages."
  []
  (let [date (js/Date.)
        date (if (< (-> date .getHours) workday-start-hour)
               (js/Date. (-> date .getTime (- (* 1000 60 60 24))))
               date)]
    (date->date-iso date)))


(defn uhrzeit-sm []
  (let [date (js/Date.)]
    (str (prefix-with-0 (-> date .getHours))
         ":"
         (prefix-with-0 (-> date .getMinutes)))))

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
                    (resolve transformed))))
         (.catch reject)))))


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
