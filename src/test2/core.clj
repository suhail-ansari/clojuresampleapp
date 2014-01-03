(ns test2.core
	(:use [compojure.route :only [files not-found]]
      [compojure.handler :only [site]]
      [compojure.core :only [defroutes GET POST DELETE ANY context]]
      org.httpkit.server)
	(:require [cheshire.core :refer :all])
	(:require [clojure.java.jdbc :as j])
	(:require clojure.stacktrace))


;;define and connect mysql;;
(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/cljex1"
               :user "dbuser"
               :password "dbpass"})

;;write data :data  to database :db
(defn write-data [data]
	(println data)
	(if (empty? data)
		false
		(do (try (j/insert! mysql-db (keyword (:db data)) (:data data)) true 
			(catch Exception e false)))))


;;get + jsonp request
(defn get-handler [{params :params}]
	(println params)
	(if (write-data (apply dissoc params [:_ :callback]))
		(do (if (contains? params :callback)
				(do {:status 200
					:headers {"Content-Type" "application/json"}
					:body (str (:callback params) "(" (generate-string {:status 200 :message "OK"}) ");")
				})
				(do {:status 200
					:headers {"Content-Type" "application/json"}
					:body (str (generate-string {:status 200 :message "OK"}))
				})))
		(do (if (contains? params :callback)
				(do {:status 200
					:headers {"Content-Type" "application/json"}
					:body (str (:callback params) "(" (generate-string {:status 101 :message "error"}) ");")
				})
				(do {:status 200
					:headers {"Content-Type" "application/json"}
					:body (str (generate-string {:status 101 :message "error"}))
				})))))

;;post request
(defn post-handler [{params :params}]
	(println params)
	(if (write-data (apply dissoc params [:_ :callback]))
		(do {:status 200
					:headers {"Content-Type" "application/json"}
					:body (str (generate-string {:status 200 :message "OK"}))
				})
		(do {:status 200
					:headers {"Content-Type" "application/json"}
					:body (str (generate-string {:status 101 :message "error"}))
				})))

;;404 error handler
(defn not-found-handler [params]
	(println params)
	{:status 404
	      :headers {"Content-Type" "application/json"}
	      :body  (str (generate-string {:status 404 :message "not found"}))})

(defroutes main-routes
	(GET "/" {params :params} get-handler)
	(POST "/" {params :params} post-handler)
	(not-found not-found-handler))

(defn -main []
	(println "main start")
	(run-server (site #'main-routes) {:port 8080}))
