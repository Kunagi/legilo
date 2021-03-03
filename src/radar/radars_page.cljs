(ns radar.radars-page
  (:require
   ["@material-ui/core" :as mui]

   [spark.logging :refer [log]]
   [spark.auth :as auth]

   [spark.ui :as ui :refer [def-ui def-page $]]

   [base.user :as user]

   [radar.radar :as radar]
   [radar.commands :as radar-commands]))


;;;
;;; Radars
;;;


(def-ui Radar [radar]
  ($ mui/Card
     ($ mui/CardActionArea
        {:component ui/Link
         :to (str "/ui/radars/" (-> radar :firestore/id))}
        ($ mui/CardContent
           (-> radar :title)))))

(def-ui Radars [user]
  {:from-context [user]}
  (let [radars (ui/use-cols-union (radar/union-col-paths--for-user user))]
    ($ ui/Stack
       (when user
         ($ ui/Flexbox
            ($ ui/CommandButton
               {:command radar-commands/CreateRadar
                :context {:user user}})))
       (for [radar (->> radars (sort-by radar/title-in-lowercase))]
         ($ Radar
            {:key (-> radar :firestore/id)
             :radar radar})))))

(def-ui RadarsPageContent []
  (let [uid (ui/use-uid)]
    (when uid
      ($ Radars))))

(def-page radars-page
  {:path "/"
   :content RadarsPageContent})
