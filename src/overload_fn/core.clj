(ns overload-fn.core
  (:require [clojure.string :as str]))


(defn use-protocol? [types]
  (not
   (boolean
    (some (fn [args]
            (let [args* (rest args)]
              (when-not (or (empty? args*)
                            (every? nil? args*))
                args))) types))))


(defmacro defno
  [name & body]
  ;;TODO checks
  (let [types (map (comp (partial mapv (comp :tag meta))
                         first) body)]
    (if (use-protocol? types)
      ;;TODO camelCase | my-add -> IMy-addProtocol
      (let [protocol-name (symbol (str "I" (str/capitalize name) "Protocol"))]
        `(do
           (defprotocol ~protocol-name
             (~name ~@(distinct
                       (map (fn [args]
                              (vec (cons 'this (rest args))))
                            (distinct (map first body))))))
           (extend-protocol ~protocol-name
             ~@(apply concat
                      (for [[k v] (group-by (comp :tag meta ffirst) body)]
                        (list k (cons name v)))))))
      `(do
         (println "Using multi-methods")
         (let [v# (def ~name)]
           (when (bound? v#)
             (println "removing bind")
             (.unbindRoot v#)))
         (defmulti ~name (fn [& args#] (mapv type args#)))
         ~@(for [[args form] body]
             `(defmethod ~name ~(mapv (comp :tag meta) args) ~args
                ~form))))))

(comment
 (macroexpand-1 '(defno my-add2
                        ([^Long x ^Double y]
                         (println "Long - Double"))
                        ([^Long x ^Float y]
                         (println "Long - Float"))
                        ([^String x ^String y]
                         (println "string - string"))
                        ([x y]
                         (println "nil - nil"))))
 (defno my-add2
        ([^Long x ^Double y]
         (println "Long - Double"))
        ([^Long x ^Float y]
         (println "Long - Float"))
        ([^String x ^String y]
         (println "string - string"))
        ([x y]
         (println "nil - nil")))
 (my-add2 12 12.2)
 (my-add2 12 (float 12.2))
 (my-add2 nil nil)
 (my-add2 "ss" "asd")

 (macroexpand-1 '(defno mything
                        ([^Double y]
                         (println "Double"))
                        ([^Float y]
                         (println "Float"))
                        ([y]
                         (println "nil"))))
 (mything nil)
 )

