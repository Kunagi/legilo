(ns radar.book-ui
  (:require
   [clojure.string :as str]
   ["@material-ui/core" :as mui]

   [commons.context :as c.context]
   [commons.mui :as cui :refer [defnc $ <> div]]

   [base.user :as user]

   [openlibrary.service :as openlibrary-service]
   [amazon.service :as amazon-service]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.service :as service]
   [radar.context :as context]

   [clojure.string :as str]))


(defn format-text [s]
  (-> s
      (str/split #"\n\n")
      (->> (map-indexed (fn [idx e]
                          ($ :p
                             {:key idx}
                             (-> e
                                 (str/split #"\n")
                                 (->> (map-indexed (fn [idx e]
                                                     (<>
                                                        {:key idx}
                                                        e
                                                        ($ :br))))))))))))


(defnc Review [{:keys [review]}]
  (let [user (c.context/use-doc ["users" (-> review :uid)])]
    ($ :div
       ($ :div
          {:className "Recommendation"
           :style {:display :flex
                   :place-content :stretch
                   :place-items :stretch}}
          ($ :div
             {:style {:padding "4px"}}
             ($ mui/Avatar
                {:src (user/best-photo-url user)
                 :alt (user/best-display-name user)}))
          ($ mui/Card
             {:className "flex-grow-1 ml-1"}
             ($ mui/CardContent
                ($ cui/Stack
                   ($ :div
                      {:style {:color "#666"
                               :font-style "italic"}}
                      (user/best-display-name user))
                   ($ :div
                      (format-text (-> review :text))))))))))


(defn start-review [radar book uid text]
  (cui/show-form-dialog
   {:fields [{:id :text
              :value text
              :rows 5
              :multiline? true}]
    :submit #(service/update-review-text> radar book uid (assoc % :uid uid))}))

(defnc OwnReview [{:keys [review]}]
  (let [radar (context/use-radar)
        book-id (context/use-book-id)
        book (radar/book-by-id radar book-id)
        uid (context/use-uid)
        recommended? (service/book-recommended-by-user? book uid)]
    ($ :div
       ($ :div
          {:className "Recommendation"
           :style {:display :flex
                   :place-content :stretch
                   :place-items :stretch}}
          ($ :div
             (if recommended?
               ($ cui/IconButton
                  {:command book/recommend
                   :onClick #(service/un-recommend-book> radar book uid)
                   :icon "thumb_up"
                   :color "secondary"})
               ($ cui/IconButton
                  {:command book/recommend
                   :onClick #(service/recommend-book> radar book uid)
                   :theme "outlined"})))
          ($ mui/Card
             {:className "flex-grow-1 ml-1"}
             ($ mui/CardActionArea
                {:onClick #(start-review radar book uid (-> review :text))}
                ($ mui/CardContent
                   (if (-> review :text)
                     ($ :div
                        (-> review :text format-text))
                     ($ :p {:style {:color "grey"
                                      :font-style "italic"}}
                        (if recommended?
                          "Leave a review?"
                          "Recommend this book?"))))))))))

(defnc Reviews []
  (let [{:keys [radar book-id uid]} (c.context/use-context-data)
        book (radar/book-by-id radar book-id)
        reviews (-> book :reviews vals)
        reviews-grouped (->> reviews (group-by #(= uid (-> % :uid))))
        own-review (first (get reviews-grouped true))
        other-reviews (get reviews-grouped false)]
    ($ cui/Stack
       ($ OwnReview {:review own-review})
       (for [review other-reviews]
         ($ Review
            {:key (-> review :uid)
             :review review}))
       #_(cui/data reviews-grouped))))


(defn counter [book]
  (let [c (service/book-recommendation-count book)]
    ($ :div
       {:style {:margin "0 auto"
                :text-align "center"}}
       ($ :div
          {:style {:font-weight 900
                   :font-size "300%"}}
          c)
       ($ :div
          (if (= c 1)
            " Recommendation"
            " Recommendations")))))


(defnc Book [{:keys []}]
  (let [radar (context/use-radar)
        book-id (context/use-book-id)
        book (radar/book-by-id radar book-id)
        isbn (-> book :isbn)
        asin (-> book :asin)
        image-url (cond
                    isbn (openlibrary-service/cover-url-by-isbn isbn)
                    asin (amazon-service/cover-url-by-asin asin)
                    :else nil)]
    ($ :div
       {:style {:display :grid
                :grid-template-columns "auto minmax(100px,200px)"
                :grid-gap "8px"}}

       ($ cui/Stack

          ($ :div
             ($ mui/Card
                ($ cui/FormCardArea
                   {:form {:fields [book/title book/author book/isbn book/asin book/tags]
                           :values book
                           :submit #(service/update-book> radar book %)}}
                   ($ mui/CardContent
                      ($ :div (-> book :author))
                      ($ :h2 (-> book :title))
                      ($ cui/StringVectorChips {:values (-> book :tags)})
                      ))))

          #_($ cui/FieldsCard
             {:entity book
              :update-f #(service/update-book> radar book %)
              :fields [book/title book/author book/isbn book/asin book/tags]})

          ($ Reviews))

       ($ :div
          ($ cui/Stack
             {:spacing 3}

             (when image-url
               ($ :img
                  {:src image-url
                   :referrer-policy "no-referrer"
                   :class "MuiPaper-root MuiPaper-elevation1 MuiPaper-rounded"
                   :style {:margin "0 auto"}}))

             (counter book)

             ($ cui/Stack

                ($ cui/Button
                   {:command book/view-on-amazon
                    :href (if-let [asin (-> book :asin)]
                            (amazon-service/href asin)
                            (amazon-service/search-href (or isbn (-> book :title))))
                    :target :_blank
                    :color "secondary"}))

             )))))
