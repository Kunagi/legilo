(ns base.user
  (:require
   ["md5" :as md5]
   [spark.core :as spark :refer [def-doc]]
    ))


(def-doc User
  [{:firestore/collection "users"}])


(defn id [this]
  (-> this :id))

(defn auth-domain [this]
  (-> this :auth-domain))

(defn best-display-name [user]
  (or
   (-> user :auth-display-name)
   (-> user :auth-email)
   (-> user id)))

(defn auth-email [this]
  (-> this :auth-email))

(defn auth-photo-url [this]
  (-> this :auth-photo-url))

(defn gravatar-photo-url [this]
  (when-let [email (-> this auth-email)]
    (str "https://www.gravatar.com/avatar/"
         (md5 email)
         "?d=retro")))

(defn best-photo-url [this]
  (or (-> this auth-photo-url)
      (-> this gravatar-photo-url)))


(comment
  (md5 "test"))
