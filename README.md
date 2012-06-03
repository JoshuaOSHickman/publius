# publius



## Usage

Prototypes, these days, are done often in Rails. I've been asked if clojure has something like rails. The answer is clearly no. And THANK GOD. Rails is terrible. It's funny to hear rails people talk about ritual programming and then go insane if you don't split out one web request across three files so you can call it "well abstracted". 

Prototypes don't need taxonomy. You buy a lot when you have less code, and if it's tightly organized, you buy even more.

You got some data?

; Define the data with an initial value and some methods you can call on it.

	(publius-data counter 0
	    (add-one [] (inc counter))
	    (add [n] (+ counter n)))

; Maybe you want to do something non-CRUD-y

	(publius! report-winnings [name]
	    (str "Hello to you, " name ", good sir, and your $" counter " winnings!"))

	(start-http-server publius-app {:port 1337})

; That's all the code you need to write. 

; There's also a middleware option so you can integrate it into your code easier, if you'd like.

	(def handler (-> my-app
        	         less-cool-middleware
                	 publius-app-middleware))

Then you do the web-magics. 

; First, start mongodb (the default persistence engine).

mongod

; Note, you're doing these in a browser -- publius keeps track of identity between requests for you.

; These commands also work over with POSTs and form params.

	GET http://localhost:1337/publius/counter.add?n=5

	=> 5

	GET http://localhost:1337/publius/counter.add-one

	=> 6

	GET http://localhost:1337/publius/report-winnings?name=Bob%20Marley

	=> "Hello to you, Bob Marley, good sir, and your $6 winnings!"

Tada! There's your api. No more worries. 

Better examples on their way (clearly lists and deeply nested objects are just as usable). 

## ToDos

Plenty, plenty, see the code for notes. But this is all you need to get prototypes working, really.

Higher level ideas include:
* Async behavior control commands
* JS client library
* a dynamic/static content handling abstraction
* optimization (shouldn't warn for reflection)

## License

Copyright (C) 2012 Joshua Orion Skylar Hickman

Distributed under the Eclipse Public License, the same as Clojure.
