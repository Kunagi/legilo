(ns amazon.ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.mui :as ui :refer [defnc $ <> div]]))


(def partner-id "legilo-21")


(defn href [asin]
  (str "https://www.amazon.de/gp/product/" asin
       "?tag=" partner-id))

(defn search-href [text]
  (str "https://www.amazon.de/s"
       "?tag=" partner-id
       "&k=" text))

(defn image-url [asin]
  (str "//ws-eu.amazon-adsystem.com/widgets/q?MarketPlace=DE&ASIN="
       asin
       "&ServiceVersion=20070822&ID=AsinImage&WS=1&Format=_SL160_&tag="
       partner-id))

(defn ImageLink [asin]
  ($ :a
     {:target "_blank"
      :href (href asin)}
     ($ :img
        {:border 0
         :src (image-url asin)}
        )))


(defnc SearchWidget[{:keys [title]}]
  (div
   {:id "AMAZSEARCH"}
   ($ :script (str "
amzn_assoc_ad_type ='responsive_search_widget';
amzn_assoc_tracking_id ='" partner-id "';
amzn_assoc_marketplace ='amazon';
amzn_assoc_region ='DE';
amzn_assoc_placement ='';
amzn_assoc_search_type = 'search_widget';
amzn_assoc_width ='300';
amzn_assoc_height ='250';
amzn_assoc_default_search_category ='';
amzn_assoc_default_search_key ='" title "';
amzn_assoc_theme ='light';
amzn_assoc_bg_color ='FFFFFF';"))
   ($ :script
      {:src "https://z-eu.amazon-adsystem.com/widgets/q?ServiceVersion=20070822&Operation=GetScript&ID=OneJS&WS=1&Marketplace=DE"})))
