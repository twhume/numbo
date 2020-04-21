(ns numbo.viz
	(:require [numbo.coderack :as cr])
	(:require [numbo.history :as hist])
	(:require [numbo.pnet :as pn])
	(:require [numbo.working :as wm])
	(:require [rhizome.viz :as rh])
 (:use [seesaw.core]
 						[seesaw.graphics]
 						[seesaw.keymap]
 						[seesaw.keystroke]
        [seesaw.invoke :only [signaller]]
        [seesaw.options :only [apply-options]])
  (:require [seesaw.dev :as dev]))



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
	"Convert a complete pnet into a rhizome representation, removing bidi links"
	[p]
	(into {} (map -pnet-node-to-rh (vals p)))
)

(defn plot-pnet
 "Convert a pnet to a graph structure suitable for rhizome"
 [p]
 (let [rh-graph (-pnet-into-rh p)]
 	(rh/graph->image (keys rh-graph) rh-graph
 	 :directed? false
 	 :options {:concentrate true :layout "dot" }
 		:node->descriptor (fn [n] {:label n :style "filled" :fillcolor (-activation-to-color (:activation (get p n)))})
 		:edge->descriptor (fn [n1 n2] {:style (get -link-style-map (pn/get-link-type p n1 n2))})
 	)))

; ----- Seesaw GUI hereon -----

(def PNET-IMAGE (atom nil))
(def CURRENT (atom 0))

(defn re-render-pnet
 "Rerender the pnet buffer image"
 []
 (reset! PNET-IMAGE (plot-pnet (:pnet (nth @hist/HISTORY @CURRENT)))))

(defn render-pnet
 "Renders image for the Pnet"
 [c g]
 (do 
	 (if (nil? @PNET-IMAGE) (re-render-pnet))
	 (.drawImage g @PNET-IMAGE 0 0 nil)))

(defn pn-tab
 "Draws the PNet  tab"
 []
	(scrollable (canvas :id :pnet-canvas
		:paint render-pnet
		:preferred-size [1000 :by 1000]
		:background :white)))

(defn wm-tab
 "Draws the Working Memory tab"
 [] (pn-tab))

(defn cr-tab
 "Draws the Coderack tab"
 [] (pn-tab))


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

			    (horizontal-panel
			    	:items [

			    		(scrollable (text :id :codelet :multi-line? true :editable? false :focusable? false :wrap-lines? true))

			    		(vertical-panel
			    			:items [
			    				(button :id :prev :text "Previous")
			    				(button :id :next :text "Next")
			    				(button :id :quit :text "Quit")

			    			])
			    	])])))

(defn go-history
	"Go to item i in history, and repaint using root r"
	[i r]
		(if
			(and (>= i 0) (< i (count @hist/HISTORY)))
				(do
					(reset! CURRENT i)
					(println "painting " (:iteration (nth @hist/HISTORY @CURRENT)) "out of" (count @hist/HISTORY))
					(re-render-pnet)
					(repaint! (select r [:#pnet-canvas]))
					(println (:desc (:codelet (nth @hist/HISTORY @CURRENT))))
					(text! (select r [:#codelet]) (:desc (:codelet (nth @hist/HISTORY @CURRENT))))
	)))

(defn back [f]
	(go-history (dec @CURRENT) f))

(defn forward [f]
	(go-history (inc @CURRENT) f))


(defn add-behaviors [f]
  (let [{:keys [quit prev next]} (group-by-id f)]

    (listen prev :action (fn [_] (back f)))
    (listen next :action (fn [_] (forward f)))
    (listen quit :action (fn [e] (System/exit 0) ))

    (map-key f "LEFT" (fn [_] (back f)) :scope :global)
    (map-key f "RIGHT" (fn [_] (forward f)) :scope :global)

;    (request-focus! f)
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
         (show! f#)))

     (defn ~'-main [& args#]
       (apply ~'run :exit args#))))

(defexample show-numbo
 []
  (-> (make-frame)
    add-behaviors))
;(run :dispose)
