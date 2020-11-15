(ns spa.ui
  (:require-macros [spa.ui])
  (:require
   [cljs.pprint :refer [pprint]]
   [cljs-bean.core :as cljs-bean]

   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]

   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as mui-styles]

   [spa.api :refer [log]]

   [spa.impl.firestore-hooks :as firestore-hooks]
   [spa.impl.firestore :as fs]
   ))

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

(def Route router/Route)
(def Link router/Link)

(defn use-params []
  (cljs-bean/->clj (router/useParams)))

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

(def use-col firestore-hooks/use-col)
(def use-doc firestore-hooks/use-doc)

(defn atom-hook [ATOM]
  (fn use-atom []
    (let [[value set-value] (hooks/use-state @ATOM)
          watch-key (random-uuid)]

      (hooks/use-effect
       :once
       (add-watch ATOM watch-key
                  (fn [_k _r _ov nv]
                    (set-value nv)))
       #(remove-watch ATOM watch-key))

      value)))



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


;;;
;;; common components
;;;


(defnc Spacer[{:keys [width height]}]
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
              :gap (-> theme (.spacing (or spacing 1)))}}
     children)))


(defnc CardOverline [{:keys [children]}]
  ($ mui/Typography
     {:component "h2"
      :variant "overline"}
     children))

(defnc CardRow [{:keys [children]}]
  (d/div
   {:style {:display :flex}}
   children))

(defnc Field [{:keys [label children]}]
  ($ Stack
     {:spacing 0.5
      :class "EditableField"}
     (d/div
      {:style {:color "grey"}}
      label)
     (d/div
      {:style {:min-height "15px"}}
      children)))


(defnc FieldCardContent [{:keys [label children]}]
  ($ mui/CardContent
     ($ Field {:label label}
        children)))




;;;
;;; Dialog Form
;;;

(defonce DIALOG_FORM (atom nil))

(defn close-form-dialog []
  (swap! DIALOG_FORM assoc :open? false))

(defn show-form-dialog [form]
  (reset! DIALOG_FORM (assoc form :open? true)))

(def use-dialog-form (atom-hook DIALOG_FORM))

(defnc DialogFormDebugCard []
  ($ mui/Card
     ($ mui/CardContent
        (data (use-dialog-form)))))


(defnc FormDialog [{:keys []}]
  (let [form (use-dialog-form)
        [inputs set-inputs] (hooks/use-state nil)
        convert-for-output (fn [value type]
                             (if (or (nil? value)
                                     (= "" value))
                               nil
                               (case type
                                 "number" (js/parseInt value)
                                 value)))
        update-input (fn [event type]
                       (set-inputs
                        (assoc inputs
                               (-> event .-target .-id keyword)
                               (-> event .-target .-value (convert-for-output type)))))
        submit (fn []
                 (log ::submit :form form :inputs inputs)
                 (let [submit (get form :submit)]
                   (when-not submit
                     (throw (ex-info (str "Missing :submit function in form.")
                                     {:form form})))
                   (when (or inputs (-> form :submit-unchanged?))
                     (submit inputs)))
                 (close-form-dialog))]
    (when (and (not (-> form :open?))
               (not (nil? inputs)))
      (set-inputs nil))
    (d/div
     ($ mui/Dialog
        {:open (-> form :open? boolean)
         :onClose close-form-dialog}
        ($ mui/DialogContent
           (for [[idx field] (map-indexed vector (get form :fields))]
             (let [type (or (-> field :type) "text")]
               ($ mui/TextField
                  {:key (-> field :id)
                   :id (-> field :id name)
                   :name (or (-> field :name)
                             (-> field :id name))
                   :defaultValue (get field :value)
                   :onChange #(update-input % type)
                   :label (get field :label)
                   :autoFocus (= 0 idx)
                   :type type
                   :inputProps (if-let [props (-> field :input-props)]
                                 (clj->js props)
                                 (clj->js {}))
                   :margin "dense"
                   :fullWidth true})))
           (get form :content))
        ($ mui/DialogActions
           ($ mui/Button
              {:onClick close-form-dialog}
              "Abbrechen")
           ($ mui/Button
              {:onClick submit
               :variant "contained"
               :color "primary"}
              "Ok"))))))


(defnc EditableCardActionArea [{:keys [form children]}]
  ($ mui/CardActionArea
     {:onClick #(show-form-dialog form)}
     children))


(defnc StringVectorChips [{:keys [values]}]
  ($ Flexbox
   (for [value values]
     ($ mui/Chip
        {:key value
         :label value}))))


(defnc EditableFieldCardActionArea [{:keys [doc field]}]
  (let [id (get field :id)
        label (get field :label)
        value (get doc id)
        submit #(fs/update-doc> doc {id (get % id)})
        type (get field :type)]
    ($ EditableCardActionArea
       {:form {:fields [(assoc field :value value)]
               :submit submit}}
       ($ mui/CardContent
          ($ Field
             {:label label}
             (case type
               :chips ($ StringVectorChips {:values value})
               (str value)))))))


(defnc EditableFieldCard [{:keys [doc field]}]
  ($ mui/Card
     ($ EditableFieldCardActionArea
        {:doc doc
         :field field})))
