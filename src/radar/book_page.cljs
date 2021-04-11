(ns radar.book-page
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   ["@material-ui/core" :as mui]

   [spark.ui :as ui :refer [def-ui def-page $ <>]]

   [base.user :as user]

   [amazon.service :as amazon-service]

   [radar.openlib-ui :as openlib]
   [radar.radar :as radar]
   [radar.book :as book]
   [radar.commands :as commands]
   [radar.ui :as radar-ui]))


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
           :style     {:display       :flex
                       :place-content :stretch
                       :place-items   :stretch}}
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
                      {:style {:color      "#666"
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
           :style     {:display       :flex
                       :place-content :stretch
                       :place-items   :stretch}}
          ($ :div
             (if recommended?
               ($ ui/CommandButton
                  {:command  commands/unrecommend-book
                   :context  {:radar radar
                              :book  book
                              :uid   uid}
                   :as-icon? true
                   :color    "secondary"
                   :class    "RecommendationButton"})
               (ui/div
                ($ ui/CommandButton
                   {:command    commands/recommend-book
                    :context    {:radar radar
                                 :book  book
                                 :uid   uid}
                    :as-icon?   true
                    :icon-theme "outlined"
                    :class      "RecommendationButton"
                    :styles     {:color "#aaa"}
                    }))))
          ($ mui/Card
             {:className "flex-grow-1 ml-1"}
             ($ ui/CommandCardArea
                {:command (when (or recommended?
                                    (-> review :text))
                            commands/update-book-review)
                 :context {:radar radar
                           :book  book
                           :uid   uid}}
                ($ mui/CardContent
                   (if (-> review :text)
                     ($ :div
                        {:style {
                                 ;; :font-size "0.875rem"
                                 ;; :line-height "1.43"
                                 }}
                        (-> review :text format-text))
                     ($ :p {:style {:color      "grey"
                                    :font-style "italic"}}
                        (if recommended?
                          "Leave a review?"
                          "Hit thumbs up to recommend this book."))))))))))

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
  (let [recommendations (->> book :recommendations (into #{}))
        reviews         (->> book :reviews vals
                             (remove #(not (contains? recommendations (-> % :uid))))
                             )
        reviews-grouped (->> reviews (group-by #(= uid (-> % :uid))))
        own-review      (first (get reviews-grouped true))
        other-reviews   (get reviews-grouped false)
        reviews-uids    (->> reviews
                             (map :uid)
                             (into #{}))
        recommendations (set/difference
                         recommendations
                         reviews-uids)]
    ($ ui/Stack
       ($ :h4
          "What I say")
       ($ OwnReview {:review own-review})
       ($ :h4
          "What others say | "
          (counter book))
       (if (seq other-reviews)
         (for [review (->> other-reviews
                           (sort-by (fn [review] [(-> review :ts-updated)
                                                  (-> review :uid)]))

                           (sort-by :uid))]
           ($ Review
              {:key    (-> review :uid)
               :review review}))
         ($ :div
            {:style {:color      "grey"
                     :font-style "italic"}}
            "no reviews yet"))
       ($ ui/Flexbox
          {:style {:margin-top "1.33em"}}
          (for [uid (-> recommendations sort)]
            ($ radar-ui/Avatar
               {:key uid
                :uid uid}))))))

;; (defn lookup-isbn> [isbn]
;;   (js/Promise.
;;    (fn [resolve reject]
;;      (js/setTimeout
;;       #(resolve {:title "Boom"})
;;       1000))))


(def-ui Book [radar book]
  {:from-context [radar book]}
  (let [isbn (-> book :isbn)
        image-url (book/cover-url book)

        [menu-anchor-el set-menu-anchor-el] (ui/use-state nil)

        BookDataCard ($ mui/Card
                        ($ ui/CommandCardArea
                           {:command (openlib/enhance-book-command
                                      commands/update-book)
                            :context {:radar radar
                                      :book book}}
                           ($ mui/CardContent
                              ($ :div (-> book :author))
                              ($ :h2 (-> book :title))
                              ($ :h3 (-> book :subtitle))))
                        ($ mui/Divider)
                        ($ ui/CommandCardArea
                           {:command commands/update-book-tags
                            :context {:book book
                                      :radar radar}}
                           ($ mui/CardContent
                              ($ ui/Stack
                                 ($ ui/FieldLabel
                                    {:text "Tags"})
                                 ($ ui/StringVectorChips {:values (book/tags-in-order book)}))))
                        ($ mui/Divider)
                        (ui/div
                         {:display "flex"
                          :justify-content "flex-end"
                          :align-items "center"
                          :padding "8px 8px 8px 0"}
                         (when (-> book :hidden)
                           (ui/div
                            {:margin "0 8px 0 0"}
                            "This book is marked for deletion."))
                         ($ mui/IconButton
                            {:onClick #(-> % .-currentTarget set-menu-anchor-el)
                             :size "small"}
                            (ui/icon "more_vert"))
                         ($ mui/Menu
                            {:open (not (nil? menu-anchor-el))
                             :onClose #(set-menu-anchor-el nil)
                             :anchorEl menu-anchor-el}
                            (if (-> book :hidden)
                              ($ mui/MenuItem
                                 {:onClick #(do
                                              (set-menu-anchor-el nil)
                                              (ui/execute-command>
                                               commands/unhide-book
                                               {:book book
                                                :radar radar}))}
                                 "Restore")
                              ($ mui/MenuItem
                                 {:onClick #(do
                                              (set-menu-anchor-el nil)
                                              (ui/execute-command>
                                               commands/hide-book
                                               {:book book
                                                :radar radar}))}
                                 "Delete")))))

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


(def-ui BookPageContent []
  ($ Book))

(def-page book-page
  {:path                   "/ui/radars/:radar/book/:book"
   :content                BookPageContent
   :force-sign-in          true
   :appbar-title-component radar-ui/RadarAppbarTitle
   :use-docs               {:radar radar/Radar}
   :update-context
   (fn [{:keys [radar book] :as context}]
     (let [book (if (string? book)
                  (-> radar (radar/book-by-id book))
                  nil)]
       (assoc context :book book)))
   :back-to                (fn [{:keys [radar]}]
                             (str "/ui/radars/" (-> radar :id)))
   })