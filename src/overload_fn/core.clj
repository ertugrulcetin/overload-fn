(ns overload-fn.core
  (:refer-clojure :exclude [defn])
  (:require [clojure.string :as str]))


(defn- use-protocol? [types]
  (not
   (boolean
    (some (fn [args]
            (let [args* (rest args)]
              (when-not (or (empty? args*)
                            (every? nil? args*))
                args))) types))))


(defmacro defn
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
                        (list k (cons name v)))))
           (def ~name)))
      `(do
         (let [v# (def ~name)]
           (when (bound? v#)
             (.unbindRoot v#)))
         (defmulti ~name (fn [& args#] (mapv type args#)))
         ~@(for [[args form] body]
             `(defmethod ~name ~(mapv (comp :tag meta) args) ~args
                ~form))
         (def ~name)))))


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
 (defn my-add2
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

 (macroexpand-1 '(defn mything
                       ([^Double y]
                        (println "Double"))
                       ([^Float y]
                        (println "Float"))
                       ([y]
                        (println "nil"))))
 (mything 12.2)
 (mything nil)
 (defprotocol IErtu (done [this]))
 (extend-protocol IErtu
   Double
   (done [this]))
 #'done
 (.unbindRoot v#)
 (bound? #'done)
 )

