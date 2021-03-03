(ns radar.ui
  (:require
   [clojure.string :as str]
   [clojure.edn :as edn]
   [cljs.pprint :refer [pprint]]
   ["@material-ui/core" :as mui]
   ["@material-ui/lab" :as mui-lab]

   [spark.utils :as u]
   [spark.logging :refer [log]]

   [spark.core :as spark :refer [def-page]]
   [spark.ui :as ui :refer [def-ui def-ui-test $]]
   [spark.repository :as repository]
   [spark.runtime :as runtime]


   [radar.radar :as radar]
   [radar.book :as book]
   [radar.commands :as commands]
   [radar.openlib-ui :as openlib]))


;;; UI Rendering


(def-ui Book [radar book uid]
  {:from-context [radar book uid]}
  (let [radar-id (-> radar :id)
        book-id (-> book :id)
        cover-url (book/cover-url book)]
    ($ mui/Card
       ($ mui/CardActionArea
          {:component ui/Link
           :to (str "/ui/radars/" radar-id "/book/" book-id)}
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
                   (when (book/recommended-by-user? book uid)
                     ($ :div
                        {:className "material-icons"
                         :style {:color "#999"}}
                        "thumb_up")))))))))

(def-ui Section [section books]
  ($ ui/Stack
     ($ mui/Typography
        {:component "h3"
         :variant "h6"}
        (-> section :name))
     (if (empty? books)
       ($ :div
          {:style {:color "grey"
                   :font-style "italic"}}
          "no books here")
       (for [book (->> books (sort-by (fn [book] [(- (book/recommendation-count book))
                                                  (-> book :title)])))]
         ($ Book
            {:key (-> book :id)
             :book book})))))

(defonce SELECTED_TAG (atom nil))

(def use-selected-tag (ui/atom-hook SELECTED_TAG))

(def-ui Filter [radar]
  {:from-context [radar]}
  (let [selected-tag (use-selected-tag)
        all-tags (radar/all-tags radar)]
    ($ ui/Stack
       ($ :div
          {:style {:display :flex
                   :flex-wrap :wrap
                   :gap "8px"}}
          (for [tag (sort all-tags)]
            (let [selected? (= tag selected-tag)]
              ($ mui/Chip
                 {:key tag
                  :onClick #(reset! SELECTED_TAG (if selected? nil tag))
                  :color (if selected? "primary" "default")
                  :label tag
                  :size "small"})))))))

(def-ui Radar [radar]
  {:from-context [radar]}
  (let [selected-tag (use-selected-tag)
        selected-tag (when (u/v-contains? (radar/all-tags radar) selected-tag)
                       selected-tag)
        books (radar/books radar)
        books (if selected-tag
                (->> books
                     (filter #(book/contains-tag? % selected-tag)))
                books)]
    ($ ui/Stack
       {:spacing 3}
       ($ mui/Typography
          {:variant "h4"
           :component "h2"}
          (-> radar :title))
       ($ ui/Flexbox
          ($ ui/CommandButton
             {:command commands/AddBook
              :color "secondary"})
          #_($ ui/Button
             {:text "Add Book"
              :icon "add"
              :color "secondary"
              :onClick #(ui/show-dialog
                         {:content ($ openlib/SearchWidget)})}))
       ($ Filter)
       ($ ui/Stack
          (for [section radar/sections]
            ($ Section
               {:key (-> section :idx)
                :section section
                :books (get (radar/books-by-section-key books) (-> section :key))}))))))

(def-ui RadarPageContent []
  ($ Radar))

(def-ui RadarAppbarTitle [radar]
  {:from-context [radar]}
  (-> radar :title))

(def-page RadarPage
  {:path "/ui/radars/:radar"
   :content RadarPageContent
   :appbar-title-component RadarAppbarTitle
   :use-docs {:radar radar/Radar}})



(def-ui MenuIcon [radar]
  {:from-context [radar]}
  (when radar
    ($ mui/IconButton
       {:component ui/Link
        :to (str "/ui/radars/" (-> radar :id) "/config")}
       ($ :div {:class "i material-icons"} "settings"))))
