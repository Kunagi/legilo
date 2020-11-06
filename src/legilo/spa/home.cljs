(ns legilo.spa.home
  (:require
   ["@material-ui/core" :as mui]

   [legilo.spa.api :as api :refer [log]]
   [legilo.spa.ui :as ui :refer [defnc $ <> div]]
   [legilo.spa.context :as context]
   ))


(defn use-users []
  (ui/use-col ["users"]))


(defnc UserCard [{:keys [user]}]
  ($ mui/Card
     ($ mui/CardContent
        (ui/data user))))

(defnc Users[]
  (let [users (use-users)
        uid (context/use-uid)]
    ($ ui/Stack
       (div "You are " (if uid uid "Anonymous") "!")
       (div "And here comes all the other users:")
       (for [user users]
         ($ UserCard
            {:key (api/doc-id user)
             :user user})))))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Users)))
