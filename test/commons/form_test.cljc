(ns commons.form-test
  (:require
   [commons.form :as sut]
   [cljs.test :as t :refer [deftest is] :include-macros true]

   ))


(deftest field-by-id-test
  (is (= {:id :name :label "Name"}
         (-> {:fields [{:id :name
                        :label "Name"}
                       {:id :city
                        :label "City"}]}
             (sut/field-by-id :name)))))
