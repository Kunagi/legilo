(ns radar.openlib-ui
  (:require
   [clojure.string :as str]
   ["@material-ui/core" :as mui]

   [spark.ui :as ui :refer [def-ui def-ui-test $ <>]]

   [spark.logging :refer [log]]

   [radar.book :as book]
   [radar.commands :as commands]))

(defn search-url [text]
  (str "http://openlibrary.org/search.json?title=" (js/encodeURIComponent text)))

(defn result-item->book [item]
  {:title (get item "title")
   :author (-> item (get "author_name") (->> (str/join ", ")))
   :subtitle (-> item (get "subtitle"))
   :isbn (-> item (get "isbn") (->> (sort-by count) reverse first))
   :publish-year (-> item (get "first_publish_year")) ;; TODO remove "0"
   })

(def-ui-test [search-url]
  (ui/data (search-url "Domain Driven Design")))


(def-ui SearchBookCardContent [book suffix-component]
  (let [cover-url (book/cover-url book)]
    ($ :div
       {:style {:display "flex"}}
       (when cover-url
         ($ :div
            {:style {:background-image (str "url(" cover-url ")")
                     :background-position "center"
                     :background-size "cover"
                     :width "50px"
                     :min-width "50px"
                     :border-top-left-radius "4px"
                     :border-bottom-left-radius "4px"
                     :overflow "hidden"}}))
       ($ mui/CardContent
          {:className "CardContent--book"}
          ($ :div
             {:style {:display "flex"
                      :justify-content "space-between"
                      :height "100%"
                      :align-items "center"}}
             ($ :div
                (-> book :title))
             ($ :div
                (-> book :author))
             ($ :div
                (-> book :isbn))
             ($ :div
                (-> book :publish-year))
             suffix-component)))))


(def-ui SearchWidget [radar]
  {:from-context [radar]}
  (let [[text set-text] (ui/use-state "the lord of the rings")
        [results set-results] (ui/use-state [])
        hide-dialog (ui/use-hide-dialog)
        search (fn [text]
                 (when (-> text count (> 5))
                   (log ::search
                        :text text)
                   (-> (js/fetch (search-url text))
                       (.then (fn [result]
                                (-> (-> result .json)
                                    (.then (fn [result]
                                             (log ::result
                                                  :result result)
                                             (-> result
                                                 js->clj
                                                 (get "docs")
                                                 (->> (map result-item->book)
                                                      set-results))))))))))]
    (ui/stack
     ($ mui/TextField
        {:defaultValue text
         :onChange #(-> % .-target .-value set-text)
         :onKeyDown #(when (= "Enter" (-> ^js % .-nativeEvent .-code))
                       (search text))
         #_(js/console.log (-> % .-target .-value search))})
     (for [book results]
       ($ mui/Card
          {:key book}
          ($ mui/CardActionArea
             {:onClick #(do
                          (hide-dialog)
                          (ui/execute-command>
                             commands/AddFoundBook
                             {:radar radar
                              :values book}
                             nil))}
             ($ SearchBookCardContent
                {:book book})))))))


(def-ui-test [SearchWidget]
  ($ SearchWidget))
