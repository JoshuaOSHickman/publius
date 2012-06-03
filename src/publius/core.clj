(ns publius.core
  (:use lamina.core aleph.http ring.adapter.jetty
        [ns-tracker.core :only (ns-tracker)])
  (:require [cheshire.core :as json]
            ring.middleware.params
            ring.middleware.cookies
            [monger.core :as mg]
            [monger.collection :as mc]))

(defn print-errors [f]
  (fn [& args]
    (try (apply f args)
         (catch Exception e (do
                              (prn e)
                              (println (apply str (interpose "\n    " (seq (.getStackTrace e))))))))))

(def not-found
  (print-errors
   (fn
     ([request]
        {:status 404
         :headers {}
         :body "Huh... wut... you haven't found what you were looking for synchronously."})
     ([response-ch request]
        (enqueue response-ch
                 {:status 404
                  :headers {}
                  :body "I... no... what are you looking for asynchronously?"})))))

(def publius-root "/publius/")
(def publius-middleware (atom []))
(def publius-functions (atom {}))

(def publius-app-middleware
  (reify clojure.lang.IDeref
    (deref [_]
      (reduce comp (concat (vals @publius-functions)
                           (reverse @publius-middleware))))))

(def publius-app
  (reify clojure.lang.IDeref
    (deref [_]
      (reduce #(%2 %1) not-found
              (concat (vals @publius-functions)
                      (reverse @publius-middleware))))))

(defn modifying-middleware [request-mod response-mod]
  (fn [app]
    (print-errors
     (fn
       ([req] (-> req request-mod app response-mod))
       ([resp-ch req]
          (let [c (read-channel (channel))]
            (on-realized c
                         (fn [resp] (enqueue resp-ch (response-mod resp)))
                         (fn [err] (not-found resp-ch req)))
            (app c (request-mod req))))))))

;; TODO this can be done with modifying-middleware, can't it?
;; something like
;  (fn [app]
;    (fn [& args]
;      (let [a (atom)]
;        ( ... ? ...
;  )
(defn data-keeping-middleware [request-processor response-processor]
  (fn [app]
    (print-errors
     (fn
       ([req]
          (let [a (atom nil)
                req (request-processor req a)
                resp (app req)
                resp (response-processor resp a)]
            resp))
       ([resp-ch req]
          (let [a (atom nil)
                c (read-channel (channel))]
            (on-realized c
                         (fn [resp] (enqueue resp-ch (response-processor resp a)))
                         (fn [err] (not-found resp-ch req)))
            (app c (request-processor req a))))))))

(defn short-circuiting-middleware [predicate result]
  (fn [app]
    (print-errors
     (fn
      ([req] (if (predicate req)
               (result req)
               (app req)))
      ([resp-ch req]
         (if (predicate req)
           (result resp-ch req)
           (app resp-ch req)))))))

(defn path-catching-middleware
  ([path dispatch] (path-catching-middleware path :any dispatch))
  ([path method dispatch]
     (let [path-check (fn [req]
                        (let [uri (:uri req)
                              req-method (:request-method req)]
                          (and (or (= :any method) (= method req-method))
                               (= uri path))))]
       (short-circuiting-middleware path-check dispatch))))

(def assoc-form-params @#'ring.middleware.params/assoc-form-params)
(def assoc-query-params @#'ring.middleware.params/assoc-query-params)

(def wrap-params
  (modifying-middleware
   (fn [req]
     (let [enc (or (:character-encoding req)
                   "UTF-8")
           req (if (:form-params req)
                 req
                 (assoc-form-params req enc))
           req (if (:query-params req)
                 req
                 (assoc-query-params req enc))]
       req))
   identity))

(defn publius-middleware! [middleware]
  (swap! publius-middleware conj middleware))

(publius-middleware! wrap-params)


(mg/connect!)
(mg/set-db! (mg/get-db "publius-data"))

(defprotocol PubliusDataStore
  "Publius needs a high-volume persistent key-value datastore."
  (publius-grab [self visitor-id kind] [self data-id] "Getter for user data of all kinds, as well as semi-public data")
  (publius-put [self visitor-id kind data] [self data-id data] "Setter for user data of all kinds, as well as semi-public data")
  (publius-initialize [self kind] "Initialization for a new kind of data it's storing"))

(deftype MongoDataStore [] ; todo: support non-standard setups
  PubliusDataStore
  (publius-put [self visitor-id kind data]
    (mc/update kind {:visitor-id visitor-id} {:visitor-id visitor-id :value data} :upsert true))
  (publius-put [self data-id data]
    (mc/update "public-data" {:data-id data-id} {:value data} :upsert true))
  (publius-grab [self visitor-id kind]
    (:value (mc/find-one-as-map kind {:visitor-id visitor-id})))
  (publius-grab [self data-id]
    (:value (mc/find-one-as-map "public-data" {:data-id data-id})))
  (publius-initialize [self kind]
    (mc/ensure-index "public-data" {:data-id 1})
    (mc/ensure-index kind {:visitor-id 1})))

(def ^:dynamic *publius-db* (MongoDataStore.))

(def parse-cookies @#'ring.middleware.cookies/parse-cookies)
(def set-cookies @#'ring.middleware.cookies/set-cookies)

(publius-middleware!; cookies
 (modifying-middleware
   (fn [request]
    (if (request :cookies) request
      (assoc request :cookies (parse-cookies request))))
   (fn [response]
     (dissoc (set-cookies response) :cookies))))

(publius-middleware!; visitor-ids
 (data-keeping-middleware
  (fn [req a]
    (if ((:cookies req) "identity")
      (assoc req :identity (get-in req [:cookies "identity" :value]))
      (do
        (swap! a (fn [_] (.toString (java.util.UUID/randomUUID))))
        (assoc req :identity @a))))
  (fn [resp a]
    (if (not (nil? @a))
      (assoc-in resp [:cookies "identity"] @a)
      resp))))

(defn code-contains [code item]
  (if (coll? code)
    (some #(code-contains % item) (seq code))
    (= code item)))

(defmacro publius-destination! [name & body]
  `(let [new-middleware# (path-catching-middleware
                          ~(str publius-root name)
                          (fn
                            ([~'request] ~@body); below pattern allows you to use response channel if needed
                            ([~'response-channel ~'request]
                               ~(if (code-contains body 'response-channel) ; obviously, if it's queued before the block ends
                                  ;; This branch doesn't matter (it just ignores the enqueues after the first)
                                  ;; but if the symbol's in there, they could throw the channel into a hash or something
                                  ;; to respond to it later, so, you know. only do it automatically if no one else is handling it.
                                  ;; does weird things if you use the comment macro, somewhat predictably. 
                                  `(do ~@body)
                                  `(let [res# (do ~@body)]
                                     (enqueue ~'response-channel res#))))))]
     (swap! publius-functions conj [~(str publius-root name) new-middleware#])))

;;;;(publius-destination! :hello {:status 200 :body "Hello, world!" :headers {}})
(publius-destination! hello {:status 200 :body "Hello, world!" :headers {}})

(defn parse-request-args [req expected-args]
  (let [params (:params req)
        values (map params expected-args)
        results (vec (map json/parse-string values))]
    results))

(def publius-data-default-map (atom {}))

(defn publius-defaults [name]
  (@publius-data-default-map (str name)))

;; Why getters and setter as macros?
;; because there's basically no way to use these meaningfully
;; outside of macros and it makes the signatures cleaner
(defn get-data-field [identity field]
  (or (and identity
           (let [data-string (publius-grab *publius-db* identity field)]
             (and (not (nil? data-string))
                  (not (.isEmpty data-string))
                  (read-string data-string))))
         (publius-defaults field)))

(defn set-data-field [identity field value]
  (publius-put *publius-db* identity field (pr-str value)))

(def publius-data-fields (atom []))

;;; TODO auto get/set methods unless overridden
;;; TODO give data to publius as atoms with matches on them

(defmacro publius-data! [name default & functions]
  `(do (swap! publius-data-fields conj '~name)
       (swap! publius-data-default-map assoc ~(str name) ~default)
       ~@(map (fn [[function-name args & body]]
                `(publius-destination!
                  ~(str name "." function-name)
                  (let [identity# (:identity ~'request)
                        ~name (get-data-field identity# ~(str name))
                        ~args (parse-request-args ~'request ~(vec (map str args)))
                        result# (do ~@body)]
                    (set-data-field identity# ~(str name) result#) 
                    {:status 200
                     :headers {"Content-Type" "application/json"}
                     :body (json/generate-string result#)})))
              functions)))

; TODO request counting and shared atoms
(defn get-db-watched-atom [identity field]
  (let [a (atom (get-data-field identity field))]
    (add-watch a :publius
               (fn [key reference old-state new-state]
                 (set-data-field identity field new-state)))
    a))

(defmacro publius! [name args & body]
  `(publius-destination!
    ~name
    (let [~'identity (:identity ~'request) ; gensym here was causing me troubles, and it might as well be visible
          ~args (parse-request-args ~'request ~(vec (map str args)))
          ~(vec (deref publius-data-fields))
          (map (fn [field#]
                 (get-db-watched-atom ~'identity (str field#)))
               ~(vec (map str @publius-data-fields)))]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string (do ~@body))})))

;(publius-data! counter 0
;               (moveupone [] (inc counter))
;               (moveup [n] (+ counter n)))

;(publius! add [a b]
;          (swap! counter / 2)
;          (+ @counter a b))

(comment ;; this doesn't quite work yet, we get a function isn't associative error
  (publius-middleware!; reload
   (modifying-middleware (fn [req]
                           (let [source-dirs ["src"]
                                 modified-namespaces (ns-tracker source-dirs)]
                             (fn [request]
                               (doseq [ns-sym (modified-namespaces)]
                                 (require ns-sym :reload))
                               request)))
                         identity))
  )


;;;; TODO publius-data! (requires reordering some of these, as it should be <middleware combinators>, <visitor>, <data>, <publius!>
;;;; todo named middleware
;;;; AUTO ssl support with self-signed cert

(defn both-servers []
  (start-http-server @publius-app {:port 1337})
  (run-jetty @publius-app {:port 1336}))

(defn -main []
  (println "Starting servers...")
  (both-servers))