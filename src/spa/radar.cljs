(ns spa.radar
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]))


(defn book-recommendation-count [book]
  (-> book :recommendations count))


;;; Commands


(defn recommend-book [uid book]
  (api/update-doc> book {:recommendations (api/update--array-union [uid])}))


;;; UI State


(defn use-radar-id []
  (-> (ui/use-params) :radarId))


(defn use-radar []
  (ui/use-doc ["radars" (use-radar-id)]))


(defn use-books []
  (ui/use-col ["radars" (use-radar-id) "books"]))


;;; UI Rendering


(defnc Book[{:keys [book]}]
  (let [uid (context/use-uid)]
    ($ mui/Card
       ($ mui/CardContent
          ($ ui/Stack
             (-> book :title)
             (ui/data book)
             (div
              (book-recommendation-count book))
             ($ mui/Button
                {:onClick #(recommend-book uid book)
                 :variant "contained"
                 :color "secondary"}
                "I recommend this book"))))))


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
