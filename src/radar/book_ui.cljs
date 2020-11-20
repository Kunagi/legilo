(ns radar.book-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.mui :refer [defnc $ <> div]]
   [base.ui :as ui]

   [base.context :as context]

   [amazon.ui :as amazon]

   [radar.service :as service]
   ))



(defn use-radar-id []
  (-> (ui/use-params) :radarId))


(defn use-book-id []
  (-> (ui/use-params) :bookId))


(defn use-book []
  (ui/use-doc ["radars" (use-radar-id) "books" (use-book-id)]))


;;; Commands




;;; Forms


(defn book-title-field [book]
  {:id :title
   :label "Book Title"
   :value (-> book :title)})


(defn book-asin-field [book]
  {:id :asin
   :label "ASIN"
   :value (-> book :asin)})


(defn tags-field [book]
  {:id :tags
   :label "Tags"
   :value (-> book :tags)
   :type "chips"})


(defn book-form [radar-id book]
  {:fields [(book-title-field book)
            (book-asin-field book)]
   :submit (fn [inputs]
             (service/update-book radar-id book inputs))})


(defn show-book-form [radar-id book]
  (ui/show-form-dialog (book-form radar-id book)))


(defnc Book [{:keys []}]
  (let [book (use-book)
        uid (context/use-uid)]
    ($ mui/Card
       ($ ui/EditableFieldCardActionArea
          {:doc book
           :field (book-title-field book)})
       ($ ui/CardRow
          ($ ui/EditableFieldCardActionArea
             {:doc book
              :field {:id :isbn
                      :label "ISBN"}})
          ($ ui/EditableFieldCardActionArea
             {:doc book
              :field (book-asin-field book)})
          (when-let [asin (-> book :asin)]
            (amazon/ImageLink asin)))
       ($ ui/EditableFieldCardActionArea
          {:doc book
           :field (tags-field book)})
       ($ mui/CardContent
          ($ ui/Stack
             ;; ($ amazon/SearchWidget {:title (-> book :title)})
             ;; (ui/data book)
             (div
              (service/book-recommendation-count book))
             ($ ui/Flexbox
                ($ mui/Button
                   {:onClick #(service/recommend-book uid book)
                    :variant "contained"
                    :color "secondary"
                    :startIcon (ui/icon "thumb_up")}
                   "I recommend this book")
                ($ mui/Button
                   {:href (if-let [asin (-> book :asin)]
                            (amazon/href asin)
                            (amazon/search-href (-> book :title)))
                    :target :_blank
                    :variant "contained"
                    :color "secondary"
                    :startIcon (ui/icon "shopping_cart")}
                   "View on Amazon")))))))
