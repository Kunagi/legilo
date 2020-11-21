(ns radar.ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.mui :as cmui :refer [defnc $ <> div]]

   [base.ui :as ui]

   [radar.book :as book]
   [radar.repository :as repository]
   [radar.service :as service]
   [radar.book-ui :as book-ui]
   ))



;;; UI State


(defn use-radar-id []
  (-> (ui/use-params) :radarId))


(defn use-radar []
  (ui/use-doc ["radars" (use-radar-id)]))


(defn use-books []
  (ui/use-col ["radars" (use-radar-id) "books"]))


;;; UI Rendering


(defnc Book[{:keys [book]}]
  (let [radar-id (use-radar-id)
        book-id (-> book :firestore/id)]
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
        {:key (-> book :firestore/id)
         :book book}))))


(def sections
  [{:key :must-read
    :name "Must Read"
    :idx 0}
   {:key :should-read
    :name "Should Read"
    :idx 1}
   {:key :trial
    :name "Trial"
    :idx 2}
   {:key :to-read
    :name "To Read"
    :idx 3}
   ])


(defnc Radar []
  (let [radar (use-radar)
        radar-id (-> radar :firestore/id)
        books (use-books)
        books-by-section-key (->> books
                                  (group-by #(let [c (-> % :recommendations count)]
                                               (cond
                                                 (>= c 5) :must-read
                                                 (>= c 2) :should-read
                                                 (>= c 1) :trial
                                                 :else :to-read))))]
    ($ ui/Stack
       ($ mui/Typography
          {:variant "h4"
           :component "h2"}
          (-> radar :name))
       ($ cmui/Button
          {:command (service/add-book-command radar-id)
           :color "secondary"})
       ($ ui/Stack
          (for [section sections]
            ($ Section
               {:key (-> section :idx)
                :section section
                :books (get books-by-section-key (-> section :key))}))
          (when (empty? books)
            ($ cmui/Button
               {:text "Add example Books"
                :onClick #(service/add-example-books> (-> radar :firestore/id))}))))))


(defnc RadarPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radar)))


(defnc BookPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ book-ui/Book)))
