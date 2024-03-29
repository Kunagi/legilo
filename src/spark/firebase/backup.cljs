(ns spark.firebase.backup
  (:require
   [tick.locale-en-us]
   [tick.timezone]
   [tick.core :as tick]
   ;; [tick.format :as tick.format]
   ["firebase-admin" :as admin]
   [spark.firestore :as firestore]
   [spark.utils :as u]
   [spark.gcf :as gcf]))

(defn file [bucket path]
  (-> ^js bucket (.file path)))

(comment
  (u/=> (firestore/col> [ "radars"])
        (fn [result]
          (js/console.log "DEBUG-1" result))))

(defn write-string> [bucket path value]
  (u/promise>
   (fn [resolve _reject]
     (let [f        (file bucket path)
           writable (-> ^js f .createWriteStream)]
       (-> writable ^js (.on "finish" #(resolve path)))
       (-> writable ^js (.write value))
       (-> writable ^js .end)
       ))))

(defn download-string [bucket path]
  (let [file (file bucket path)]
    (-> ^js file .download)))

(defn write-doc> [bucket path doc]
  (let [path (str path "/" (-> doc :firestore/path) ".edn")
        s    (u/->edn doc)]
    (write-string> bucket path s)))

(defn backup-col> [bucket path col-name]
  (u/=> (firestore/col> [col-name])
        #(u/all> (map (partial write-doc> bucket path) %))))

(defn backup-cols> [bucket path cols-names]
  (u/all>
   (map #(backup-col> bucket path %)
        cols-names)))

(def date-path-format (tick/formatter "yyyy/MM/dd"))

(defn date-path []
  (let [now  (tick/now)
        date (tick/date now)]
    (str
     (tick/format date-path-format date)
     "/"
     (tick/now))))

(defn bucket [bucket-name]
  (-> ^js admin .storage (.bucket (str "gs://" bucket-name))))

(defn backup-all> [bucket-name]
  (let [bucket (bucket bucket-name)
        path   (str (if goog.DEBUG "dev" "prod")
                    "/"
                    (date-path))]
    (u/=> (firestore/cols-names>)
          (fn [cols-names]
            (backup-cols> bucket path cols-names)))))

(comment
  (-> (backup-all> "legilo-backups")
      u/tap>))


(defn handle-on-backup> [bucket-name ^js _req]
  (backup-all> bucket-name))

(defn exports [bucket-name]
  {

   :backup
   (gcf/on-request--format-output> (partial handle-on-backup> bucket-name))

   })
