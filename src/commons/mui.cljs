(ns commons.mui
  (:require-macros [commons.mui])
  (:require
   [cljs.pprint :refer [pprint]]
   [cljs-bean.core :as cljs-bean]

   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]

   ["react" :as react]
   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as mui-styles]

   ["material-ui-chip-input" :default ChipInput]

   [commons.firestore :as fs]
   [commons.firestore-hooks :as firestore-hooks]
   ))


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
;;; Router
;;;

(defn use-params []
  (cljs-bean/->clj (router/useParams)))

(defn use-param [param-key]
  (-> (use-params) (get param-key)))

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

(defn atom-hook
  ([ATOM]
   (atom-hook ATOM identity))
  ([ATOM transformator]
   (fn use-atom []
     (let [[value set-value] (hooks/use-state @ATOM)
           watch-key (random-uuid)]

       (hooks/use-effect
        :once
        (add-watch ATOM watch-key
                   (fn [_k _r _ov nv]
                     (set-value nv)))
        #(remove-watch ATOM watch-key))

       (transformator value)))))

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


(defnc Button [{:keys [text icon onClick to href target variant color action]}]
  (let [text (or text (-> action :label) ":text missing")
        icon (when-let [icon (or icon (-> action :icon))]
               (if (string? icon)
                 (d/div {:class "i material-icons"} icon)
                 icon))]
    (if to
      ($ mui/Button
         {:to to
          :component router/Link
          :variant (or variant "contained")
          :color (or color "primary")
          :startIcon icon}
         text)
      ($ mui/Button
         {:onClick onClick
          :href href
          :target target
          :variant (or variant "contained")
          :color (or color "primary")
          :startIcon icon}
         text))))


(defnc SimpleCard [{:keys [title children]}]
  ($ mui/Card
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

(defnc FieldLabel [{:keys [text]}]
  (d/div
   {:style {:color "grey"}}
   text))

(defnc Field [{:keys [label children]}]
  ($ Stack
     {:spacing 0.5
      :class "EditableField"}
     ($ FieldLabel
        {:text label})
     (d/div
      {:style {:min-height "15px"}}
      children)))

(defnc FieldCardContent [{:keys [label children]}]
  ($ mui/CardContent
     ($ Field {:label label}
        children)))

(defnc StringVectorChips [{:keys [values]}]
  ($ Flexbox
     (for [value values]
       ($ mui/Chip
          {:key value
           :label value}))))
