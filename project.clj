(defproject test2 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main test2.core
  :dependencies [[org.clojure/clojure "1.5.1"]
  				[http-kit "2.1.15"]
  				[compojure "1.1.6"]
  				[ring/ring-core "1.1.8"]
  				[cheshire "LATEST"]
  				[org.clojure/java.jdbc "0.3.2"]
  				[mysql/mysql-connector-java "5.1.25"]])
