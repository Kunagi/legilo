(ns radar.commands
  (:require

   [spark.core :as spark :refer [def-cmd]]
   [base.user :as user]
   [radar.radar :as radar]
   [radar.book :as book]
   [clojure.string :as str]))

(def-cmd CreateRadar
  {:label "Create new Radar"

   :context-args [[:user user/User]]

   :form (fn [{:keys [user]}]
           {:fields [radar/title radar/allow-domain]
            :values {:uids [(-> user :id)]}})

   :f (fn [{:keys [user values]}]
        [[:db/create radar/Radar values]])})

(def-cmd AddBook
  {:label "Add Book"
   :icon "add"

   :form (fn [{:keys [radar]}]
           {:fields [book/title book/isbn book/author book/asin
                     (assoc-in book/tags
                               [1 :options]
                               (radar/all-tags radar))]})

   :f (fn [{:keys [radar values]}]
        [[:db/add-child radar [:books] values]])})

(def-cmd AddFoundBook
  {:label "Add Book"
   :icon "add"

   :f (fn [{:keys [radar values]}]
        [[:db/add-child radar [:books] values]])})

(def-cmd UpdateBook
  {:label "Edit Book"

   :form (fn [{:keys [book]}]
           {:fields [book/title book/author book/isbn book/asin]
            :fields-values book})

   :f (fn [{:keys [radar book values]}]
        [[:db/update-child radar [:books] (-> book book/id) values]])})

(def-cmd UpdateBookTags
  {:label "Edit Book Tags"

   :doc-param :radar
   :child-param :book
   :inner-path [:books]

   :form (fn [{:keys [book radar]}]
           {:fields [(assoc-in  book/tags
                                [1 :options] (radar/all-tags radar))]
            :fields-values book})

   :f (fn [{:keys [radar book values]}]
        (let [values (update values :tags #(mapv str/lower-case %))]
          [[:db/update-child radar [:books] (-> book book/id) values]]))})

(def-cmd UpdateBookReview
  {:label "Edit Book Review"

   :context-args [[:radar radar/Radar]
                  [:book book/Book]]

   :form (fn [{:keys [book uid]}]
           {:fields [{:id :text
                      :rows 5
                      :multiline? true}]
            :fields-values (book/review-by-uid book uid)})

   :f (fn [{:keys [radar book uid values]}]
        (let [review (book/review-by-uid book uid)
              path [:books (-> book :id) :reviews]]
          (if review
            [[:db/update-child radar path uid values]]
            [[:db/add-child radar path (assoc values
                                              :id uid
                                              :uid uid)]])))})

(def-cmd RecommendBook
  {:label "Recommend Book"
   :icon "thumb_up"
   :inconspicuous? true

   :f (fn [{:keys [radar book uid]}]
        [[:db/update-child radar [:books] (-> book book/id)
          {:recommendations [:db/array-union [uid]]}]])})

(def-cmd UnRecommendBook
  {:label "Recommend Book"
   :icon "thumb_up"

   :f (fn [{:keys [radar book uid]}]
        [[:db/update-child radar [:books] (-> book book/id)
          {:recommendations [:db/array-remove [uid]]}]])})
