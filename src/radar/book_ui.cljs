(ns radar.book-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.mui :as cui :refer [defnc $ <> div]]

   [amazon.ui :as amazon]

   [radar.book :as book]
   [radar.service :as service]
   [radar.context :as context]
   ))


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
  (let [book (context/use-book)
        uid (context/use-uid)]
    ($ :div
       {:style {:display :grid
                :grid-template-columns "auto minmax(100px,200px)"
                :grid-gap "8px"}}

       ($ cui/DocFieldsCard
          {:doc book
           :fields [book/title book/author book/isbn book/asin book/tags]})

       ($ :div
          ($ cui/Stack
             {:spacing 3}

             ($ :div)

             (counter book)

             ($ cui/Stack

                (if (service/book-recommended-by-user? book uid)
                  ($ cui/Button
                     {:command book/un-recommend
                      :onClick #(service/un-recommend-book> uid book)
                      :color "default"})
                  ($ cui/Button
                     {:command book/recommend
                      :onClick #(service/recommend-book> uid book)
                      :color "secondary"}))

                ($ cui/Button
                   {:command book/view-on-amazon
                    :href (if-let [asin (-> book :asin)]
                            (amazon/href asin)
                            (amazon/search-href (-> book :title)))
                    :target :_blank
                    :color "secondary"}))

             (when-let [asin (-> book :asin)]
               ($ :img
                  {:src (amazon/image-url asin)
                   :referrer-policy "no-referrer"
                   :class "MuiPaper-root MuiPaper-elevation1 MuiPaper-rounded"
                   :style {:margin "0 auto"}})))))))
