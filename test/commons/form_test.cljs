(ns commons.form-test
  (:require [commons.form :as sut]
            [cljs.test :as t :include-macros true]))


(t/deftest dummy-1
  (t/is (= 1 1)))

