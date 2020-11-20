(ns radar.ui
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]
   [spa.amazon :as amazon]

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
  (let [radar-id (use-radar-id)]
    ($ mui/Card
       ($ mui/CardActionArea
          {:component ui/Link
           :to (str "/ui/radars/" radar-id "/book/" (api/doc-id book))}
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
        {:key (api/doc-id book)
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
       ($ mui/Button
          {:onClick #(book-ui/show-book-form (api/doc-id radar) nil)
           :variant "contained"
           :color "secondary"}
          "New Book")
       ($ ui/Stack
          (for [section sections]
            ($ Section
               {:key (-> section :idx)
                :section section
                :books (get books-by-section-key (-> section :key))}))
          ))))


(defnc RadarPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radar)))


(defnc BookPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ book-ui/Book)))
