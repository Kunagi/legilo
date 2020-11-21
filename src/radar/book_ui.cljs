(ns radar.book-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.mui :refer [defnc $ <> div]]
   [base.ui :as ui]

   [base.context :as context]

   [amazon.ui :as amazon]

   [radar.book :as book]
   [radar.service :as service]
   ))



(defn use-radar-id []
  (-> (ui/use-params) :radarId))


(defn use-book-id []
  (-> (ui/use-params) :bookId))


(defn use-book []
  (ui/use-doc ["radars" (use-radar-id) "books" (use-book-id)]))



;;; Forms


(defn book-form [radar-id book]
  {:fields [book/title book/asin]
   :submit (fn [inputs]
             (if book
               (service/update-book> book inputs)
               (service/add-book> radar-id inputs)))})


(defn show-book-form [radar-id book]
  (ui/show-form-dialog (book-form radar-id book)))


(defnc Book [{:keys []}]
  (let [book (use-book)
        uid (context/use-uid)]
    ($ mui/Card
       ($ ui/EditableFieldCardActionArea
          {:doc book
           :field book/title})
       ($ ui/CardRow
          ($ ui/EditableFieldCardActionArea
             {:doc book
              :field book/isbn})
          ($ ui/EditableFieldCardActionArea
             {:doc book
              :field book/asin})
          (when-let [asin (-> book :asin)]
            (amazon/ImageLink asin)))
       ($ ui/EditableFieldCardActionArea
          {:doc book
           :field book/tags})
       ($ mui/CardContent
          ($ ui/Stack
             ;; ($ amazon/SearchWidget {:title (-> book :title)})
             ;; (ui/data book)
             (div
              (service/book-recommendation-count book))
             ($ ui/Flexbox
                ($ mui/Button
                   {:onClick #(service/recommend-book> uid book)
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
