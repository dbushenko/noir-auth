# noir-auth

A simple authentication/authorization library heavily inspired by https://github.com/cemerick/friend.

The main feature of this library is simplicity. Unlike Chas Emerick's "Friend" which is rather general
library and could be used in wide variety of projects, noir-auth is small, simple and allows only
one type of authentication -- form authentication.

See the usage example here: https://github.com/dbushenko/noir-auth-example

## Usage

Add dependency

	[noir-auth "0.2.0-SNAPSHOT"]

You should be using lib-noir in your project since noir-auth relies on
the session management functionality provided by the lib-noir. That's why your project
should also have this dependency:

       [lib-noir "0.6.6"]

You need to switch on the session management using noir. Your Ring handler should look like this:


    (ns noir-auth-example.handler
      (:use compojure.core)
        (:require [compojure.handler :as handler]
	          [compojure.route :as route]
		  [noir.util.middleware :as noir]
		  [noir-auth.core :as auth]))
	. . .
	(def app
	  (->
	     [(handler/site app-routes)]
	     noir/app-handler))


Authentication should look like following:

  (POST "/login" [user password] (fn [a] (if (auth/authenticate user password
                                                                [{:user "user
                                                                  :password (auth/generate-password "password")
                                                                  :roles #{::user ::editor ::admin}}])
                                           (function-when-authenticated)
                                           (function-when-not-authenticated

You have to supply user, password and list of users to the function "authenticate". The list of users should contain
hash-map items with at least three fields:

* :user -- username
* :password -- encrypted password
* :roles -- sequence of roles of the user

You may create roles hierarchies deriving one role from another like this:

    	(derive ::root ::user)

Here the role ::root will be authorized for all routes which the role ::user is authorized.

You may authorize the route as following:

    (GET "/user" [] (auth/authorized-for (user-authorized-function)
                                         ::user))

    (GET "/root" [] (auth/authorized-for (root-authorized-function)
                                         [::root ::admin]))

Use "authorized-for" macro supplying it the function and the role or sequence of roles which are authorized
for this function.


There are two extra functions provided:

* current-user -- returns the current user item;
* logout -- logouts the user.

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
