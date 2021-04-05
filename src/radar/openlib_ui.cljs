(ns radar.openlib-ui
  (:require
   [clojure.string :as str]
   ["@material-ui/core" :as mui]

   [spark.logging :refer [log]]
   [spark.utils :as u]
   [spark.form :as form]
   [spark.ui :as ui :refer [def-ui def-ui-test $]]

   [radar.book :as book]
   [radar.commands :as commands]))


;;; isbn lookup

;; https://openlibrary.org/dev/docs/api/books


;; https://openlibrary.org/isbn/9780140328721.json

(defn lookup-isbn> [isbn]
  (if (str/blank? isbn)
    (u/reject> "ISBN required")
    (let [url (str "https://openlibrary.org/api/books?format=json&jscmd=data"
                   "&bibkeys=ISBN:" isbn)]
      (-> (u/fetch-json> url)
          (.then #(-> % vals first))))))

(comment
  (u/tap> (lookup-isbn> "9780140328721"))
  (u/tap> (lookup-isbn> ""))
  (u/tap> (lookup-isbn> "gibts-nicht")))

(defn- apply-book-data-to-form [book-data form]
  (if book-data
    (-> form
        (form/set-waiting false)
        (form/set-fields-values
         {:title (-> book-data :title)
          :subtitle (-> book-data :subtitle)
          :author (->> book-data
                       :authors
                       (map :name)
                       (str/join ", "))}))
    (-> form
        (form/set-waiting false)
        (assoc-in [:errors :isbn] "ISBN not found"))))

(defn- on-isbn-lookup> [form]
  (-> (lookup-isbn> (-> form :values :isbn))
      (.then (fn [book-data]
               (partial apply-book-data-to-form book-data)))))

(defn enhance-book-command [command]
  (assoc command
         :form (fn [{:keys [book]}]
                 {:fields [(assoc-in book/isbn [1 :action]
                                     {:label "Lookup"
                                      :f on-isbn-lookup>})
                           book/title book/subtitle book/author
                           book/asin]
                  :fields-values book})))


;; ;; https://openlibrary.org/isbn/9780140328721.json


;;; search book

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
            {:style {:background-image          (str "url(" cover-url ")")
                     :background-position       "center"
                     :background-size           "cover"
                     :width                     "50px"
                     :min-width                 "50px"
                     :border-top-left-radius    "4px"
                     :border-bottom-left-radius "4px"
                     :overflow                  "hidden"}}))
       ($ mui/CardContent
          {:className "CardContent--book"}
          ($ :div
             {:style {:display         "flex"
                      :justify-content "space-between"
                      :height          "100%"
                      :align-items     "center"}}
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
  (let [[text set-text]       (ui/use-state "the lord of the rings")
        [results set-results] (ui/use-state [])
        hide-dialog           (ui/use-hide-dialog)
        search                (fn [text]
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
          :onChange     #(-> % .-target .-value set-text)
          :onKeyDown    #(when (= "Enter" (-> ^js % .-nativeEvent .-code))
                        (search text))
          #_            (js/console.log (-> % .-target .-value search))})
      (for [book results]
        ($ mui/Card
           {:key book}
           ($ mui/CardActionArea
              {:onClick #(do
                           (hide-dialog)
                           (ui/execute-command>
                             commands/add-found-book
                             {:radar  radar
                              :values book}
                             nil))}
              ($ SearchBookCardContent
                 {:book book})))))))


(def-ui-test [SearchWidget]
  ($ SearchWidget))
