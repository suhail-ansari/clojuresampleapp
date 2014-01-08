(ns test2.core
	(:gen-class)
	(:use [compojure.route :only [files not-found]]
	      [compojure.handler :only [site]]
	      [compojure.core :only [defroutes GET POST DELETE ANY context]]
	      [cheshire.core :only [generate-string parse-string]]
	      [ring.middleware.session :only [wrap-session]]
	      org.httpkit.server)
	(:require [clojure.java.jdbc :as j]))


(def not-contains? (complement contains?))
(def not-nil? (complement nil?))

;;define and connect mysql;;
(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/cljex1"
               :user "dbuser"
               :password "dbpass"})

;;write data :data  to database :db
(defn write-data [data]
	(if (or (empty? data) (not-contains? data :db) (not-contains? data :data))
		[102 "empty/invalid parameters"]
		(do (try (j/insert! mysql-db (keyword (:db data)) (:data data)) [200 "OK"]
			(catch Exception e [101 "error"])))))


;;get + jsonp request
(defn get-handler [request]
	(let [result (write-data (-> request :params (dissoc [:_ :callback]))) callback (-> request :params :callback )]
		{:status 200
			:headers {"Content-Type" "application/json" "Connection" "close"}
			:body (if (nil? callback) 
					(do (str (generate-string {:status (first result) :message (second result)}))))
					(do (str callback "(" (generate-string {:status (first result) :message (second result)}) ");"))
			}))

;;post request
(defn post-handler [request]
	(println (:session request))
	(let [result (write-data (-> request :params (dissoc [:_ :callback])))]
		(do {:status 200
					:headers {"Content-Type" "application/json" "Connection" "close"}
					:body (str (generate-string {:status (first result) :message (second result)}))
				})))


;;404 error handler
(defn not-found-handler [request]
	{:status 404
	      :headers {"Content-Type" "application/json" "Connection" "close"}
	      :body  (str (generate-string {:status 404 :message "not found"}))})



(defroutes main-routes
	(GET "/" request get-handler)
	(POST "/" request post-handler)
	(not-found not-found-handler))


(def app (site #'main-routes))

(defn -main [& args]
	(run-server app {:port 8080 ::queue-size 204800})
	(println "Started Server @ 127.0.0.1:8080"))
