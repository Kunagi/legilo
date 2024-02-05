(ns radar.radar-page
  (:require
   ["@material-ui/core" :as mui]

   [spark.utils :as u]

   [spark.ui :as ui :refer [def-ui def-page $]]
   [spark.db :as db]

   [base.user :as user]
   [radar.openlib-ui :as openlib]
   [radar.radar :as radar]
   [radar.book :as book]
   [radar.commands :as commands]
   [radar.ui :as radar-ui]))

;;; XENIUM Migration

(def xenium-migration-from-domain (if goog.DEBUG
                                    "koczewski.de"
                                    "xenium.de"))
(def xenium-migration-to-domain (if goog.DEBUG
                                  "gmail.com"
                                  "xenium.com"))

(def-ui XeniumReviewMigration [review book author user]

  (ui/use-effect
    :once
    (let [old-uid (-> author :id)
          new-uid (-> user :id)
          new-review (-> review
                         (select-keys [:text :ts-updated])
                         (assoc :id new-uid)
                         (assoc :uid new-uid))]
      (db/transact>
       [(db/update-tx book {:recommendations [:db/array-remove [old-uid]]})
        (db/update-tx book {:recommendations [:db/array-union [new-uid]]})

        (db/update-tx book {(str "recommendations-times." old-uid) [:db/delete]
                            (str "recommendations-times." new-uid) (-> book :recommendations-times (get old-uid))})

        (db/delete-tx review)
        (db/add-child-tx book [:reviews] new-review)]))

    nil)

  (ui/<>
   (ui/DEBUG "migrated!")
   ))

(defn email-prefix [email]
  (when email
    (-> email (.substring 0 (-> email (.indexOf "@"))))))

(def-ui XeniumMigrationReview [review book user]
  {:from-context [user]}
  (let [review-author (ui/use-doc user/User (-> review :uid))]
    ($ :div
       ;; (ui/DEBUG review-author)
       ;; (ui/DEBUG user)
       ;; (ui/DEBUG (email-prefix (-> review-author :auth-email)))
       ;; (ui/DEBUG (-> book :recommendations-times (get (-> review-author :uid))))

       (when (and
              (-> review-author :auth-email)
              (-> review-author :auth-email (.endsWith xenium-migration-from-domain))
              (-> user :auth-email (.endsWith xenium-migration-to-domain))
              (= (email-prefix (-> review-author :auth-email))
                 (email-prefix (-> user :auth-email))))
         ($ XeniumReviewMigration
            {:review review
             :book book
             :author review-author
             :user user}))
       
       (ui/<>
        (ui/DEBUG review))
       )))

(def-ui XeniumMigration [book]
  (let [recommendations (->> book :recommendations (into #{}))
        reviews         (->> book :reviews vals
                             (remove #(not (contains? recommendations (-> % :uid))))
                             )]
    (ui/<>
     (for [review reviews]
       ($ XeniumMigrationReview
          {:key    (-> review :uid)
           :review review
           :book book})))))

;;; UI Rendering

(def-ui Book [radar book uid]
  {:from-context [radar book uid]}
  (let [radar-id  (-> radar :id)
        book-id   (-> book :id)
        cover-url (book/cover-url book)


        ]
    (ui/<>
     ($ mui/Card
        ($ mui/CardActionArea
           {:component ui/RouterLink
            :to        (str "/ui/radars/" radar-id "/book/" book-id)}
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
                    (ui/div
                     (ui/span
                      {:font-weight :bold}
                      (-> book :title))
                     (when-let [subtitle (-> book :subtitle)]
                       ($ :span
                          {:style {:margin-left "0.25rem"
                                   :font-weight 300
                                   :color       "#555"
                                   }}
                          subtitle)))
                    (when (book/recommended-by-user? book uid)
                      ($ :div
                         {:className "material-icons"
                          :style     {:color "#999"}}
                         "thumb_up")))))))
     
     ($ XeniumMigration {:book book})
     )))

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
             {:command (openlib/enhance-book-command
                        commands/add-book)
              :context {:radar radar}
              :color "secondary"})
          #_($ ui/Button
               {:text "Add Book"
                :icon "add"
                :color "secondary"
                :onClick #(ui/show-dialog
                           {:content ($ openlib/SearchWidget)})}))
       ($ Filter)

       ($ ui/Stack

          (when-let [uids (->> radar radar/recommendation-uids sort)]
            ($ :div
               {:style {:display "flex"
                        :gap "8px"}}
               (for [uid uids]
                 ($ radar.ui/Avatar {:key uid :uid uid}))))

          (for [section radar/sections]
            ($ Section
               {:key (-> section :idx)
                :section section
                :books (get (radar/books-by-section-key books) (-> section :key))}))))))

(def-ui PageContent []
  ($ Radar))

(def-page radar-page
  {:path                   ["radars" radar/Radar]
   :content                PageContent
   :appbar-title-component radar-ui/RadarAppbarTitle
   :force-sign-in          true})
