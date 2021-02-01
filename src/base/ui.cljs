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




