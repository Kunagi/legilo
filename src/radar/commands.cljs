(ns radar.commands
  (:require
   [commons.models :as m :refer [def-model]]

   [base.user :as user]
   [radar.radar :as radar]
   [radar.book :as book]))


(def-model CreateRadar
  [m/Command--create-doc
   {:label "Create new Radar"

    :col radar/Radars

    :context-args [[:user user/User]]

    :form (fn [{:keys [user]}]
            {:fields [radar/title radar/allow-domain]
             :values {:uids [(-> user :id)]}})
    }])


(def-model AddBook
  [m/Command--update-doc--add-child
   {:label "Add Book"
    :icon "add"

    :doc-param :radar
    :inner-path [:books]

    :form {:fields [book/title book/author book/isbn book/asin book/tags]}}])



(def-model UpdateBook
  [m/Command--update-doc--update-child
   {:label "Edit Book"

    :doc-param :radar
    :child-param :book
    :inner-path [:books]

    :form (fn [{:keys [book]}]
            {:fields [book/title book/author book/isbn book/asin]
             :fields-values book})}])


(def-model UpdateBookTags
  [m/Command--update-doc--update-child
   {:label "Edit Book Tags"

    :doc-param :radar
    :child-param :book
    :inner-path [:books]

    :form (fn [{:keys [book]}]
            {:fields [book/tags]
             :fields-values book}) }])


(def-model UpdateBookReview
  [m/Command
   {:label "Edit Book Review"

    :form (fn [{:keys [book uid]}]
            {:fields [{:id :text
                       :rows 5
                       :multiline? true}]
             :fields-values (book/review-by-uid book uid)})

    :f (fn [{:keys [radar book uid values]}]
         (js/console.log "YEY" uid values)
         (let [review (book/review-by-uid book uid)
               path [:books (-> book :id) :reviews]]
           (if review
             [[:db/update-child radar path uid values]]
             [[:db/add-child radar path (assoc values
                                               :id uid
                                               :uid uid)]])))}])


(def-model RecommendBook
  [m/Command--update-doc--update-child
   {:label "Recommend Book"
    :icon "thumb_up"
    :inconspicuous? true
    :doc-param :radar
    :child-param :book
    :inner-path [:books]
    :static-changes (fn [{:keys [uid]}]
                      {:recommendations [:db/array-union [uid]]})}])

(def-model UnRecommendBook
  [m/Command--update-doc--update-child
   {:label "Recommend Book"
    :icon "thumb_up"
    :doc-param :radar
    :child-param :book
    :inner-path [:books]
    :static-changes (fn [{:keys [uid]}]
                      {:recommendations [:db/array-remove [uid]]})}])
