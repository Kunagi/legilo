(ns legilo.spa.home
  (:require
   ["@material-ui/core" :as mui]

   [legilo.spa.api :as api :refer [log]]
   [legilo.spa.ui :as ui :refer [defnc $ <> div]]))


(defn use-users []
  (ui/use-col ["users"]))


(defnc UserCard [{:keys [user]}]
  ($ mui/Card
     ($ mui/CardContent
        (ui/data user))))

(defnc Users[]
  (let [users (use-users)]
    ($ ui/Stack
       (div "Here comes the users!")
       (for [user users]
         ($ UserCard
            {:key (api/doc-id user)
             :user user})))))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Users)))
