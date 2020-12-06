(ns commons.mui
  (:require-macros [commons.mui])
  (:require
   [cljs.pprint :refer [pprint]]
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
   [commons.context :as context]
   [commons.form-ui :as form-ui]
   ))


(def StringVectorChips form-ui/StringVectorChips)
(def CommandCardArea form-ui/CommandCardArea)
(def FormCardArea form-ui/FormCardArea)
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
               :font-family :monospace}}
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
                       command]}]
  (let [command (u/trampoline-if command)
        text (or text (-> command :label) ":text missing")
        icon (when-let [icon (or icon (-> command :icon))]
               (if (string? icon)
                 (d/div {:class "i material-icons"} icon)
                 icon))
        onClick (or onClick
                    (-> command :onClick)
                    (when-let [form (-> command :form)]
                      #(show-form-dialog form)))
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


(defnc IconButton [{:keys [icon onClick color size command]}]
  (let [command (u/trampoline-if command)
        onClick (or onClick
                    (-> command :onClick)
                    (when-let [form (-> command :form)]
                      #(show-form-dialog form)))
        icon (when-let [icon (or icon
                                 (-> command :icon)
                                 "play_arrow")]
               (if (string? icon)
                 (d/div {:class "i material-icons"} icon)
                 icon))]
    ($ mui/IconButton
       {:onClick onClick
        :color color
        :size size}
       icon)))


(defnc SimpleCard [{:keys [title children className]}]
  ($ mui/Card
     {:className className}
     ($ mui/CardContent
        ($ Stack
           (when title
             ($ mui/Typography
                {:variant "overline"}
                title))
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

