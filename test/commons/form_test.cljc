(ns commons.form-test
  (:require
   [commons.form :as sut]
   [cljs.test :as t :refer [deftest is] :include-macros true]

   ))

(def name-field {:id :name
                 :label "Name"
                 :type "text"})

(def adr-form
  {:fields [name-field
            {:id :city
             :label "City"}]
   :submit #(println "SUBMIT:" %)})


(deftest field-by-id-test
  (is (= name-field (sut/field-by-id adr-form :name))))


(deftest field-type-test
  (is (= "text" (sut/field-type adr-form :name)))
  (is (= "text" (sut/field-type adr-form :city))))
