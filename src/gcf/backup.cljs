(ns gcf.backup
  (:require
   ["firebase-admin" :as admin]
   [spark.firestore :as firestore]
   [spark.utils :as u]
   [spark.gcf :as gcf]))


(defn bucket []
  (-> ^js admin .storage (.bucket "gs://legilo-backups")))

(defn file [path]
  (let [bucket (bucket)
        file   (-> ^js bucket (.file path))]
    file))

(comment
  (u/=> (firestore/col> [ "radars"])
        (fn [result]
          (js/console.log "DEBUG-1" result))))

(defn write-string> [path value]
  (u/promise>
   (fn [resolve _reject]
     (let [f        (file path)
           writable (-> ^js f .createWriteStream)]
       (-> writable ^js (.on "finish" #(resolve path)))
       (-> writable ^js (.write value))
       (-> writable ^js .end)
       ))))

(defn download-string [path]
  (let [file (file path)]
    (-> ^js file .download)))

(comment
  (u/=> (write-string> "test.txt" "test")
        u/tap>)
  (u/=> (download-string "test.txt")
        (fn [^js buffer]
          (-> buffer .toString))
        u/tap>))



(defn write-doc> [path doc]
  (let [path (str path "/" (-> doc :firestore/path) ".edn")
        s    (u/->edn doc)]
    (write-string> path s)))

(defn backup-col> [path col-name]
  (u/=> (firestore/col> [col-name])
        #(u/all> (map (partial write-doc> path) %))))

(defn backup-all> []
  (let [path (str (if goog.DEBUG "dev" "prod")
                  "_"
                  (-> (js/Date.) .toISOString))]
    (u/all>
     (->> ["radars" "users"]
          (map #(backup-col> path %))))))

(defn handle-on-backup> [^js _req]
  (backup-all>))

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
