(ns spa.home
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]))


(defn use-users []
  (ui/use-col ["users"]))


(defnc UserCard [{:keys [user]}]
  ($ mui/Card
     ($ mui/CardContent
        (ui/data user))))

(defnc Users[]
  (let [users (use-users)
        user (context/use-user)]
    ($ ui/Stack
       (div "You are " (if user (-> user :email) "Anonymous") "!")
       (when user
         ($ ui/Stack
            (div "And here comes all the other users:")
            (for [user users]
              ($ UserCard
                 {:key (api/doc-id user)
                  :user user})))))))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Users)))
