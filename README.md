# publius

## Usage

Prototypes don't need taxonomy. You buy a lot when you have less code, and if it's tightly organized, you buy even more.

You got some data?

Define the data with an initial value and some methods you can call on it.

```clojure
(use 'publius.core)

(publius-data! counter 0
    (add-one [] (inc counter))
    (add [n] (+ counter n)))

(publius-data! name "World"
    (excited [val] (str val "!"))
    (normal [val] val))
```

Maybe you want to do something non-CRUD-y

```clojure
(publius! get-lucky [nonluck-factor]
    (swap! counter / nonluck-factor) ; these changes go to the database
    (str "The lucky number is..." @counter ", " @name))
```

Then you start the server.

```clojure
(start-http-server @publius-app {:port 1337})
```

That's all the code you need to write. 

There's also a middleware option so you can integrate it into your code easier, if you'd like.

```clojure
(def handler (-> my-app
       	         less-cool-middleware
              	 (@publius-app-middleware)))
```

Then you do the web-magics. 

First, start mongodb (the default persistence engine).

	mongod

Note, you're doing these in a browser -- publius keeps track of identity between requests for you.

These commands also work over with POSTs and form params.

	GET http://localhost:1337/publius/counter.add?n=5
	=> 5

	GET http://localhost:1337/publius/counter.add-one
	=> 6

	GET http://localhost:1337/publius/name.excited?val=Lotus

	GET http://localhost:1337/publius/get-lucky?nonluck-factor=3
	=> "The lucky number is... 2, Lotus!"

Tada! There's your api. No more worries. 

Better examples on their way (clearly lists and deeply nested objects are just as functional). 

## ToDos

Plenty, plenty, see the code for notes. But this is all you need to get prototypes working, really.

Higher level ideas include (in order of feature level priority -- all non-Readme docs will be in the form of complete projects, which I'll do as I get farther/run out of interesting problems to solve):

1. Shared Data
2. Push Notifications
3. Authentication
4. SSL support by default
5. Dynamic/Static content (JS and HTML in particular) Handling
6. AB Testing
7. Riak connector (more knobs than mongo, not the tremendous pain of literally every db that isn't one of them)
8. Auto-reload, stacktrace, connection closing on error, etc. support built-in
9. Symmetric JS client library
10. Optimization (shouldn't warn for reflection, etc.) (NOTE: right now this can handle ~350-400 serial requests on my modest laptop, and since it scales out horizontally perfectly fine, I'm not sweating this, likely ever -- push notifications might require a little jiggering to scale linearly, but rest assured, I can do the jiggering for you). 

## Technical Geekery

Yes, I am aware (use 'publius.core) takes like ten seconds to finish. As my friend Chris accurately guessed, this is "an explosion of macros". It also has more than a few dependencies, which all need to be loaded in to the JVM. 

On an more technical note, I'm aware this breaks some "style guidelines". Variable capture is used incessantly. I think it makes the interface cleaner, and I prefer a clean interface to the highly local complexity of the macros. 

Feedback is much appreciated. I consider this one of the more advanced uses of macros (although not as crazy as core.match), and I hope it's one of the more useful abstractions I could provide.

## License

Copyright (C) 2012 Joshua Orion Skylar Hickman

Distributed under the Eclipse Public License, the same as Clojure.
