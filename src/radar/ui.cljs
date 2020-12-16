(ns radar.ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.mui :as cui :refer [defnc $ <> div]]

   [base.ui :as ui]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.repository :as repository]
   [radar.service :as service]
   [radar.context :as context]
   [radar.book-ui :as book-ui]
   ))


;;; UI Rendering


(defnc Book[{:keys [book]}]
  (let [radar-id (context/use-radar-id)
        book-id (-> book :id)]
    ($ mui/Card
       ($ mui/CardActionArea
          {:component ui/Link
           :to (str "/ui/radars/" radar-id "/book/" book-id)}
          ($ mui/CardContent
             (-> book :title))))))


(defnc Section [{:keys [section books]}]
  ($ ui/Stack
   ($ mui/Typography
      {:component "h3"
       :variant "h5"}
      (-> section :name))
   (for [book (->> books (sort-by service/book-recommendation-count) reverse)]
     ($ Book
        {:key (-> book :id)
         :book book}))))


(defnc Radar []
  (let [radar (context/use-radar)
        books (radar/books radar)]
    ($ ui/Stack
       ($ mui/Typography
          {:variant "h4"
           :component "h2"}
          (-> radar :title))
       ($ cui/Button
          {:command #(service/add-book-command radar)
           :color "secondary"})
       ($ ui/Stack
          (for [section radar/sections]
            ($ Section
               {:key (-> section :idx)
                :section section
                :books (get (radar/books-by-section-key books) (-> section :key))}))
          (when (empty? books)
            ($ cui/Button
               {:command (service/add-example-books-command radar)}))))))


(defnc RadarPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radar)))


(defnc BookPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ book-ui/Book)))


;;;
;;; Radar Config
;;;


(defnc MenuIcon []
  (let [radar-id (context/use-radar-id)]
    ($ mui/IconButton
       {:component ui/Link
        :to (str "/ui/radars/" radar-id "/config")}
       ($ :div {:class "i material-icons"} "settings"))))


(defnc RadarConfigCard []
  (let [radar (context/use-radar)]
    ($ cui/FieldsCard
       {:entity radar
        :update-f #(service/update-radar> radar %)
        :fields [radar/title radar/allow-domain]})))


(defnc RadarConfigPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ RadarConfigCard)))
