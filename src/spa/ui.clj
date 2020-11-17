(ns spa.ui
  (:require
   [helix.core :as helix]
   [helix.hooks :as hooks]
   [helix.dom :as d]

   [commons.mui :as cmui]))


;;;
;;; Helix
;;;

(defmacro defnc [& body] `(cmui/defnc ~@body))
(defmacro $ [type & args] `(helix/$ ~type ~@args))
(defmacro <> [& children] `(helix/<> ~@children))

;;;
;;; Hooks
;;;

(defmacro use-state [& body] `(hooks/use-state ~@body))
(defmacro use-effect [& body] `(hooks/use-effect ~@body))

;;;
;;; DOM
;;;

(defmacro div [& body] `(d/div ~@body))
(defmacro span [& body] `(d/span ~@body))
(defmacro img [& body] `(d/img ~@body))

