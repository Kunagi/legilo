(ns base.ui
  (:require-macros [base.ui])
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

   [commons.mui :as cmui]
   [commons.logging :refer [log]]
   [commons.firestore :as fs]
   [commons.firestore-hooks :as firestore-hooks]

   [commons.context :as c.context]
   ))


;;;
;;; Router
;;;

(def Route router/Route)
(def Link router/Link)

;;;
;;; styles
;;;

(def style-bg-img cmui/style-bg-img)

(def use-theme mui-styles/useTheme)

;;;
;;; Hooks
;;;

(def use-col firestore-hooks/use-col)
(def use-doc firestore-hooks/use-doc)

(def atom-hook cmui/atom-hook)

;;;
;;; common ui functions
;;;

(def data cmui/data)
(def icon cmui/icon)

;;;
;;; common components
;;;

(def Spacer cmui/Spacer)
(def Stack cmui/Stack)
(def Flexbox cmui/Flexbox)

(def CardRow cmui/CardRow)

(def FieldLabel cmui/FieldLabel)
(def Field cmui/Field)
(def FieldCardContent cmui/FieldCardContent)


(defnc Guard [{:keys [children]}]
  (if (c.context/use-uid)
    children
    (d/div "Sign in required")))


(defnc UserGuard [{:keys [children]}]
  (if (c.context/use-uid)
    children
    "loading..."))
