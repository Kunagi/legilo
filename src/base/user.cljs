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
   (-> user :auth-email)))


(defn best-photo-url [user]
  (when user
    (or (-> user :auth-photo-url)
        (str "https://www.gravatar.com/avatar/"
             (md5 (-> user :auth-email))
             "?d=retro"))))


(comment
  (def user {:auth-email "wi@koczewski.de"})
  (md5 "test"))
