(ns radar.radars-page
  (:require
   ["@material-ui/core" :as mui]

   [spark.logging :refer [log]]

   [spark.ui :as ui :refer [def-ui def-page $]]

   [radar.radar :as radar]
   [radar.queries :as queries]
   [radar.commands :as commands]))


(def-ui Radar [radar]
  ($ mui/Card
     ($ mui/CardActionArea
        {:component ui/Link
         :to (str "/ui/radars/" (-> radar :firestore/id))}
        ($ mui/CardContent
           (-> radar :title)))))

(def-ui Radars [uid user]
  {:from-context [uid user]}
  (let [radars (ui/use-query queries/radars-for-user {:user user})]
    ($ ui/Stack
       (when user
         ($ ui/Flexbox
            ($ ui/CommandButton
               {:command commands/create-radar
                :context {:uid uid}})))
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
