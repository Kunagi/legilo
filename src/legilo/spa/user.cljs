(ns legilo.spa.user
  (:require
   ["@material-ui/core" :as mui]

   [legilo.utils :as u]

   [legilo.spa.api :as api :refer [log]]
   [legilo.spa.context :as context]
   [legilo.spa.ui :as ui :refer [defnc $ <>]]
   ))


(defonce CURRENT_UID (atom nil))

(defn update-last-usage [uid]
  (log ::update-last-usage :uid uid)
  (-> (api/update-doc> ["users" uid]
                       {:last-usage (api/update--timestamp)})))


(add-watch context/USER ::update
           (fn [_k _r _ov ^js user]
             (when user
               (let [uid (-> user .-uid)]
                 (reset! CURRENT_UID uid)
                 (update-last-usage uid)))))
