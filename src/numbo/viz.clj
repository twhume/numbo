(ns numbo.viz
	(:require [clojure.string :as str])
	(:require [clojure.tools.logging :as log])
	(:require [clojure.zip :as zip])
	(:require [numbo.coderack :as cr])
	(:require [numbo.cyto :as cy])
	(:require [numbo.history :as hist])
	(:require [numbo.misc :as misc])
	(:require [numbo.pnet :as pn])
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

(defn -add-blocks
 "Take a Rhizome graph g, and add new entries for the blocks in sequence-of-blocks s"
	[s]
	(do
		(log/debug "-add-blocks" s)
	(first (map-indexed #(let [n %1
																					b (:val %2)
																					prefix (str "blocks-" n)]
		(vector (vector (str prefix "-res") (vector (str prefix "-op")))
										(vector (str prefix "-op") (vector (str prefix "-p1") (str prefix "-p2")))
										(vector (str prefix "-p1") '[])
										(vector (str prefix "-p2") '[]))

		)
		s))
))

(defn -to-graph
 "Convert a cytoplasm into a graph for Rhizome"
 [c]
 (-> '{}
 	(into (map #(vector (str "bricks-" %1) '[]) (range 0 (count (:bricks c)))))
 	(into (map #(vector (str "targets-" %1) '[]) (range 0 (count (:targets c)))))
 	(into (-add-blocks (:blocks c)))
 ))

(defn -attr-to-color
	"Handles coloring nodes by attractiveness"
	[a]
	(let [r (int ( * 255 (- 1 a)))
							g (- 255 r)
							b (- 255 r)]
							(log/debug "-attr-to-color" a)
		(format "#FF%02X%02X" r r)))

(def -op-names '{ :times "X" :plus "+" :minus "-"})

(defn -name-val-from-label
 "Labels are of the form brick-1 - this returns each part"
 [s]
 (let [parts (str/split s #"-")]
 	(concat (vector (first parts) (Integer/parseInt (second parts))) (drop 2 parts))))

(def -op-lookups {+ "+" - "-" * "*"})

(defn -mk-label
 "Make a label for the cyto c node with type t, number n, optional part p"
 [c t n p]
 (do
	 (log/debug "-mk-label" c t n p)
	 (let [node (nth ((keyword t) c) n)]
		 (cond
		 	(or
		 		(= t "targets")
		 		(= t "bricks")) (:val node)
		 	(= t "blocks") (cond
		 																(= p "op") (get -op-lookups (first (:val node)))
		 																(= p "res") (eval (:val node))
		 																(= p "p1") (second (:val node))
		 																(= p "p2") (misc/third (:val node))))))
)

(defn plot-wm
 "Show the graph for the working memory target, bricks and blocks"
 ([c w h] (let [g (-to-graph c)]
	 (rh/graph->image (keys g) g
	 	:directed? false
 	 :options {:concentrate true :layout "neato" :clusterrank "local" :dpi 60}
 		:node->descriptor (fn [s]
 			(let [[t n p] (-name-val-from-label s)]
	 			{ :label (-mk-label c t n p)
	 				 :fontcolor "black"
	 				 :style (if (= t "targets") "bold" "solid")
	 			  :fillcolor (-attr-to-color (:attr (nth ((keyword t) c) n)))
	 			 }
	))))))
 
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
	 (reset! WM-IMAGE (plot-wm (:cyto (nth @hist/HISTORY @CURRENT)) (.getWidth c) (.getHeight c)))
 	(repaint! c)))

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
