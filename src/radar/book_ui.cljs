(ns radar.book-ui
  (:require
   [clojure.string :as str]
   ["@material-ui/core" :as mui]

   [spark.ui :as ui :refer [def-ui $ <>]]

   [base.user :as user]

   [amazon.service :as amazon-service]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.commands :as commands]))


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

(def-ui Review [review]
  (let [user (ui/use-doc user/User (-> review :uid))]
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
                ($ ui/Stack
                   ($ :div
                      {:style {:color "#666"
                               :font-style "italic"}}
                      (user/best-display-name user))
                   ($ :div
                      (format-text (-> review :text))))))))))

(def-ui OwnReview [uid radar book review]
  {:from-context [uid radar book]}
  (let [recommended? (book/recommended-by-user? book uid)]
    ($ :div
       ($ :div
          {:className "Recommendation"
           :style {:display :flex
                   :place-content :stretch
                   :place-items :stretch}}
          ($ :div
             (if recommended?
               ($ ui/CommandButton
                  {:command commands/UnRecommendBook
                   :context {:book book
                             :uid uid}
                   :as-icon? true
                   :color "secondary"})
               ($ ui/CommandButton
                  {:command commands/RecommendBook
                   :context {:book book
                             :uid uid}
                   :as-icon? true
                   :icon-theme "outlined"})))
          ($ mui/Card
             {:className "flex-grow-1 ml-1"}
             ($ ui/CommandCardArea
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
          {:style {:font-weight 100}}
          (if (= c 1)
            " recommendation"
            " recommendations")))))

(def-ui Reviews [radar book uid]
  {:from-context [radar book uid]}
  (let [reviews (-> book :reviews vals)
        reviews-grouped (->> reviews (group-by #(= uid (-> % :uid))))
        own-review (first (get reviews-grouped true))
        other-reviews (get reviews-grouped false)]
    ($ ui/Stack
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

(def-ui Book [radar book]
  {:from-context [radar book]}
  (let [isbn (-> book :isbn)
        image-url (book/cover-url book)

        BookDataCard ($ mui/Card
                        ($ ui/CommandCardArea
                           {:command commands/UpdateBook
                            :context {:book book}}
                           ($ mui/CardContent
                              ($ :div (-> book :author))
                              ($ :h2 (-> book :title))))
                        ($ mui/Divider)
                        ($ ui/CommandCardArea
                           {:command commands/UpdateBookTags
                            :context {:book book}}
                           ($ mui/CardContent
                              ($ ui/Stack
                                 ($ ui/FieldLabel
                                    {:text "Tags"})
                                 ($ ui/StringVectorChips {:values (book/tags-in-order book)})))))

        Cover (when image-url
                ($ :img
                   {:src image-url
                    :referrer-policy "no-referrer"
                    :class "MuiPaper-root MuiPaper-elevation1 MuiPaper-rounded"
                    :style {:margin "0 auto"
                            :max-width "30vw"}}))

        AmazonBuyButton ($ ui/Button
                           {:text "Amazon"
                            :icon "shopping_cart"
                            :href (if-let [asin (-> book :asin)]
                                    (amazon-service/href asin)
                                    (amazon-service/search-href (or isbn (-> book :title))))
                            :target :_blank
                            :color "secondary"})]

    ($ ui/Stack

       ($ :div
          {:style {:display :flex}}

          ($ :div
             {:style {:flex "2"
                      :margin-right "8px"}}
             BookDataCard)

          ($ :div
             {:style {:flex "1"}}
             ($ ui/Stack
                Cover
                AmazonBuyButton)))

       #_(counter book)

       ($ Reviews))))
