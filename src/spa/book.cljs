(ns spa.book
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]
   [spa.amazon :as amazon]))


(defn book-recommendation-count [book]
  (-> book :recommendations count))

(defn use-radar-id []
  (-> (ui/use-params) :radarId))


(defn use-book-id []
  (-> (ui/use-params) :bookId))


(defn use-book []
  (ui/use-doc ["radars" (use-radar-id) "books" (use-book-id)]))


;;; Commands


(defn recommend-book [uid book]
  (api/update-doc> book {:recommendations (api/update--array-union [uid])}))


(defn update-book [radar-id book changes]
  (api/update-doc>
   (or book ["radars" radar-id "books" (str (random-uuid))])
   changes))


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
   :type :chips})


(defn book-form [radar-id book]
  {:fields [(book-title-field book)
            (book-asin-field book)]
   :submit (fn [inputs]
             (update-book radar-id book inputs))})


(defn show-book-form [radar-id book]
  (ui/show-form-dialog (book-form radar-id book)))


(defnc Book[{:keys []}]
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

             ;; (ui/data book)
             (div
              (book-recommendation-count book))
             ($ mui/Button
                {:onClick #(recommend-book uid book)
                 :variant "contained"
                 :color "secondary"}
                "I recommend this book"))))))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Book)))
