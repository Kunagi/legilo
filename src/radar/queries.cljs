(ns radar.queries
  (:require

   [spark.core :refer [def-query def-test]]

   [base.user :as user]
   ))


;; (def-query radar
;;   {:path (fn [{:keys [id]}]
;;            (when id
;;              ["radars" id]))})


;; (def-test [radar execute-query>]
;;   (expect (fn [radar] (= "hello world" (-> radar :title)))
;;           (execute-query> radar {:id "10a8dd5c-2756-4fc1-80fc-ce9b65e389df"})))


;; (def-query radars-by-uid
;;   {:path (fn [{:keys [user]}]
;;            (when user
;;              [{:id "radars"
;;                :wheres [["uids" "!=" nil]
;;                         ["uids" "array-contains" (-> user user/id)]]}]))})

;; (def-test [radars-by-uid execute-query>]
;;   (execute-query> radars-by-uid {:user {:id "G4fCIVVCTpUxXMmht0s25jZMnoM2"}}))


;; (def-query radars-by-domain
;;   {:path (fn [{:keys [user]}]
;;            (when user
;;              (when-let [domain (-> user user/auth-domain)]
;;                [{:id "radars"
;;                  :wheres [["allow-domain" "==" domain]]}])))})

;; (def-test [radars-by-domain execute-query>]
;;   (execute-query> radars-by-domain {:user {:auth-domain "koczewski.de"}}))


(def-query radars-for-user
  {:paths (fn [{:keys [user]}]
            [
             (when user
               [{:id     "radars"
                 :wheres [["uids" "!=" nil]
                          ["uids" "array-contains" (-> user user/id)]]}])

             (when user
               (when-let [domain (-> user user/auth-domain)]
                 [{:id     "radars"
                   :wheres [["allow-domain" "==" domain]]}]))

             (when user
               (when-let [email (-> user user/auth-email)]
                 [{:id     "radars"
                   :wheres [["allow-emails" "array-contains" email]]}]))
             ])})


(def-test [radars-for-user execute-query>]
  (execute-query> radars-for-user {:user {:id "G4fCIVVCTpUxXMmht0s25jZMnoM2"
                                          :auth-domain "koczewski.de"}}))
