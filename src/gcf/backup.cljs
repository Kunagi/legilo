(ns gcf.backup
  (:require
   ["firebase-admin" :as admin]
   [spark.firestore :as firestore]
   [spark.utils :as u]
   [spark.gcf :as gcf]))

(js/console.log "hello")


(comment
  (u/=> (firestore/col> [ "radars"])
        (fn [result]
          (js/console.log "DEBUG-1" result))))

(defn write-string [path value]
  (js/console.log "write-string" path)
  (let [bucket   (-> admin .storage (.bucket "gs://legilo-backups"))
        file     (-> bucket (.file path))
        writable (-> file .createWriteStream)]
    (-> writable (.write value))
    (-> writable .end)
    path))

(comment
  (sequential? [])
  (write-string "test.txt" "test"))



(defn write-doc [path doc]
  (let [path (str path "/" (-> doc :firestore/path) ".edn")
        s    (u/->edn doc)]
    (write-string path s)))

(defn backup-col> [path col-name]
  (u/=> (firestore/col> [col-name])
        (fn [docs]
          (->> docs
               (map #(write-doc path %))
               doall))))

(defn backup-all> []
  (let [path (str (if goog.DEBUG "dev" "prod")
                  "_"
                  (-> (js/Date.) .toISOString))]
    (u/all>
     (->> ["radars" "users"]
          (map #(backup-col> path %))))))

(defn handle-on-backup> [^js _req]
  (u/=> (backup-all>)
        clj->js))

(comment
  (backup-all>)
  (backup-col> "test" "radars"))

(comment
  (let [bucket   (-> admin .storage (.bucket "legilo-backups"))
        file     (-> bucket (.file "test/test.txt"))
        writable (-> file .createWriteStream)]
    (-> writable (.write "hello world"))
    (-> writable .end)))

(defn exports []
  {

   :backup
   (gcf/on-request--format-output> handle-on-backup>)
   ;; http://localhost:5001/legilo/europe-west1/backup

   })
