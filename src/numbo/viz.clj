(ns numbo.viz
	(:require [clojure.tools.logging :as log])
	(:require [clojure.zip :as zip])
	(:require [numbo.coderack :as cr])
	(:require [numbo.history :as hist])
	(:require [numbo.pnet :as pn])
	(:require [numbo.working :as wm])
	(:require [rhizome.viz :as rh])
 (:use [seesaw.border]
       [seesaw.core]
 						[seesaw.graphics]
 						[seesaw.keymap]
 						[seesaw.keystroke]))

; Helper method to visualize the pnet. Plot all links, label them, color by activation

(defn -pnet-node-to-rh
	"Take a single pnet node n from pnet p, output a vector of its destination links"
	[n]
	(vector (:name n) (into [] (map first (:links n)))))

(defn -activation-to-color
	"Handles coloring nodes by activation"
	[a]
	(let [r (int ( * 255 (- 1 a)))
							g (- 255 r)
							b (- 255 r)
	]
	(format "#FF%02X%02X", r r )))

(def -link-style-map
	{:param "solid"
		:result "bold"
	 :similar "dotted"
	 :operator "dashed"}
)

(defn -pnet-into-rh
	"Convert a complete pnet into a rhizome representation"
	[p]
	(into {} (map -pnet-node-to-rh (vals p)))
)

(defn plot-pnet
 "Convert a pnet to a graph structure suitable for rhizome, then to an image"
 [p w h]
 (let [just-activated (filter #(> (:activation (second %1)) 0) p)
 						rh-graph (-pnet-into-rh just-activated)]
; (let [rh-graph (-pnet-into-rh p)]
 	(rh/graph->image (keys rh-graph) rh-graph
 	 :directed? false
 	 :options {:concentrate true :layout "fdp" :dpi 50 }
 	 :node->cluster (fn [n] (:type (get p n)))
 		:node->descriptor (fn [n] {:label n :style "filled" :fillcolor (-activation-to-color (:activation (get p n)))})
 ;		:edge->descriptor (fn [n1 n2] {:style (get -link-style-map (pn/get-link-type p n1 n2))})
  		:edge->descriptor (fn [n1 n2] {:style :invis})
 	)))

; ----- Functions to plot a working memory -----

(defn -is-virt-uuid?
 "Is the supplied string a virtual UUID?"
	[s]
	(cond
		(.endsWith s "_param0") true
		(.endsWith s "_param1") true
		(.endsWith s "_op") true
		:else false
))

(defn -get-virt-uuid
 "Returns the UUID part of a virtual node UUID"
 [u]
 (clojure.string/replace u #"_.*$" ""))

(defn -get-virt-param
 "Returns the param part of a virtual node UUID"
 [u]
 (clojure.string/replace u #"^.*_" ""))

(defn -node-to-graph
	"Converts the node at a zipper loc to its Rhizome representation"
	[loc]
	(let [node (zip/node loc)]
		(list (:uuid node) (vector (str (:uuid node) "_op") (:params node))))
)

; Either the UUIDs of sub-blocks or "virtual UUIDs" referring to params

(defn -block-children
	"Returns vector of UUID representations of node n"
	[n]
	(let [children (:params n)
							uuid (:uuid n)]
		(vec (map-indexed #(if (int? %2) (str uuid "_param" %1) (:uuid %2)) children))))

(defn -blocktree-to-graph
	"Convert a single blocktree into a Rhizome-format graph"
	[bt]
	(let [zipper (wm/-make-blocktree-zipper bt)]
		(loop [cur zipper out '{}]
		 (if (zip/end? cur) out
		 	(let [node (zip/node cur)
		 							node-uuid (:uuid node)
		 							node-op-uuid (str node-uuid "_op")
		 							node-children (-block-children node)
		 							]
		 							(log/debug "-blocktree-to-graph out=" out "node=" node "node-uuid=" node-uuid "node-op-uuid=" node-op-uuid "node-children=" node-children)
			  	(recur
			  		(zip/next cur)
			  		(do
			  			(cond
			  				(int? node) out
			  				(or (nil? node-children) (empty? node-children)) (assoc out node-uuid (vector node-op-uuid))
					  		:else (assoc out
													  		node-uuid (vector node-op-uuid)
													  		node-op-uuid node-children
													  		(first node-children) '[]
													  		(second node-children) '[]
			  		)))))))))

(defn -brick-to-graph
 "Convert a single brick (or target) to a Rhizome graph entry"
	[br]
	(hash-map (:uuid br) '[]))

(defn -to-graph
 "Convert a target, bricks and blocks into a graph for Rhizome"
 [ta br bl]
 (let [bt-graphs (apply merge (map -blocktree-to-graph bl))
 						ta-graph (if (nil? ta) '{} (-brick-to-graph ta))
 						br-graphs (apply merge (map -brick-to-graph br))]
 						  (log/debug "-to-graph bt-graphs=" bt-graphs)
 						  (log/debug "-to-graph ta-graph=" ta-graph)
 						  (log/debug "-to-graph br-graphs=" br-graphs)

 	(apply merge bt-graphs ta-graph br-graphs)))

; add root nodes for all the MAGIC child IDs

(defn -attr-to-color
	"Handles coloring nodes by attractiveness"
	[a]
	(let [r (int ( * 255 (- 1 a)))
							g (- 255 r)
							b (- 255 r)]
		(format "#FF%02X%02X" r r)))

(defn -get-wm-style
 "Works out appropriate style for a type t"
 [t]
 (case t
 	:target "bold"
 	:secondary "bold"
 	:brick "solid"
 	:block "dashed"
 ))

(def -op-names '{ :times "X" :plus "+" :minus "-"})

(defn -get-node-label
 "Given the UUID u of a block in the list of blocks bl, return a tuple of its [label,type,attr]"
	[ta br bl u]
	(let [node-uuid (if (-is-virt-uuid? u) (-get-virt-uuid u) u)
							[entry src] (wm/find-anywhere ta br bl node-uuid)]
							(if (-is-virt-uuid? u)
								(condp = (-get-virt-param u)
									"op" [((:op entry) -op-names) :op (:attr entry)]
									"param0" [(first (:params entry)) :param (:attr entry)]
									"param1" [(second (:params entry)) :param (:attr entry)]
									"ERROR")
							[(:value entry) src (:attr entry)])))

(defn plot-wm
 "Show the graph for the working memory target, bricks and blocks"
 ([ta br bl w h]
 (let [g (-to-graph ta br bl)]
	 (rh/graph->image (keys g) g
	 	:directed? false
 	 :options {:concentrate true :layout "neato" :clusterrank "local" :dpi 60}
 		:node->descriptor (fn [u]
 		  (let [[label type attr] (-get-node-label ta br bl u)]
 		  		(condp = type
 		  		 :op (hash-map :label label :style "filled" :fontcolor "white"  :color "black" :fixedsize true :width 0.4 :height 0.4 )
 		  		 :param (hash-map :label label :style "filled" :fontcolor "black" :color "black" :fillcolor (-attr-to-color attr))
 		  		 :blocks (hash-map :label label :style "filled" :color "black" :penwidth 1 :fontcolor "black" :fillcolor (-attr-to-color attr))
 		  		 :bricks (hash-map :label label :style "filled" :color "black" :penwidth 2 :fontcolor "black" :fillcolor (-attr-to-color attr) )
 		  		 :target (hash-map :label label :style "solid,filled" :color "red" :penwidth 2 :fontcolor "black" :fillcolor (-attr-to-color attr) )
 		  		 :else (log/warn "WEIRD TYPE" type)))))))
 ([] (plot-wm @wm/TARGET @wm/BRICKS @wm/BLOCKS)))
 
; ----- Seesaw GUI hereon -----

(def PNET-IMAGE (atom nil))
(def WM-IMAGE (atom nil))
(def CURRENT (atom 0))

(defn re-render-pnet
 "Rerender the pnet buffer image to canvas c"
 [c]
 (do
	 (reset! PNET-IMAGE (plot-pnet (:pnet (nth @hist/HISTORY @CURRENT)) (.getWidth c) (.getHeight c)))
		(repaint! c)))

(defn render-pnet
 "Renders image for the Pnet"
 [c g]
	 (if (nil? @PNET-IMAGE) (re-render-pnet c)
	 (.drawImage g @PNET-IMAGE 0 0 nil)))

(defn pn-tab
 "Draws the PNet tab"
 []
	(scrollable (canvas :id :pnet-canvas
		:paint render-pnet
		:background :white)))

(defn re-render-wm
 "Rerender the working memory buffer image"
 [c]
 (do
	 (reset! WM-IMAGE (plot-wm
	 	(:target (nth @hist/HISTORY @CURRENT))
	 	(:bricks (nth @hist/HISTORY @CURRENT))
	 	(:blocks (nth @hist/HISTORY @CURRENT))
	 	 (.getWidth c) (.getHeight c))))
 	(repaint! c))

(defn render-wm
 "Renders image for the Working memory"
 [c g]
 (do 
	 (if (nil? @WM-IMAGE) (re-render-wm c))
	 (.drawImage g @WM-IMAGE 0 0 nil)))

(defn wm-tab
 "Draws the working memory tab"
 []
	(scrollable (canvas :id :wm-canvas
		:paint render-wm
		:background :white)))

(defn -current-coderack
 []
 (vector
 	:columns [ { :key :iteration :text "Born"} { :key :urgency :text "Urgency"}  	{ :key :desc :text "Description"} ]
 	:rows (vec (into '[["Born" "Urgency" "Codelet"]] (:coderack (nth @hist/HISTORY @CURRENT))))))

(defn cr-tab
 "Draws the Coderack tab"
 [] 
 (scrollable (table :id :coderack-table
 							:model (-current-coderack))))

(defn make-frame []
  (frame
    :title "Numbojure Visualizer"
    :size [1024 :by 768]
    :id :main
    :on-close :hide
    :content
    	(vertical-panel
    		:items [

			    (tabbed-panel 
				    :tabs [{:title "PNet" :content (pn-tab)}
				           {:title "Working Memory" :content (wm-tab)}
				           {:title "Coderack"   :content (cr-tab)}])

			    (border-panel
			    		:border [10 "" (empty-border :thickness 15)]
			    		:maximum-size [Integer/MAX_VALUE :by 200]
			    		:west (grid-panel
			    			:columns 3
			    			:maximum-size [300 :by Integer/MAX_VALUE ]
			    			:items [
					    		(label :text "Iteration") (label :text "0 / 100" :id :iteration) ""
					    		(label :text "Codelet")  (label :text "fred" :id :codelet) ""
					    		(label :text "Temperature") (label :text "96%" :id :temperature) ""
			    			])
			    		:east (vertical-panel
					    	:items [
				    				(button :id :btn-last :text "Last")
				    				(button :id :btn-next :text "Next")
				    				(button :id :btn-prev :text "Previous")
				    				(button :id :btn-first :text "First")
				    				(button :id :btn-quit :text "Quit")
			    			]))])))

(defn repaint-images
	"Redraw all our images"
 [f]
	(re-render-pnet (select f [:#pnet-canvas]))
	(re-render-wm (select f [:#wm-canvas])))

(defn go-history
	"Go to item i in history, and repaint using root r"
	[r i]
		(if
			(and (>= i 0) (< i (count @hist/HISTORY)))
				(do
					(reset! CURRENT i)
					(repaint-images r)
					(config! (select r [:#coderack-table]) :model (-current-coderack))
					(config! (select r [:#iteration]) :text (str (inc @CURRENT) "/" (count @hist/HISTORY)))
					(text! (select r [:#codelet]) (:desc (:codelet (nth @hist/HISTORY @CURRENT))))
					(text! (select r [:#temperature]) (format "%3.0f%%" (* 100 (:temperature (nth @hist/HISTORY @CURRENT)))))
	)))

(defn go-back [f]
	(go-history f (dec @CURRENT)))

(defn go-forward [f]
	(go-history f (inc @CURRENT)))

(defn go-first [f]
	(go-history f 0))

(defn go-last [f]
	(go-history f (dec (count @hist/HISTORY))))


(defn add-behaviors [f]
  (let [{:keys [btn-quit btn-prev btn-next btn-first btn-last]} (group-by-id f)]

    (listen btn-prev :action (fn [_] (go-back f)))
    (listen btn-next :action (fn [_] (go-forward f)))
    (listen btn-first :action (fn [_] (go-first f)))
    (listen btn-last :action (fn [_] (go-last f)))
    (listen btn-quit :action (fn [e] (System/exit 0) ))

    (listen f :component-resized (fn [_] (repaint-images f)))

    (map-key f "LEFT" (fn [_] (go-back f)) :scope :global)
    (map-key f "RIGHT" (fn [_] (go-forward f)) :scope :global)

  f))

; Takes a run history and visualizes it

(defmacro defexample
  "Does the boilerplate for an example.
  arg-vec is a binding vector of arguments for the example, usually command-line
  args. body is code which must return an instance of javax.swing.JFrame. If the
  frame's size has not been set at all, pack! is called. Then show! is called.
  Defines two functions:
    run   : takes an on-close keyword and trailing args and runs
            the example.
    -main : calls (run :exit & args). i.e. runs the example and exits when
            closed
  See the plethora of examples in this directory for usage examples.
  "
  [arg-vec & body]
  `(do
     (defn ~'run [on-close# & args#]
       (let [~arg-vec args#
             f# (invoke-now ~@body)]
         (config! f# :on-close on-close#)
         (when (= (java.awt.Dimension.) (.getSize f#))
           (pack! f#))
         (show! f#)
         (go-history f# 0)))

     (defn ~'-main [& args#]
       (apply ~'run :exit args#))))

(defexample show-numbo
 []
	  (-> (make-frame)
	    add-behaviors))
;(run :dispose)
