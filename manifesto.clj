;;; Good Evening, intrepid engineer and thinkatorian.
;;; Today I seek nothing short of transforming the way you view software.
;;; Let's begin, shall we?

;;; You'll notice that this file is commented as a Clojure file.
;;; That isn't ornamental.
;;; After hearing people saying tests are a good example of code, I wasn't convinced. I've worked in environments where there are no tests, where there are tons of tests, more tests than code by a factor that astounds the modern thinker. The one common thread is that they've been tremendously poor documentation, as they don't involve the top level. They don't tell you where to start. Sure, you can dive down, but I find that somewhat tedious.

;;; =========== Web 2.0 and Rails ====================================

;;; When I recently succumbed to the professional need to aquant myself with Rails (having done a smattering of Ruby already), I was struck by how poor many examples are. Every tutorial seemed to use their first two chapters to explain non-intersecting subsets of Rails, and given the hype, I just assumed rails itself was magical. A tool to directly express intention and build giant balls of speghetti code fast.

;;; I'm in it for the long haul, I thought. No thank you.

;;; But to be honest, that's not really true. I was pretty disappointed, actually. An interesting but seemingly nonsensical theology surrounding databases (because surely a philosophy would leave Ruby with a better library for tasks like, for instance, returning multiple result sets from a SQL engine). But that's about it. Nothing to see here. Another Model-2 system with Actions and Controllers and Views and oh my god what if I want to just make something.

;;; Perhaps that's where my confusion comes in. Rails doesn't eliminate the complexity of those taxonomies. It simply writes some of the code for you.

;;; To hear the hype surrounding metaprogramming in Ruby and the sheer volume of mechanical copy-paste involved with the `rails new` command, particularly as an ambassador from the Lisp culture.

;;; Well, to be honest, I was floored.

;;; =========== Moving Forward with Ideas ============================

;;; Web 2.0 is... about ten thousand tech generations old. And we've seen some new technologies. The callbacks of node.js, which was very exciting. The non-indented form of that same strategies with things like EventMachine (more specifically, with libraries like Goliath in Ruby, although plenty of EM-related libraries don't use callbacks). The exposure of the inherent complexity in things like Aleph. I've used them. They're fine, I guess. Lots of rigamarole.

;;; On the other end, the taxonomies have gotten larger and larger. Competent web applications use more and more JSON-returning requests to communicate logic. Despite HTML's status as structured data, it's lost in the war for communication to dynamic web pages. Partially because they are hardly that anymore. Websites aren't 'pages' or 'documents' anymore, at least, not in the general case. A fully functional version of tetris isn't a document. You can't capture it in that way.

;;; So what's the next paradigm? I have no interest in trying to promulgate any ideology. One of the reasons Lisps will always be powerful (although I believe interfaces and syntax will change tremendously in the next ten years) is that organizations of ideas are important. More than that, the number of paradigms isn't just expanding decade after decade. It's bounded only by our imagination, our intelligence and cultural familiarity.

;;; Clearly the smallest unit of a dynamic web page interaction coincides with these 'events' I keep hearing so much about. Cool. And with things like longpolling, we get all that elgance of legacy socket programming, without the conveinence of inter-request or timing logic.

;;; ...

;;; Yay. I guess.

;;; =========== We Should All Be Ashamed of Ourselves ================

;;; This is a nearly embarrassing state of engineering acumen on behalf of the people who've been reading about "10 ways to please your lady... customers" on Hacker News. Don't get me wrong, open source, sure. Oh yeah, fight the machine. As of the initial release, this is under a non-commercial license. But I'm 100% convinced that better toolsets exist within private companies. Or, more likely, with private contracters.

;;; We're tool builders. Come on, ya know? The tools for photoediting are real life generations ahead of tools for tool building itself.

;;; This is my contribution to that arms race.

;;; =========== A Note About Clojure =================================

;;; Clojure isn't the end of the discussion when it comes to code. But it's on the boundary of what can happen given modern paradigms. I don't mean boundary as in exploration. That's a fundamentally wrongheaded way of thinking about technology. It isn't about collection of languages. It's about toolsets. That's an entirely different discussion, though. Clojure is one of the choices, optimal (given assumptions such as code being stored in text), for some users. I'd say Haskell is another item on the boundary. Presumably it's optimal for some problems -- aside from things like Shuttle launch or taxes where errors are literally unacceptable at any point, I can't imagine what that would be. (As a side note, you'll note that those two examples are heavily numeric in nature. Is that what makes Haskell good, doing math well? Not really, in my estimation. But code that generates a human interface, or code that interfaces with a ephemeral application (AJAX/javascript) is a more fault-forgiving, even if not fault tolerant, environment.)

;;; If you want a better tool, make it.
