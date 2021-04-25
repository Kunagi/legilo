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
  (let [bucket   (-> admin .storage (.bucket "legilo-backups"))
        file     (-> bucket (.file path))
        writable (-> file .createWriteStream)]
    (-> writable (.write value))
    (-> writable .end)))

(defn write-doc [path doc]
  (let [path (str path "/" (-> doc :firestore/path) ".edn")
        s    (u/->edn doc)]
    (write-string path s)))

(defn backup-col [path col-name]
  (u/=> (firestore/col> [col-name])
        (fn [docs]
          (doseq [doc docs]
            (write-doc path doc)))))

(defn backup-all []
  (let [path (-> (js/Date.) .toISOString)]
    (->> ["radars" "users"]
         (map #(backup-col path %))
         doall)))

(defn handle-on-backup> [^js _req]
  (u/resolve> (backup-all)))

(comment
  (backup-all)
  (backup-col "test" "radars"))

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
