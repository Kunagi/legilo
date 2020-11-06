(ns legilo.spa.user
  (:require
   ["@material-ui/core" :as mui]

   [legilo.utils :as u]

   [legilo.spa.api :as api :refer [log]]
   [legilo.spa.context :as context]
   [legilo.spa.ui :as ui :refer [defnc $ <>]]
   ))


(defn update-last-usage [user]
  (log ::update-last-usage :user user)
  (-> (api/update-doc> ["users" (-> user :uid)]
                       {:uid (-> user :uid)
                        :email (-> user :email)
                        :display-name (-> user :display-name)
                        :last-usage (api/update--timestamp)})))


(add-watch context/USER ::update
           (fn [_k _r _ov user]
             (when user
               (update-last-usage user))))
