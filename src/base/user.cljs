(ns base.user
  (:require
   ["md5" :as md5]
   [commons.models :as m :refer [def-model]]))



(def-model User
  [m/Doc
   {}])


(def-model Users
  [m/Col
   {:doc User}])


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
