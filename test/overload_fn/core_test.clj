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
            ([x]
             :any-type))
   (is (= (my-fn 12.2) :double))
   (is (= (my-fn 12) :long))
   (is (= (my-fn :hey) :any-type))
   (is (= (my-fn nil) :any-type))
   (is (= (my-fn {:a 1}) :any-type)))

  (with-test
     (of/defn add
              ([^Double x y z]
               (+ x y z))
              ([^Long x y z]
               (+ x y z 2)))
     (is (= (add 0.5 1 1) 2.5))
     (is (= (add 1 1 1) 5))))


(testing "multi-methods based overload"
    (with-test
     (of/defn my-multi-fn
              ([^Double x ^String y]
               :double-string)
              ([^Long x ^String y]
               :long-string)
              ([^Object x ^String y]
               :any-string)
              ([x ^String y]
               :any-string))
     (is (= (my-multi-fn 12.2 "hey") :double-string))
     (is (= (my-multi-fn 12 "hey") :long-string))
     (is (= (my-multi-fn nil "hey") :any-string))
     (is (= (my-multi-fn {:a 1} "hey") :any-string))
     (is (= (my-multi-fn :dude "hey") :any-string)))

    (with-test
     (of/defn my-multi-fn-2
              ([^Long x ^String y ^Long z]
               [:long :str :long])
              ([^Long x ^String y ^Number z]
               [:long :str :number])
              ([^Long x ^String y z]
               [:long :str :any]))
     (is (= (my-multi-fn-2 1 "hey" 2) [:long :str :long]))
     (is (= (my-multi-fn-2 1 "hey" 5.5) [:long :str :number]))
     (is (= (my-multi-fn-2 1 "hey" :dude) [:long :str :any]))
     (is (= (my-multi-fn-2 1 "hey" {:a 1}) [:long :str :any]))
     (is (= (my-multi-fn-2 1 "hey" nil) [:long :str :any]))))


(run-tests)


(comment

 (#'protocol? IErtusProtocol)
 (of/defn ertus
          ([^Double x]
           :double)
          ([^Long x]
           :long)
          ([^Object x]
           :any-type))

 (macroexpand-1 '(of/defn my-fn
                          ([^Double x]
                           :double)
                          ([^Long x]
                           :long)
                          ([x]
                           :any-type))))
