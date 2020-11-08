(ns spa.radar
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]))


(defn use-radar-id []
  (-> (ui/use-params) :radarId))


(defn use-radar []
  (ui/use-doc ["radars" (use-radar-id)]))


(defn use-books []
  (ui/use-col ["radars" (use-radar-id) "books"]))


(defnc Book[{:keys [book]}]
  ($ mui/Card
     ($ mui/CardContent
        (-> book :title))))


(defnc Radar []
  (let [radar (use-radar)
        books (use-books)]
    ($ ui/Stack
     ($ mui/Typography
        {:variant "h4"
         :component "h2"}
        (-> radar :name))
     ($ ui/Stack
        (for [book books]
          ($ Book
             {:key (api/doc-id book)
              :book book}))))))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radar)))
