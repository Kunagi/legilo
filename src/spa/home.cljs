(ns spa.home
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]))


(defnc Radar [{:keys [radar]}]
  ($ mui/Card
     ($ mui/CardActionArea
        {:component ui/Link
         :to (str "/ui/radars/" (api/doc-id radar))}
        ($ mui/CardContent
           (-> radar :name)))))


(defn use-radars []
  (ui/use-col ["radars"]))


(defnc Radars []
  (let [radars (use-radars)]
    ($ ui/Stack
     (for [radar radars]
       ($ Radar
          {:key (api/doc-id radar)
           :radar radar})))))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radars)))
