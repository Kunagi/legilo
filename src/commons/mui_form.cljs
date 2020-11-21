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
   [commons.form :as form]

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



(defmulti create-input (fn [field] (-> field :type (or "text"))))

(defmethod create-input "text" [field]
  ($ mui/TextField
     {
      :id (-> field :id name)
      :name (or (-> field :name)
                (-> field :id name))
      :defaultValue (get field :value)
      :onChange #((:on-change field)
                  (-> % .-target .-value))
      :label (or (-> field :label)
                 (-> field :name)
                 (-> field :id name))
      :autoFocus (-> field :auto-focus?)
      :type (-> field :type)
      :multiline (boolean (get field :rows))
      :rows (get field :rows)
      :inputProps (if-let [props (-> field :input-props)]
                    (clj->js props)
                    (clj->js {}))
      :margin "dense"
      :fullWidth true}))

(defmethod create-input "tel" [field]
  (create-input (assoc field :type "text")))

(defmethod create-input "time" [field]
  (create-input (assoc field :type "text")))

(defmethod create-input "date" [field]
  (create-input (assoc field :type "text")))

(defmethod create-input "number" [field]
  (create-input (assoc field :type "text")))

(defmethod create-input "chips" [field]
  ($ ChipInput
     {
      :id (-> field :id name)
      :name (or (-> field :name)
                (-> field :id name))
      :defaultValue (clj->js (-> field :value))
      :onChange #((:on-change field) (-> % js->clj))
      :dataSource (clj->js ["hallo" "welt"])
      :label (-> field :label)
      :autoFocus (-> field :auto-focus?)
      ;; :inputProps (if-let [props (-> field :input-props)]
      ;;               (clj->js props)
      ;;               (clj->js {}))
      :margin "dense"
      :fullWidth true}))

(defmethod create-input "boolean" [field]
  ($ mui/FormControl
     {:component "fieldset"}
     ($ mui/FormLabel
        {:component "legend"}
        (-> field :label))
     ($ mui/RadioGroup
        {:name (or (-> field :name)
                   (-> field :id name))
         :defaultValue (if (-> field :value) "true" "false")
         :onChange #((:on-change field) (= "true" (-> % .-target .-value)))}
        ($ mui/FormControlLabel
           {:value "true"
            :label "Ja"
            :control ($ mui/Radio)})
        ($ mui/FormControlLabel
           {:value "false"
            :label "Nein"
            :control ($ mui/Radio)}))))


(defnc FormDialog [{:keys []}]
  (let [form-spec (use-dialog-form)
        [form set-form] (hooks/use-state form-spec)
        update-input (fn [id value]
                       ;; (log ::update-input
                       ;;      :id id
                       ;;      :value value
                       ;;      :type type
                       ;;      :converted-value (convert-for-output value type))
                       (set-form
                        (assoc-in form
                                  [:values id]
                                  (form/convert-value-for-output value form id))))

        submit (fn []
                 (log ::pre-submit
                      :form-spec form-spec
                      :form form)
                 (let [submit (get form-spec :submit)
                       inputs (merge
                               (reduce (fn [inputs field]
                                         (assoc inputs
                                                (-> field :id)
                                                (-> field :value)))
                                       {} (get form-spec :fields))
                               (-> form :values))]
                   (when-not submit
                     (throw (ex-info (str "Missing :submit function in form.")
                                     {:form form
                                      :form-spec form-spec})))
                   (log ::submit
                        :form-spec form-spec
                        :form form)
                   (submit inputs))
                 (close-form-dialog))]
    (when (and (not (-> form-spec :open?))
               (not (nil? (-> form :values))))
      (set-form (dissoc form :values)))
    (d/div
     ($ mui/Dialog
        {:open (-> form-spec :open? boolean)
         :onClose close-form-dialog}
        ($ mui/DialogContent
           (for [[idx field] (map-indexed vector (get form-spec :fields))]
             (let [type (or (-> field :type) "text")]
               (d/div
                {:key (-> field :id)}
                (create-input (assoc field
                                     :form form
                                     :on-change (partial update-input (-> field :id))
                                     :auto-focus? (= 0 idx))))))
           (get form-spec :content))
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


(defnc EditableFieldCardActionArea [{:keys [doc doc-path field update-wrapper]}]
  (let [id (get field :id)
        label (get field :label)
        value (get doc id)
        submit #(let [changes {id (get % id)}
                      changes (if update-wrapper
                                (update-wrapper changes)
                                changes)]
                  (fs/update-fields> (or doc doc-path) changes))
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
