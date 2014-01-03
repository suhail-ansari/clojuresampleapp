(ns test2.core
	(:use [compojure.route :only [files not-found]]
      [compojure.handler :only [site]]
      [compojure.core :only [defroutes GET POST DELETE ANY context]]
      org.httpkit.server)
	(:use [cheshire.core :only [generate-string parse-string]])
	(:require [clojure.java.jdbc :as j])
	(:gen-class))


(def not-contains? (complement contains?))


;;define and connect mysql;;
(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/cljex1"
               :user "dbuser"
               :password "dbpass"})

;;write data :data  to database :db
(defn write-data [data]
	(println data)
	(if (or (empty? data) (not-contains? data :db) (not-contains? data :data))
		[102 "empty/invalid parameters"]
		(do (try (j/insert! mysql-db (keyword (:db data)) (:data data)) [200 "OK"]
			(catch Exception e [101 "error"])))))


;;get + jsonp request
(defn get-handler [{params :params}]
	(let [result (write-data (apply dissoc params [:_ :callback]))]
		(do (if (contains? params :callback)
				(do {:status 200
					:headers {"Content-Type" "application/json" "Connection" "close"}
					:body (str (:callback params) "(" (generate-string {:status (nth result 0) :message (nth result 1)}) ");")
				})
				(do {:status 200
					:headers {"Content-Type" "application/json"}
					:body (str (generate-string {:status (nth result 0) :message (nth result 1)}))
				})))))

;;post request
(defn post-handler [{params :params}]
	(let [result (write-data (apply dissoc params [:_ :callback]))]
		(do {:status 200
					:headers {"Content-Type" "application/json" "Connection" "close"}
					:body (str (generate-string {:status (nth result 0) :message (nth result 1)}))
				})))

;;404 error handler
(defn not-found-handler [params]
	{:status 404
	      :headers {"Content-Type" "application/json" "Connection" "close"}
	      :body  (str (generate-string {:status 404 :message "not found"}))})


(defroutes main-routes
	(GET "/" {params :params} get-handler)
	(POST "/" {params :params} post-handler)
	(not-found not-found-handler))

(defn -main []
	(println "main start")
	(run-server (site #'main-routes) {:port 8080}))
