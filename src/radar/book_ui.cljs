(ns radar.book-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.context :as c.context]
   [commons.mui :as cui :refer [defnc $ <> div]]

   [amazon.service :as amazon-service]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.service :as service]
   [radar.context :as context]
   ))

(defnc Review [{:keys [review]}]
  ($ :div
     {:className "Recommendation"
      :style {:display :flex
              :place-content :stretch
              :place-items :stretch}}
     ($ mui/Avatar)
     ($ mui/Card
        {:className "flex-grow-1 ml-1"}
        ($ mui/CardContent
           ($ cui/Stack
              ($ :div
                 {:style {:color "#666"
                          :font-style "italic"}}
                 (-> review :uid))
              ($ :div
                 (-> review :text)))))))

(defn start-review [radar book uid]
  (cui/show-form-dialog
   {:fields [{:id :text
              :rows 5
              :multiline? true}]
    :submit #(service/update-review-text> radar book uid (assoc % :uid uid))}))

(defnc OwnReview []
  (let [
        radar (context/use-radar)
        book-id (context/use-book-id)
        book (radar/book-by-id radar book-id)
        uid (context/use-uid)
        recommended? (service/book-recommended-by-user? book uid)]
    ($ :div
       {:className "Recommendation"
        :style {:display :flex
                :place-content :stretch
                :place-items :stretch}}
       (if recommended?
         ($ cui/IconButton
            {:command book/recommend
             :onClick #(service/un-recommend-book> radar book uid)
             :icon "thumb_up"
             :color "secondary"})
         ($ cui/IconButton
            {:command book/recommend
             :onClick #(service/recommend-book> radar book uid)
             :theme "outlined"}))
       ($ mui/Card
          {:className "flex-grow-1 ml-1"}
          ($ mui/CardActionArea
             {:onClick #(start-review radar book uid)}
             ($ mui/CardContent
                ($ :div {:style {:color "grey"
                                 :font-style "italic"}}
                   (if recommended?
                     "Leave a review?"
                     "Recommend this book?"))))))))

(defnc Reviews []
  (let [{:keys [radar book-id]} (c.context/use-context-data)
        book (radar/book-by-id radar book-id)
        reviews (-> book :reviews vals)]
    ($ cui/Stack
       ($ OwnReview {})
       (for [review reviews]
         ($ Review
            {:key (-> review :uid)
             :review review}))
       (cui/data (-> book :reviews)))))


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
        uid (context/use-uid)]
    ($ :div
       {:style {:display :grid
                :grid-template-columns "auto minmax(100px,200px)"
                :grid-gap "8px"}}

       ($ cui/Stack
          ($ cui/FieldsCard
             {:entity book
              :update-f #(service/update-book> radar book %)
              :fields [book/title book/author book/isbn book/asin book/tags]})

          ($ Reviews))

       ($ :div
          ($ cui/Stack
             {:spacing 3}

             ($ :div)

             (counter book)

             ($ cui/Stack

                (if (service/book-recommended-by-user? book uid)
                  ($ cui/Button
                     {:command book/un-recommend
                      :onClick #(service/un-recommend-book> radar book uid)
                      :color "default"})
                  ($ cui/Button
                     {:command book/recommend
                      :onClick #(service/recommend-book> radar book uid)
                      :color "secondary"}))

                ($ cui/Button
                   {:command book/view-on-amazon
                    :href (if-let [asin (-> book :asin)]
                            (amazon-service/href asin)
                            (amazon-service/search-href (-> book :title)))
                    :target :_blank
                    :color "secondary"}))

             (when-let [asin (-> book :asin)]
               ($ :img
                  {:src (amazon-service/image-url asin)
                   :referrer-policy "no-referrer"
                   :class "MuiPaper-root MuiPaper-elevation1 MuiPaper-rounded"
                   :style {:margin "0 auto"}})))))))
