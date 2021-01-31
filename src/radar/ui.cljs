(ns radar.ui
  (:require
   [cljs.pprint :refer [pprint]]
   ["@material-ui/core" :as mui]

   [commons.utils :as u]
   [commons.logging :refer [log]]
   [commons.models :as models :refer [def-model]]
   [commons.context :as c.context]
   [commons.mui :as cui :refer [defnc $]]

   [base.context :as b.context]
   [base.ui :as ui]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.commands :as commands]
   [radar.book-ui :as book-ui]
   ))


;;; UI Rendering


(defnc Book[{:keys [book]}]
  (let [uid (b.context/use-uid)
        {:keys [radar]} (c.context/use-context-data)
        radar-id (-> radar :id)
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
                           :overflow "hidden"}})
               #_($ :img
                  {:src cover-url
                   :width "50px"
                   :style {:overflow "hidden"}}))
             ($ mui/CardContent
                {:className "CardContent--book"}
                ($ :div
                   {:style {:display "flex"
                            :justify-content "space-between"
                            :height "100%"
                            :align-items "center"}}
                   ($ :div
                      #_{:style {:font-weight "bold"}}
                      (-> book :title))
                   (when (book/recommended-by-user? book uid)
                     ($ :div
                        {:className "material-icons"
                         :style {:color "#999"}}
                        "thumb_up"))
                   #_(when-let [author (-> book :author)]
                     ($ :span
                        {:style {:color "#666"
                                 :margin-left "8px"}}
                        author)))))))))


(defnc Section [{:keys [section books]}]
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

(def use-selected-tag (c.context/atom-hook SELECTED_TAG))


(defnc Filter [{:keys [radar]}]
  (let [selected-tag (use-selected-tag)
        all-tags (radar/all-tags radar)]
    ($ cui/Stack
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


(defnc Radar []
  (let [{:keys [radar]} (c.context/use-context-data)
        selected-tag (use-selected-tag)
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
       ($ cui/Flexbox
          ($ cui/CommandButton
             {:command commands/AddBook
              :color "secondary"}))
       ($ Filter
          {:radar radar})
       ($ ui/Stack
          (for [section radar/sections]
            ($ Section
               {:key (-> section :idx)
                :section section
                :books (get (radar/books-by-section-key books) (-> section :key))}))))))


(defnc RadarPageContent []
  ($ Radar))


(def-model RadarPage
  [models/Page
   {:path "/ui/radars/:Radar"
    :content RadarPageContent
    :data {:uid :uid
           :user :user
           :radar [:param-doc radar/Radars]}}])

(defnc BookPageContent []
  ($ book-ui/Book))

(def-model BookPage
  [models/Page
   {:path "/ui/radars/:Radar/book/:bookId"
    :content BookPageContent
    :data {:uid :uid
           :user :user
           :radar [:param-doc radar/Radars]}}])

;;;
;;; Radar Config
;;;


(defnc MenuIcon []
  (let [radar-id (c.context/use-param-2 :radarId)]
    ($ mui/IconButton
       {:component ui/Link
        :to (str "/ui/radars/" radar-id "/config")}
       ($ :div {:class "i material-icons"} "settings"))))


(defnc RadarConfigCard []
  (let [{:keys [radar]} (c.context/use-context-data)]
    ($ cui/FieldsCard
       {:entity radar
        :fields [radar/title radar/allow-domain]})))


(defn write-to-clipboard [text]
  (-> ( js/navigator.clipboard.writeText text)))


(defnc RadarBackupCard []
  (let [{:keys [radar]} (c.context/use-context-data)]
    ($ cui/SimpleCard
       {:title "Radar Data"}
       ($ :div
          {:style {:max-height "30vh"
                   :overflow "auto"}}
          (cui/data radar))
       ($ cui/Button
          {:text "Copy to Clipboard"
           :onClick #(write-to-clipboard (with-out-str (pprint radar)))}))))


(defnc RadarConfigPageContent []
  ($ cui/Stack
     ($ RadarConfigCard)
     ($ RadarBackupCard)))


(def-model RadarConfigPage
  [models/Page
   {:path "/ui/radars/:Radar/config"
    :content RadarConfigPageContent
    :data {:uid :uid
           :user :user
           :radar [:param-doc radar/Radars]}}])
