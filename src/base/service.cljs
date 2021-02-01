(ns base.service
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.firestore :as firestore]
   [commons.auth :as c.auth]

   [commons.context :as c.context]))


(defn update-last-usage [uid email display-name photo-url phone-number]
  (log ::update-last-usage
       :uid uid
       :email email)
  (firestore/load-and-save>
   ["users" uid]
   #(merge % {:last-usage (firestore/timestamp)
              :id uid
              :uid uid
              :auth-email email
              :auth-domain (when email
                             (-> email (.substring (-> email (.indexOf "@") inc))))
              :auth-display-name display-name
              :auth-photo photo-url
              :auth-phone phone-number})))


(defn set-user [^js user]
  (log ::user-changed :user user)
  (update-last-usage
   (-> user .-uid)
   (-> user .-email)
   (-> user .-displayName)
   (-> user .-photoURL)
   (-> user .-phoneNumber)))


(c.auth/initialize
 {:set-user set-user
  :sign-in c.auth/sign-in-with-microsoft})
