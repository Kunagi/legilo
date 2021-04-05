(ns radar.commands
  (:require
   [clojure.string :as str]

   [spark.core :as spark :refer [def-cmd]]
   [spark.repository :as repository]

   [radar.radar :as radar]
   [radar.book :as book]
   ))

(def-cmd create-radar
  {:label "Create new Radar"

   :context-args [[:uid :string]]

   :form (fn [{:keys [uid]}]
           {:fields [radar/title radar/allow-domain]
            :values {:uids [uid]}})

   :f (fn [{:keys [values]}]
        [[:db/create radar/Radar values]])})

(def-cmd add-book
  {:label "Add Book"
   :icon "add"

   :context-args [[:radar radar/Radar]]

   :form (fn [{:keys [radar]}]
           {:fields [book/title book/subtitle book/isbn book/author book/asin
                     (assoc-in book/tags
                               [1 :options]
                               (radar/all-tags radar))]})

   :f (fn [{:keys [radar values]}]
        [[:db/add-child radar [:books] values]])})


(def-cmd add-found-book
  {:label "Add Book"
   :icon "add"

   :context-args [[:radar radar/Radar]]

   :f (fn [{:keys [radar values]}]
        [[:db/add-child radar [:books] values]])})

(def-cmd update-book
  {:label "Edit Book"

   :context-args [[:radar radar/Radar]
                  [:book book/Book]]


   :f (fn [{:keys [radar book values]}]
        [[:db/update-child radar [:books] (-> book book/id) values]])})

(def-cmd hide-book
  {:label "Delete"

   :context-args [[:radar radar/Radar]
                  [:book book/Book]]

   :f (fn [{:keys [radar book]}]
        [[:db/update-child radar [:books] (-> book book/id) {:hidden [:db/timestamp]}]])})

(def-cmd unhide-book
  {:label "Restore"

   :context-args [[:radar radar/Radar]
                  [:book book/Book]]

   :f (fn [{:keys [radar book]}]
        [[:db/update-child radar [:books] (-> book book/id) {:hidden nil}]])})

(def-cmd update-book-tags
  {:label "Edit Book Tags"

   :context-args [[:radar radar/Radar]
                  [:book book/Book]]

   :form (fn [{:keys [book radar]}]
           {:fields [(assoc-in  book/tags
                                [1 :options] (radar/all-tags radar))]
            :fields-values book})

   :f (fn [{:keys [radar book values]}]
        (let [values (update values :tags #(mapv str/lower-case %))]
          [[:db/update-child radar [:books] (-> book book/id) values]]))})

(def-cmd update-book-review
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

(def-cmd recommend-book
  {:label "Recommend Book"
   :icon "thumb_up"
   :inconspicuous? true

   :context-args [[:radar radar/Radar]
                  [:book book/Book]]

   :f (fn [{:keys [radar book uid]}]
        [[:db/update-child radar [:books] (-> book book/id)
          {:recommendations [:db/array-union [uid]]
           (str "recommendations-times." uid) [:db/timestamp]
           (str "reviews." uid ".ts-updated") [:db/timestamp]
           (str "reviews." uid ".id") uid
           (str "reviews." uid ".uid") uid
           }]])})

(def-cmd unrecommend-book
  {:label "Un-Recommend Book"
   :icon "thumb_up"

   :context-args [[:radar radar/Radar]
                  [:book book/Book]]

   :f (fn [{:keys [radar book uid]}]
        (repository/update-doc-child>
         radar [:books] (-> book book/id)
         {:recommendations [:db/array-remove [uid]]}))})
