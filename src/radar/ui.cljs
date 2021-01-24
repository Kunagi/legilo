(ns radar.ui
  (:require
   [cljs.pprint :refer [pprint]]
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.context :as c.context]
   [commons.mui :as cui :refer [defnc $ <> div]]

   [base.ui :as ui]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.repository :as repository]
   [radar.service :as service]
   [radar.context :as context]
   [radar.book-ui :as book-ui]
   ))


;;; UI Rendering


(defnc Book[{:keys [book]}]
  (let [uid (context/use-uid)
        radar-id (context/use-radar-id)
        book-id (-> book :id)
        cover-url (service/book-cover-url book)]
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
                   (when (service/book-recommended-by-user? book uid)
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
     (for [book (->> books (sort-by (fn [book] [(- (service/book-recommendation-count book))
                                                (-> book :title)])))]
       ($ Book
          {:key (-> book :id)
           :book book})))))


(defonce SELECTED_TAG (atom nil))

(def use-selected-tag (c.context/atom-hook SELECTED_TAG))


(defnc Filter [{:keys [radar]}]
  (let [selected-tag (use-selected-tag)]
    ($ cui/Stack
       ($ :div
          {:style {:display :flex
                   :flex-wrap :wrap
                   :gap "8px"}}
          (for [tag (radar/all-tags radar)]
            (let [selected? (= tag selected-tag)]
              ($ mui/Chip
                 {:key tag
                  :onClick #(reset! SELECTED_TAG (if selected? nil tag))
                  :color (if selected? "primary" "default")
                  :label tag
                  :size "small"})))))))


(defnc Radar []
  (let [selected-tag (use-selected-tag)
        radar (context/use-radar)
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
          ($ cui/Button
             {:command #(service/add-book-command radar)
              :color "secondary"}))
       ($ Filter
          {:radar radar})
       ($ ui/Stack
          (for [section radar/sections]
            ($ Section
               {:key (-> section :idx)
                :section section
                :books (get (radar/books-by-section-key books) (-> section :key))}))
          (when (empty? books)
            ($ cui/Button
               {:command (service/add-example-books-command radar)}))))))


(defnc RadarPageContent []
  ($ Radar))


(defnc BookPageContent []
  ($ book-ui/Book))


;;;
;;; Radar Config
;;;


(defnc MenuIcon []
  (let [radar-id (context/use-radar-id)]
    ($ mui/IconButton
       {:component ui/Link
        :to (str "/ui/radars/" radar-id "/config")}
       ($ :div {:class "i material-icons"} "settings"))))


(defnc RadarConfigCard []
  (let [radar (context/use-radar)]
    ($ cui/FieldsCard
       {:entity radar
        :update-f #(service/update-radar> radar %)
        :fields [radar/title radar/allow-domain]})))


(defn write-to-clipboard [text]
  (-> ( js/navigator.clipboard.writeText text)))


(defnc RadarBackupCard []
  (let [radar (context/use-radar)]
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
