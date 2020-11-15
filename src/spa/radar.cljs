(ns spa.radar
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]
   [spa.book :as book]
   [spa.amazon :as amazon]))





;;; UI State


(defn use-radar-id []
  (-> (ui/use-params) :radarId))


(defn use-radar []
  (ui/use-doc ["radars" (use-radar-id)]))


(defn use-books []
  (ui/use-col ["radars" (use-radar-id) "books"]))


;;; UI Rendering


(defnc Book[{:keys [book]}]
  (let [uid (context/use-uid)
        radar-id (use-radar-id)]
    ($ mui/Card
       ($ mui/CardActionArea
          {:component ui/Link
           :to (str "/radars/" radar-id "/book/" (api/doc-id book))}
          ($ mui/CardContent
             (-> book :title))))))


(defnc Radar []
  (let [radar (use-radar)
        books (use-books)]
    ($ ui/Stack
     ($ mui/Typography
        {:variant "h4"
         :component "h2"}
        (-> radar :name))
     ($ mui/Button
        {:onClick #(book/show-book-form (api/doc-id radar) nil)
         :variant "contained"
         :color "secondary"}
        "New Book")
     ($ ui/Stack
        (for [book (->> books (sort-by book/book-recommendation-count) reverse)]
          ($ Book
             {:key (api/doc-id book)
              :book book}))))))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radar)))
