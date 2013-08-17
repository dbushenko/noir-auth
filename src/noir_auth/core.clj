(ns noir-auth.core
  (:require [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.response :as resp]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utilities

(defn login-user! [user]
  (session/put! :simple-auth-user user)
  true)

(defn credentials-match? [user password users-seq]
  (some #(and (= user (:user %))
              (crypt/compare password (:password %)))
        users-seq))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Authentication

(defn authenticate [user password users-seq]
  "This is the function which tries to match user/password against
the specified list of users. If the matching user found, the function
saves it in the session returning true, otherwise -- returns nil.
Parameters:
  user -- (string) user name;
  password -- (string) password;
  users-seq -- (sequence of maps) sequence of users.
  user-seq item -- map of :user, :password, :roles.
Returns:
  true -- if authenticated
  nil -- otherwise."
  (if (credentials-match? user password users-seq)
              (login-user %)))

(defn current-user []
  "Returns authenticated user."
  (session/get :simple-auth-user))

(defn generate-password [password]
  "Generates password using BCrypt"
  (crypt/encrypt password))

(defn logout []
  (session/remove! :simple-auth-user))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Authorization

(defmacro authenticated [func & [redirect-uri]]
  "The macro should be used to wrap a function which should check
whether the user is authenticated.
Parameters:
  func -- the function call;
  redirect-uri -- (optional) the URI where the function will redirect
if the user is not authenticated."
  `(if (session/get :simple-auth-user)
     ~func
     (resp/redirect (or ~redirect-uri "/"))))

(defn authorized? [user-roles auth-roles]
  "This is a utility function used from the macros authorized-for.
It checks whether the user roles intersect with the requested roles."
  (some true?
        (for [auth-role (if (seq? auth-roles) auth-roles [auth-roles])
              user-role user-roles]
          (isa? user-role auth-role))))

(defmacro authorized-for [func roles & [redirect-uri]]
  "The macro should be used to wrap a function which should check
whether the user is authorized to call this functionality.
Parameters:
  func -- the function call;
  roles -- (keyword or sequence of keywords) user roles which are allowed
to access the wrapped functionality;
  redirect-uri -- (optional) the URI where the function will redirect
if the user is not authenticated."
  `(if (authorized? (:roles (current-user))
                    ~roles)
     ~func
     (resp/redirect (or ~redirect-uri "/"))))

