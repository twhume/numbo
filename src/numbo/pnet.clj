(ns numbo.pnet)

; Pnet is a map of name -> node, where name is a keyword name of the node, and node is a map:
; :activation - default 0
; :weight - default 0
; :links - each a tuple of destination (keyword name of dest note) and type (keyword from link-types)


(def link-types '(:operand :result :similar))

; Initial values for the Pnet - others (e.g. activation) can be added programmatically
(def init-pnet '{

	:1 {
		:links (
		 (:plus-1-1 :result),
			(:plus-1-2 :result),
			(:plus-1-3 :result))
	}

	:2 {
		:links (
		 (:plus-1-2 :result),
			(:plus-2-2 :result),
			(:plus-2-3 :result),
			(:times-2-2 :operand),
			(:times-2-3 :operand),
		 (:plus-1-1 :result)
			)
	}

	:3 {
		:links (
		 (:plus-1-3 :result),
			(:plus-2-3 :result),
			(:plus-3-3 :result),
			(:times-2-3 :operand),
			(:times-3-3 :operand),
		 (:plus-1-2 :result),
		 (:4 :similar)
			)
	}

	:plus-1-1 {
			:links ()
	}

	:plus-1-2 {
			:links ()
	}

	:plus-1-3 {
			:links ()
	}

	:plus-2-2 {
			:links ()
	}

	:plus-2-3 {
			:links ()
	}

	:plus-3-3 {
			:links ()
	}

	:times-2-2 {
			:links ()
	}

	:times-2-3 {
			:links ()
	}

	:times-3-3 {
			:links ()
	}

	:4 {
			:links ()
	}

	})

; Invalid if:
; - a link type is specified which isn't in all-types
; - nodes are referenced in links but are never defined
; (not yet)- nodes with no connection mentioned (in map or :links)

(defn validate-pnet
 "true if this is a valid pnet, false if not"
[p]
	(let [all-nodes (keys p)
							all-links (apply concat (map #(:links (second %)) p))
							used-types (distinct (map second all-links))
							used-links (distinct (map first all-links))]

							(cond
								(not-every? (set link-types) used-types) (do (println "Bad link types" (remove (set link-types) used-types)) false)
								(not-every? (set all-nodes) used-links) (do (println "Bad nodes in links" (remove (set all-nodes) used-links)) false)
								:else true								
							)))

; TODO make functions to act on a pnet - e.g. to activate a node and have its activation spread
; TODO write unit test for validate-pnet
