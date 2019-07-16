(ns temp-mail-cli.core
  (:use [hickory.core])
  (:require
   [clojure.core.reducers :as r]
   [clj-http.client :as client]
   [hickory.select :as s]
   [digest]
   [spinner.core :as spin]
   [temp-mail-cli.clipboard :as clipboard])
  (:import
   (org.apache.http.impl.client HttpClientBuilder))
  (:gen-class))

(def base-url "https://privatix-temp-mail-v1.p.rapidapi.com/request/mail/id/")

(def headers {"X-RapidAPI-Host" "privatix-temp-mail-v1.p.rapidapi.com"
              "X-RapidAPI-Key" "<API-KEY>"})

(defn cookie-disabler [^HttpClientBuilder builder
                       request]
  (when (:disable-cookies request)
    (.disableCookieManagement builder)))

(defn email-request [email-hash]
  "Given an md5 hash of a provided email - makes request for all messages in inbox"
  (:body (client/get (str base-url email-hash "/")
                     {:headers headers
                      :http-builder-fns [cookie-disabler]
                      :disable-cookies true
                      :cookie-policy :standard
                      :as :json})))

(defn format-email [{error :error
                     from :mail_from
                     subject :mail_subject
                     message :mail_text}]
  (if error
    "Inbox Empty"
    (str "From: " from "\nSubject: " subject "\n\n" message "\n")))

(defn format-inbox [emails]
  (r/fold str (map format-email emails)))

(defn print-inbox [emails]
  (if emails (println (str "\n" (format-inbox emails)))
      (println "\n📭 Inbox Empty 📭\n")))

(defn fetch-inbox [email]
  (let [api-resp (-> email digest/md5 email-request)]
    (if (:error api-resp) nil api-resp)))

(defn generate-email []
  "Scrapes the temp-email site for new email address - returns the email as a string"
  (let [parsed-html (-> (client/get "https://temp-mail.org/en/" {:cookie-policy :standard})
                        :body
                        parse
                        as-hickory)
        email-address (-> (s/select (s/id "mail") parsed-html)
                          first
                          :attrs
                          :value)]
    email-address))

(defn print-new-email [email]
  (println (str "✉️  Temporary Email: " email)))

(defn load-data-with-spinner [f]
  "Displays spinning animation while function f is running - result of f is returned"
  (let [s (spin/create-and-start!
           {:frames (:dot-around spin/styles) :fg-colour :white})
        result (f)]
    (spin/stop! s)
    result))

(defn email-copied-notification []
  (println "✅ Email copied to clipboard"))

(defn print-usage []
  "Usage:\nGenerating a temporary email: ghost-app\n
   Opening your temporary email inbox: ghost-app <email>\n")

(defn -main
  [& args]
  (let [len (count args)]
    (cond
      (= len 0) (let [temp-email (load-data-with-spinner generate-email)]
                  (clipboard/spit-clipboard temp-email)
                  (email-copied-notification)
                  (print-new-email temp-email))
      (= len 1) (let [inbox (load-data-with-spinner #(fetch-inbox (first args)))]
                  (print-inbox inbox))
      :else (print-usage))))
