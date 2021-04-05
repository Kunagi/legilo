(ns radar.radar-activitylog-page
  (:require
   [clojure.edn :as edn]
   [cljs.pprint :refer [pprint]]
   ["@material-ui/core" :as mui]


   [spark.utils :as u]
   [spark.ui :as ui :refer [def-ui def-page $]]
   [spark.repository :as repository]


   [radar.radar :as radar]
   [radar.ui :as radar-ui]
   [clojure.string :as str]))

(def-ui Book [book recommendations radar]
  {:from-context [radar]}
  (let [book-id  (-> book :id)
        radar-id (-> radar :id)]
    (ui/stack
     {:classes "ActivityLogBook"}
     ($ mui/Link
        {:to        (str "/ui/radars/" radar-id "/book/" book-id)
         :component ui/Link}
        (ui/div
         {:font-weight 900}
         (-> book :title)))
     (for [rec recommendations]
       (let [uid (-> rec :id)]
         ($ radar-ui/Avatar
            {:key uid
             :uid uid})))))
  )

(def-ui Date [date recommendations]
  (let [recs-by-book (group-by :book recommendations)
        books        (->> recs-by-book keys (sort-by :title))]
    (ui/stack
     ($ mui/Divider)
     (ui/div
      {:color "#666"}
      date)
     (for [book books]
       ($ Book
          {:key             book
           :book            book
           :recommendations (get recs-by-book book)})))))

(def-ui PageContent [radar]
  {:from-context [radar]}
  (let [books       (->> radar
                         radar/books)
        recs        (reduce (fn [result book]
                              (let [recs (->> book
                                              :recommendations
                                              (map (fn [rec-id]
                                                     {:id   rec-id
                                                      :time (-> book :recommendations-times (get rec-id))
                                                      :book book})))]
                                (into result recs)))
                            [] books)
        recs-by-day (group-by #(-> % :time u/date) recs)
        dates       (->> recs-by-day keys sort reverse)]
    (ui/div
     ($ :h1
        "Recent recommendations")
     (ui/stack-4
      (for [date dates]
        ($ Date
           {:key             date
            :date            date
            :recommendations (get recs-by-day date)}))))))

(def-page radar-activitylog-page
  {:path                   "/ui/radars/:radar/activitylog"
   :content                PageContent
   :appbar-title-component radar-ui/RadarAppbarTitle
   :use-docs               {:radar radar/Radar}
   :back-to                (fn [{:keys [radar]}]
                             (str "/ui/radars/" (-> radar :id)))})
