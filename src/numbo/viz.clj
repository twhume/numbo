(ns numbo.viz
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
	(let [normalized-a (dec a)
							r (int ( * 255 (float (/ (- 3 normalized-a) 3))))
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
 (let [rh-graph (-pnet-into-rh p)]
 	(rh/graph->image (keys rh-graph) rh-graph
 	 :directed? false
 	 :options {:concentrate true :layout "dot" :dpi (int (/ (min w h) 11))}
 		:node->descriptor (fn [n] {:label n :style "filled" :fillcolor (-activation-to-color (:activation (get p n)))})
 		:edge->descriptor (fn [n1 n2] {:style (get -link-style-map (pn/get-link-type p n1 n2))})
 	)))

(defn -attractiveness-to-color
	"Handles coloring nodes by attractiveness"
	[a]
	(let [normalized-a a
							r (int ( * 255 (float (/ (- 3 normalized-a) 3))))
							g (- 255 r)
							b (- 255 r)
	]
	(format "#FF%02X%02X", r r )))

(defn -wm-into-rh
 "Convert a working memory into a rhizome representation"
 [w]
 (into '{} (map-indexed #(vector %1 (hash-map :type (:type %2) :value (:value %2) :attractiveness (:attractiveness %2))) w)))

(defn plot-wm
 "Convert a workimg memory to a graph structure suitable for rhizome"
 [p w h]
 (let [rh-graph (-wm-into-rh p)]
 	(rh/graph->image (keys rh-graph) rh-graph
 	 :directed? false
 	 :options {:concentrate true :layout "dot" :dpi (int (/ (min w h) 11))}
 		:node->descriptor (fn [n] {:label (:value (get rh-graph n)) :style "filled" :fillcolor (-attractiveness-to-color (:attractiveness (get rh-graph n)))})
 		)))

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
	 (reset! WM-IMAGE (plot-wm (:working (nth @hist/HISTORY @CURRENT)) (.getWidth c) (.getHeight c))))
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
 (table :id :coderack-table :model (-current-coderack)))

(defn make-frame []
  (frame
    :title "Numbojure Visualizer"
    :size [1024 :by 768]
    :id :main
    :on-close :exit
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
					    		(label :text "Temperature") (label :text "96%" :id :codelet) ""
			    			])
			    		:east (vertical-panel
					    	:items [
				    				(button :id :prev :text "Previous")
				    				(button :id :next :text "Next")
				    				(button :id :quit :text "Quit")
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
	)))

(defn back [f]
	(go-history f (dec @CURRENT)))

(defn forward [f]
	(go-history f (inc @CURRENT)))

(defn add-behaviors [f]
  (let [{:keys [quit prev next]} (group-by-id f)]

    (listen prev :action (fn [_] (back f)))
    (listen next :action (fn [_] (forward f)))
    (listen quit :action (fn [e] (System/exit 0) ))

    (listen f :component-resized (fn [_] (repaint-images f)))

    (map-key f "LEFT" (fn [_] (back f)) :scope :global)
    (map-key f "RIGHT" (fn [_] (forward f)) :scope :global)

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
