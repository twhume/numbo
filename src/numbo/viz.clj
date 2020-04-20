(ns numbo.viz
	(:require [numbo.pnet :as pn])
	(:require [numbo.working :as wm])
	(:require [numbo.coderack :as cr])
	(:require [rhizome.viz :as rh]))



; Helper method to visualize the pnet. Plot all links, label them, color by activation

(defn -pnet-node-to-rh
	"Take a single pnet node n from pnet p, output a vector of its destination links"
	[n]
	(vector (:name n) (into [] (map first (:links n)))))

(defn -activation-to-color
	"Handles coloring nodes by activation"
	[a]
	(cond
		(< a 1) "white"
		(< a 2) "bisque"
		(< a 4) "gold1"
		(< a 8) "darkorange"
		(> a 8) "firebrick1"
		)	
)

(def -link-style-map
	{:param "solid"
		:result "bold"
	 :similar "dotted"
	 :operator "dashed"}
)

(defn -pnet-into-rh
	"Convert a complete pnet into a rhizome representation, removing bidi links"
	[p]
	(into {} (map -pnet-node-to-rh (vals p)))
)

(defn plot-pnet
 "Convert a pnet to a graph structure suitable for rhizome"
 [p]
 (let [rh-graph (-pnet-into-rh p)]
 	(rh/view-graph (keys rh-graph) rh-graph
 	 :directed? false
 	 :options {:concentrate true :layout "dot" }
 		:node->descriptor (fn [n] {:label n :style "filled" :fillcolor (-activation-to-color (:activation (get p n)))})
 		:edge->descriptor (fn [n1 n2] {:style (get -link-style-map (pn/get-link-type p n1 n2))})
 	)))


