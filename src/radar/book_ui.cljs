(ns radar.book-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.mui :as cmui :refer [defnc $ <> div]]
   [base.ui :as ui]

   [base.context :as context]

   [amazon.ui :as amazon]

   [radar.book :as book]
   [radar.repository :as repository]
   [radar.service :as service]
   ))


(defn use-radar-id []
  (-> (ui/use-params) :radarId))

(defn use-book-id []
  (-> (ui/use-params) :bookId))

(defn use-book []
  (ui/use-doc (repository/book-path (use-radar-id) (use-book-id))))


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
  (let [book (use-book)
        uid (context/use-uid)]
    ($ :div
       {:style {:display :grid
                :grid-template-columns "auto minmax(100px,200px)"
                :grid-gap "8px"}}

       ($ cmui/DocFieldsCard
          {:doc book
           :fields [book/title book/author book/isbn book/asin book/tags]})

       ($ :div
          ($ cmui/Stack
             {:spacing 3}

             ($ :div)

             (counter book)

             ($ cmui/Stack

                (if (service/book-recommended-by-user? book uid)
                  ($ cmui/Button
                     {:command book/un-recommend
                      :onClick #(service/un-recommend-book> uid book)
                      :color "default"})
                  ($ cmui/Button
                     {:command book/recommend
                      :onClick #(service/recommend-book> uid book)
                      :color "secondary"}))

                ($ cmui/Button
                   {:command book/view-on-amazon
                    :href (if-let [asin (-> book :asin)]
                            (amazon/href asin)
                            (amazon/search-href (-> book :title)))
                    :target :_blank
                    :color "secondary"}))

             (when-let [asin (-> book :asin)]
               ($ :img
                  {:src (amazon/image-url asin)
                   :class "MuiPaper-root MuiPaper-elevation1 MuiPaper-rounded"
                   :style {:margin "0 auto"}})))))))
