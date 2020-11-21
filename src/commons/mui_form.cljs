(ns commons.mui-form
  (:require
   [clojure.spec.alpha :as s]
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


(defonce DIALOG_FORMS (atom {}))

(defn close-form-dialog [form-id]
  (swap! DIALOG_FORMS assoc-in [form-id :open?] false)
  (js/setTimeout #(swap! DIALOG_FORMS dissoc form-id) 1000))

(defn show-form-dialog [form]
  (let [form-id (random-uuid)
        form (assoc form
                    :open? true
                    :id form-id)]
    (swap! DIALOG_FORMS assoc form-id form)))

(def use-dialog-forms (ui/atom-hook DIALOG_FORMS))

(defnc DialogFormsDebugCard []
  ($ mui/Card
     ($ mui/CardContent
        (ui/data (use-dialog-forms)))))



(defmulti create-input (fn [field] (-> field :type (or "text"))))

(defmethod create-input "text" [field]
  ($ mui/TextField
     {
      :id (-> field :id name)
      :name (-> field :name)
      :defaultValue (-> field :value)
      :onChange #((:on-change field)
                  (-> % .-target .-value))
      :label (-> field :label)
      :type (-> field :type)
      :multiline (boolean (get field :rows))
      :rows (get field :rows)
      :autoFocus (-> field :auto-focus?)
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
      :name (-> field :name)
      :defaultValue (clj->js (-> field :value))
      :onChange #((:on-change field) (-> % js->clj))
      :dataSource (clj->js ["hallo" "welt"])
      :label (-> field :label)
      :autoFocus (-> field :auto-focus?)
      :margin "dense"
      :fullWidth true}))

(defmethod create-input "boolean" [field]
  ($ mui/FormControl
     {:component "fieldset"}
     ($ mui/FormLabel
        {:component "legend"}
        (-> field :label))
     ($ mui/RadioGroup
        {:name (-> field :name)
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


(defnc FormDialog [{:keys [form]}]
  (let [form-id (-> form :id)
        [form set-form] (hooks/use-state form)
        close (fn []
                (set-form (assoc form :open? false))
                (close-form-dialog form-id))
        submit (fn []
                 (log ::pre-submit
                      :form form)
                 (let [submit (get form :submit)
                       inputs (merge
                               (reduce (fn [inputs field]
                                         (assoc inputs
                                                (-> field :id)
                                                (-> field :value)))
                                       {} (get form :fields))
                               (-> form :values))]
                   (when-not submit
                     (throw (ex-info (str "Missing :submit function in form.")
                                     {:form form})))
                   (log ::submit
                        :form form
                        :inputs inputs)
                   (submit inputs))
                 (close))]
    (when (and (not (-> form :open?))
               (not (nil? (-> form :values))))
      (set-form (dissoc form :values)))
    (d/div
     ($ mui/Dialog
        {:open (-> form :open? boolean)
         :onClose close}
        ($ mui/DialogContent
           (for [[idx field] (map-indexed vector (get form :fields))]
             (let [type (or (-> field :type) "text")]
               (d/div
                {:key (-> field :id)}
                (create-input (assoc field
                                     :form form
                                     :on-change #(set-form (form/on-field-value-change
                                                            form (-> field :id) %))
                                     :auto-focus? (= 0 idx)
                                     :name (or (-> field :name)
                                               (-> field :id name))
                                     :label (or (-> field :label)
                                                (-> field :name)
                                                (-> field :id name)))))))
           (get form :content))
        ($ mui/DialogActions
           ($ mui/Button
              {:onClick close}
              "Abbrechen")
           ($ mui/Button
              {:onClick submit
               :variant "contained"
               :color "primary"}
              "Ok"))))))


(defnc FormDialogsContainer []
  (let [forms (use-dialog-forms)]
    (for [form (-> forms vals)]
      ($ FormDialog
         {:key (-> form :id)
          :form form}))))


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
