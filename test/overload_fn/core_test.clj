(ns overload-fn.core-test
  (:require [clojure.test :refer :all]
            [overload-fn.core :as of]))


(testing "protocol based overload"
  (with-test
   (of/defn my-fn
            ([^Double x]
             :double)
            ([^Long x]
             :long)
            ([^Object x]
             :any-type)
            ([x] nil))
   (is (= (my-fn 12.2) :double))
   (is (= (my-fn 12) :long))
   (is (= (my-fn :hey) :any-type))
   (is (= (my-fn nil) nil)))

  (with-test
   (of/defn add
            ([^Double x y z]
             (+ x y z))
            ([^Long x y z]
             (+ x y z 2)))
   (is (= (add 0.5 1 1) 2.5))
   (is (= (add 1 1 1) 5))))

(run-tests)
(comment

 )
