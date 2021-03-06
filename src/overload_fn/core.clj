(ns overload-fn.core
  (:refer-clojure :exclude [defn])
  (:require
   [clojure.string :as str]
   [clojure.spec.alpha :as s])
  (:import
   (clojure.lang Var)))


(s/check-asserts true)

(s/def ::distinct (partial apply distinct?))


(defn- use-protocol? [types]
  (not
   (boolean
    (some (fn [args]
            (let [args* (rest args)]
              (when-not (or (empty? args*)
                            (every? nil? args*))
                args))) types))))


(defn- create-protocol-name [name]
  (format "I%sProtocol" (->> name
                             str
                             (re-seq #"[a-zA-Z]+")
                             (map str/capitalize)
                             (str/join))))


(defmacro defn
  [name & body]
  (let [types (map (comp (partial mapv (comp :tag meta))
                         first) body)]
    (s/assert ::distinct types)
    (if (use-protocol? types)
      (let [protocol (symbol (create-protocol-name name))]
        `(do
           (when-let [protocol# (resolve '~protocol)]
             (when (bound? protocol#)
               (.unbindRoot ^Var (:var @protocol#))))
           (defprotocol ~protocol
             (~name ~@(distinct
                       (map (fn [args]
                              (vec (cons 'this (rest args))))
                            (distinct (map first body))))))
           (extend-protocol ~protocol
             ~@(apply concat
                      (for [[k v] (let [mapping# (group-by (comp :tag meta ffirst) body)]
                                    ;; Unify Object and nil
                                    (cond-> mapping#
                                            (get mapping# nil) (assoc Object (get mapping# nil))
                                            (get mapping# 'Object) (assoc nil (get mapping# 'Object))))]
                        (list k (cons name v)))))
           (def ~name)))
      `(do
         (let [v# (def ~name)]
           (when (bound? v#)
             (.unbindRoot v#)))
         (let [->class# (fnil class Object)]
           (defmulti ~name (fn [& args#] (mapv ->class# args#))))
         ~@(for [[args form] body]
             `(defmethod ~name ~(mapv (fn [a#]
                                        (or (-> a# meta :tag)
                                            Object)) args) ~args
                ~form))
         (def ~name)))))
