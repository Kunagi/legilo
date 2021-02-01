(ns radar.book-ui
  (:require
   [clojure.string :as str]
   ["@material-ui/core" :as mui]

    
   [commons.mui :as cui :refer [defnc $ <>]]

   [base.user :as user]

   [amazon.service :as amazon-service]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.commands :as commands]
))


(defn use-book-id []
  (cui/use-param :book-id))

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
  (let [user (cui/use-doc ["users" (-> review :uid)])]
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


(defnc OwnReview [{:keys [review]}]
  (let [{:keys [radar]}(cui/use-context-data)
        book-id (use-book-id)
        book (radar/book-by-id radar book-id)
        uid (cui/use-uid)
        recommended? (book/recommended-by-user? book uid)]
    ($ :div
       ($ :div
          {:className "Recommendation"
           :style {:display :flex
                   :place-content :stretch
                   :place-items :stretch}}
          ($ :div
             (if recommended?
               ($ cui/CommandButton
                  {:command commands/UnRecommendBook
                   :context {:book book
                             :uid uid}
                   :as-icon? true
                   :color "secondary"})
               ($ cui/CommandButton
                  {:command commands/RecommendBook
                   :context {:book book
                             :uid uid}
                   :as-icon? true
                   :icon-theme "outlined"})))
          ($ mui/Card
             {:className "flex-grow-1 ml-1"}
             ($ cui/CommandCardArea
                {:command commands/UpdateBookReview
                 :context {:book book
                           :uid uid}}
                ($ mui/CardContent
                   (if (-> review :text)
                     ($ :div
                        {:style {:font-size "0.875rem"
                                 :line-height "1.43"}}
                        (-> review :text format-text))
                     ($ :p {:style {:color "grey"
                                      :font-style "italic"}}
                        (if recommended?
                          "Leave a review?"
                          "Recommend this book?"))))))))))


(defn counter [book]
  (let [c (book/recommendation-count book)]
    ($ :span
       {:style {}}
       ($ :span
          {:style {:font-weight 100}}
          c)
       ($ :span
          {:style {:font-weight 100
                   }}
          (if (= c 1)
            " recommendation"
            " recommendations")))))


(defnc Reviews []
  (let [{:keys [radar book-id uid]} (cui/use-context-data)
        book (radar/book-by-id radar book-id)
        reviews (-> book :reviews vals)
        reviews-grouped (->> reviews (group-by #(= uid (-> % :uid))))
        own-review (first (get reviews-grouped true))
        other-reviews (get reviews-grouped false)]
    ($ cui/Stack
       ($ :h4
          "What I say")
       ($ OwnReview {:review own-review})
       ($ :h4
          "What others say | "
          (counter book))
       (if (seq other-reviews)
         (for [review other-reviews]
           ($ Review
              {:key (-> review :uid)
               :review review}))
         ($ :div
            {:style {:color "grey"
                     :font-style "italic"}}
            "no reviews yet")))))


(defnc Book [{:keys []}]
  (let [{:keys [radar]}(cui/use-context-data)
        book-id (use-book-id)
        book (radar/book-by-id radar book-id)
        isbn (-> book :isbn)
        image-url (book/cover-url book)

        BookDataCard ($ mui/Card
                        ($ cui/CommandCardArea
                           {:command commands/UpdateBook
                            :context {:book book}}
                           ($ mui/CardContent
                              ($ :div (-> book :author))
                              ($ :h2 (-> book :title))
                              ))
                        ($ mui/Divider)
                        ($ cui/CommandCardArea
                           {:command commands/UpdateBookTags
                            :context {:book book}}
                           ($ mui/CardContent
                              ($ cui/Stack
                                 ($ cui/FieldLabel
                                    {:text "Tags"})
                                 ($ cui/StringVectorChips {:values (book/tags-in-order book)})))))

        Cover (when image-url
                ($ :img
                   {:src image-url
                    :referrer-policy "no-referrer"
                    :class "MuiPaper-root MuiPaper-elevation1 MuiPaper-rounded"
                    :style {:margin "0 auto"
                            :max-width "30vw"}}))

        AmazonBuyButton ($ cui/Button
                           {:command book/view-on-amazon
                            :href (if-let [asin (-> book :asin)]
                                    (amazon-service/href asin)
                                    (amazon-service/search-href (or isbn (-> book :title))))
                            :target :_blank
                            :color "secondary"})]

    ($ cui/Stack

       ($ :div
          {:style {:display :flex}}

          ($ :div
             {:style {:flex "2"
                      :margin-right "8px"}}
             BookDataCard)

          ($ :div
             {:style {:flex "1"}}
             ($ cui/Stack
                Cover
                AmazonBuyButton

                )))
       #_(counter book)

       ($ Reviews)
       )))
