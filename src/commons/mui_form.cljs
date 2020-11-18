(ns commons.mui-form
  (:require
   [cljs.pprint :refer [pprint]]
   [cljs-bean.core :as cljs-bean]

   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]

   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as mui-styles]

   ["material-ui-chip-input" :default ChipInput]

   [commons.logging :refer [log]]
   [commons.mui :as ui]

   [commons.firestore :as fs]
   ))


(defonce DIALOG_FORM (atom nil))

(defn close-form-dialog []
  (swap! DIALOG_FORM assoc :open? false))

(defn show-form-dialog [form]
  (reset! DIALOG_FORM (assoc form :open? true)))

(def use-dialog-form (ui/atom-hook DIALOG_FORM))

(defnc DialogFormDebugCard []
  ($ mui/Card
     ($ mui/CardContent
        (ui/data (use-dialog-form)))))



(defmulti create-input (fn [type field update-input auto-focus?] type))

(defmethod create-input "text" [_type field update-input auto-focus?]
  ($ mui/TextField
     {
      :id (-> field :id name)
      :name (or (-> field :name)
                (-> field :id name))
      :defaultValue (get field :value)
      :onChange #(update-input
                  (-> field :id)
                  (-> % .-target .-value)
                  (-> field :type))
      :label (get field :label)
      :autoFocus auto-focus?
      :type (-> field :type)
      :multiline (boolean (get field :rows))
      :rows (get field :rows)
      :inputProps (if-let [props (-> field :input-props)]
                    (clj->js props)
                    (clj->js {}))
      :margin "dense"
      :fullWidth true}))

(defmethod create-input "tel" [_type field update-input auto-focus?]
  (create-input "text" field update-input auto-focus?))

(defmethod create-input "time" [_type field update-input auto-focus?]
  (create-input "text" field update-input auto-focus?))

(defmethod create-input "date" [_type field update-input auto-focus?]
  (create-input "text" field update-input auto-focus?))

(defmethod create-input "number" [_type field update-input auto-focus?]
  (create-input "text" field update-input auto-focus?))

(defmethod create-input "chips" [_type field update-input auto-focus?]
  ($ ChipInput
     {
      :id (-> field :id name)
      :name (or (-> field :name)
                (-> field :id name))
      :defaultValue (clj->js (-> field :value))
      :onChange #(update-input (-> field :id)
                               (-> % js->clj)
                               (-> field :type))
      :dataSource (clj->js ["hallo" "welt"])
      :label (get field :label)
      :autoFocus auto-focus?
      ;; :inputProps (if-let [props (-> field :input-props)]
      ;;               (clj->js props)
      ;;               (clj->js {}))
      :margin "dense"
      :fullWidth true}))


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
        update-input (fn [id value type]
                       ;; (log ::update-input
                       ;;      :id id
                       ;;      :value value
                       ;;      :type type
                       ;;      :converted-value (convert-for-output value type))
                       (set-inputs
                        (assoc inputs
                               id
                               (convert-for-output value type))))

        submit (fn []
                 (log ::pre-submit :form form :inputs inputs)
                 (let [submit (get form :submit)
                       inputs (merge
                               (reduce (fn [inputs field]
                                         (assoc inputs
                                                (-> field :id)
                                                (-> field :value)))
                                       {} (get form :fields))
                               inputs)]
                   (when-not submit
                     (throw (ex-info (str "Missing :submit function in form.")
                                     {:form form})))
                   (when (or inputs (-> form :submit-unchanged?))
                     (log ::submit :form form :inputs inputs)
                     (submit inputs)))
                 (close-form-dialog))
]
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
               (d/div
                {:key (-> field :id)}
                (create-input type field update-input (= 0 idx)))))
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
          ($ ui/Field
             {:label label}
             (case type
               "chips" ($ ui/StringVectorChips {:values value})
               (str value)))))))


(defnc EditableFieldCard [{:keys [doc field]}]
  ($ mui/Card
     ($ EditableFieldCardActionArea
        {:doc doc
         :field field})))
