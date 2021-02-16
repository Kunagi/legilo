(ns radar.ui
  (:require
   [clojure.string :as str]
   [cljs.pprint :refer [pprint]]
   ["@material-ui/core" :as mui]

   [spark.utils :as u]
   [spark.logging :refer [log]]

   [spark.core :as spark :refer [def-page]]
   [spark.ui :as ui :refer [def-ui $]]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.commands :as commands]
   [radar.book-ui :as book-ui]
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
          ($ ui/Button
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

(def-ui BookPageContent []
  ($ book-ui/Book))

(def-page BookPage
  {:path "/ui/radars/:radar/book/:book"
   :content BookPageContent
   :appbar-title-component RadarAppbarTitle
   :use-docs {:radar radar/Radar}
   :update-context
   (fn [{:keys [radar book] :as context}]
     (let [book (if (string? book)
                  (-> radar (radar/book-by-id book))
                  nil)]
       (assoc context :book book)))})

;;;
;;; Radar Config
;;;


(def-ui MenuIcon [radar]
  {:from-context [radar]}
  (when radar
    ($ mui/IconButton
       {:component ui/Link
        :to (str "/ui/radars/" (-> radar :id) "/config")}
       ($ :div {:class "i material-icons"} "settings"))))

(def-ui RadarConfigCard [radar]
  {:from-context [radar]}
  ($ ui/DocFieldsCard
     {:doc radar
      :fields [radar/title radar/allow-domain]}))

(defn write-to-clipboard [text]
  (-> (js/navigator.clipboard.writeText text)))

(def-ui RadarBackupCard [radar]
  {:from-context [radar]}
  ($ ui/SimpleCard
     {:title "Radar Data"}
     ($ :div
        {:style {:max-height "30vh"
                 :overflow "auto"}}
        (ui/data radar))
     ($ ui/Button
        {:text "Copy to Clipboard"
         :onClick #(write-to-clipboard (with-out-str (pprint radar)))})))

(def-ui RadarConfigPageContent []
  ($ ui/Stack
     ($ RadarConfigCard)
     ($ RadarBackupCard)))

(def-page RadarConfigPage
  {:path "/ui/radars/:radar/config"
   :content RadarConfigPageContent
   :appbar-title-component RadarAppbarTitle
   :use-docs {:radar radar/Radar}})


;;
;; searc
;;

