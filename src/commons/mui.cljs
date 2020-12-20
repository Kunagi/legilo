(ns commons.mui
  (:require-macros [commons.mui])
  (:require
   [cljs.pprint :refer [pprint]]
   [shadow.resource :as resource]
   [cljs-bean.core :as cljs-bean]

   [helix.core :refer [defnc $]]
   [helix.dom :as d]

   ["react" :as react]
   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as mui-styles]

   ["material-ui-chip-input" :default ChipInput]

   [commons.utils :as u]
   [commons.firestore :as fs]
   [commons.firestore-hooks :as firestore-hooks]
   [commons.command :as command]
   [commons.context :as context]
   [commons.form-ui :as form-ui]
   ))


(def StringVectorChips form-ui/StringVectorChips)
(def CommandCardArea form-ui/CommandCardArea)
(def FormCardArea form-ui/FormCardArea)
(def FieldCardArea form-ui/FieldCardArea)
(def FieldsCardAreas form-ui/FieldsCardAreas)
(def FieldsCard form-ui/FieldsCard)
(def DocFieldCardArea form-ui/DocFieldCardArea)
(def DocFieldCard form-ui/DocFieldCard)
(def DocFieldsCard form-ui/DocFieldsCard)
(def FormDialogsContainer form-ui/FormDialogsContainer)

(def show-form-dialog form-ui/show-form-dialog)

(def Link router/Link)


(defn create-context [value]
  (-> react (.createContext value)))

;;;
;;; Styles / Theme
;;;

(defn make-styles [styles-f]
  (mui-styles/makeStyles
   (fn [theme]
     (clj->js (styles-f theme)))))


;;;
;;; styles
;;;

(defn style-bg-img [url]
  {:background-image (str "url(" url ")")
   :background-repeat "no-repeat"
   :background-position-x "center"
   :background-position-y "top"
   :background-size "contain"})

;;;
;;; Hooks
;;;

(def atom-hook context/atom-hook)


;;;
;;; common ui functions
;;;

(defn data [& datas]
  (d/div
   (for [[i data] (map-indexed vector datas)]
     (d/div
      {:key i
       :style {:white-space "pre-wrap"
               :font-family :monospace
               :overflow "auto"
               :width "100%"
               :background-color "#333"
               :color "#6f6"
               :padding "1rem"
               :border-radius "4px"
               :margin "1px"
               }}
      (with-out-str (pprint data))))))


(defn icon [icon-name]
  (d/div
   {:class "i material-icons"}
   icon-name))

;;;
;;; common components
;;;

(defnc Spacer [{:keys [width height]}]
  (let [theme (mui-styles/useTheme)]
    (d/div
     {:style {:width (-> theme (.spacing (or width 1)))
              :height(-> theme (.spacing (or width 1)))}})))


(defnc ValueLoadGuard [{:keys [children value padding]}]
  (if value
    children
    (let [theme (mui-styles/useTheme)]
      ($ :div
         {:style {:display :flex
                  :padding (when padding (-> theme (.spacing padding)))
                  :justify-content "space-around"}}
         ($ mui/CircularProgress)))))

(defnc ValuesLoadGuard [{:keys [children values padding]}]
  (if (reduce (fn [ret value]
                (and ret value))
              true values)
    children
    (let [theme (mui-styles/useTheme)]
      ($ :div
         {:style {:display :flex
                  :padding (when padding (-> theme (.spacing padding)))
                  :justify-content "space-around"}}
         ($ mui/CircularProgress)))))


(defnc Stack [{:keys [children spacing]}]
  (let [theme (mui-styles/useTheme)]
    (d/div
     {:style {:display :grid
              :grid-gap (-> theme (.spacing (or spacing 1)))}}
     children)))


(defnc Flexbox [{:keys [children spacing]}]
  (let [theme (mui-styles/useTheme)]
    (d/div
     {:style {:display :flex
              ;; FIXME :gap (-> theme (.spacing (or spacing 1)))
              }}
     (for [[idx child] (map-indexed vector (if (seqable? children)
                                             children
                                             [children]))]
       (d/div
        {:key idx
         :style {:margin-right (-> theme (.spacing (or spacing 1)))}}
        child)))))


(defnc Button [{:keys [text icon
                       onClick to href target
                       variant color size
                       command
                       context
                       then]}]
  (let [context (merge (context/use-context-data)
                       context)
        command (u/trampoline-if command)
        text (or text (-> command :label) ":text missing")
        icon (when-let [icon (or icon (-> command :icon))]
               (if (string? icon)
                 (d/div {:class "i material-icons"} icon)
                 icon))
        onClick (or onClick
                    (-> command :onClick)
                    (when-let [form (-> command :form)]
                      #(show-form-dialog form))
                    #(-> (command/execute> command context)
                         (.then (or then identity))))
        color (or color
                  (when (-> command :inconspicuous?) "default")
                  "primary")]
    (if to
      ($ mui/Button
         {:to to
          :component router/Link
          :variant (or variant "contained")
          :color (or color "primary")
          :startIcon icon
          :size size}
         text)
      ($ mui/Button
         {:onClick onClick
          :href href
          :target target
          :variant (or variant "contained")
          :color (or color "primary")
          :startIcon icon
          :size size}
         text))))


(defnc IconButton [{:keys [icon onClick color size command theme]}]
  (let [command (u/trampoline-if command)
        onClick (or onClick
                    (-> command :onClick)
                    (when-let [form (-> command :form)]
                      #(show-form-dialog form)))
        icon (when-let [icon (or icon
                                 (-> command :icon)
                                 "play_arrow")]
               (if (string? icon)
                 (d/div {:class (str "material-icons" (when theme (str "-" theme)))}
                        icon)
                 icon))]
    ($ mui/IconButton
       {:onClick onClick
        :color color
        :size size}
       icon)))


(defnc CardOverline [{:keys [text]}]
  ($ mui/Typography
     {:variant "overline"}
     text))

(defnc SimpleCard [{:keys [title children className]}]
  ($ mui/Card
     {:className className}
     ($ mui/CardContent
        ($ Stack
           (when title ($ CardOverline {:text title}))
           ($ Stack children)))))


(defnc CardRow [{:keys [children]}]
  (d/div
   {:style {:display :grid
            :grid-template-columns (str "repeat(" (count children) ", auto)")}}
   children))

(def FieldLabel form-ui/FieldLabel)
(def Field form-ui/Field)

(defnc FieldCardContent [{:keys [label children]}]
  ($ mui/CardContent
     ($ Field {:label label}
        children)))

;;;
;;; desktop
;;;

(defnc PageContentWrapper []
  (let [page (context/use-page)]
    ($ mui/Container
        {:maxWidth (get page :max-width "sm")}
        ($ ValuesLoadGuard {:values (-> page :data vals)
                            :padding 2}
           ($ (-> page :content))))
     ))


(defnc PageSwitch [{:keys [pages devtools-component]}]
  ($ router/Switch
     (for [page pages]
       ($ router/Route
          {:key (-> page :path)
           :path (-> page :path)}
          (context/provider
           {:context context/page
            :value page}
           ($ :div
              ($ PageContentWrapper)
              (when (and  ^boolean js/goog.DEBUG devtools-component)
                ($ devtools-component))))))))


(defnc VersionInfo []
  ($ :div
   {:style {:margin-top "4rem"
            :margin-right "1rem"
            :text-align :right
            :color "lightgrey"
            :font-size "75%"}}
   "v1."
   (str (resource/inline "../spa/version.txt"))
   " · "
   (str (resource/inline "../spa/version-time.txt"))))

;;;
;;; auth
;;;

(defnc AuthCompletedGuard [{:keys [children padding]}]
  (let [auth-completed (context/use-auth-completed)]
    ($ ValueLoadGuard {:value auth-completed :padding padding}
       children)))
