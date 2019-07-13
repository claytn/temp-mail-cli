(defproject temp-mail-cli "0.1.0-SNAPSHOT"
  :description "command line tool for temp-mail.org"
  :url "https://github.com/marsha88/temp-mail-cli"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cheshire "5.6.1"]
                 [clj-http "3.10.0"]
                 [hickory "0.7.1"]
                 [digest "1.4.9"]
                 [org.apache.httpcomponents/httpclient "4.5.8"]
                 [clj-commons/spinner "0.6.0"]]
  :main ^:skip-aot temp-mail-cli.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
