(ns spa.nav
  (:require

   ["@material-ui/core" :as mui]

   [spa.api :refer [log]]
   [spa.auth :as auth]
   [spa.ui :as ui :refer [defnc $ <>]]
   ))

(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ ui/Stack
        ($ auth/CurrentUserCard))))
