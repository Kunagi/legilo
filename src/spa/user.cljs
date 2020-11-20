(ns spa.user
  (:require
   ["@material-ui/core" :as mui]

   [commons.firestore :as firestore]
   [spa.api :as api :refer [log]]
   [spa.context :as context]
   [spa.ui :as ui :refer [defnc $ <>]]
   ))


(defn update-last-usage [uid email display-name photo-url phone-number]
  (log ::update-last-usage
       :uid uid
       :email email)
  (firestore/load-and-save>
   ["users" uid]
   #(merge % {:last-usage (firestore/timestamp)
              :auth-email email
              :auth-display-name display-name
              :auth-photo photo-url
              :auth-phone phone-number})))


(add-watch context/USER
           ::update
           (fn [_k _r _ov ^js user]
             (when user
               (log ::user-changed
                    :user user)
               (update-last-usage (-> user :uid)
                                  (-> user :email)
                                  (-> user :displayName)
                                  (-> user :photoURL)
                                  (-> user :phoneNumber)))))
