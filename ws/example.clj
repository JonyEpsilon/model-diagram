;; gorilla-repl.fileformat = 1

;; **
;;; # Model shape diagram
;;; 
;;; Here I want to sketch out code to take a specification of a "shape" of a model - that is metadata about the model's inputs and outputs - and generate a diagram from it.
;;; 
;;; I'll use thi.ng to generate the SVG, although I'm not sure what its advantage is over just generating hiccup data - something to discuss. I'll use Hiccup to render to SVG, with the hope that this will be easy to move over to hiccups/Sablono for client-side rendering.
;; **

;; @@
(ns model-shape
  (:require [gorilla-repl.html :as html]
            [thi.ng.geom.svg.core :as svg]
            [hiccup.core :as hiccup]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; We need a specification of the shape of the model. This is the minimal information needed to replicate the wireframe mockup.
;; **

;; @@
(def example-model
  {:n-inputs 2
   :n-outputs [3 2]})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;model-shape/example-model</span>","value":"#'model-shape/example-model"}
;; <=

;; **
;;; Some parameters to control the layout of the diagram.
;; **

;; @@
(def render-options
  {:box-height 30
   :box-h-spacing 20
   :box-width 125
   :box-w-spacing 75
   :group-box-padding 6
   :canvas-width 600
   :padding 10})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;model-shape/render-options</span>","value":"#'model-shape/render-options"}
;; <=

;; **
;;; Functions to draw the primitive elements.
;; **

;; @@
(defn stacked-box
  [render-options x-offset y-offset cl i]
  (let [{:keys [box-height box-width box-h-spacing]} render-options]
    (svg/rect [x-offset (+ (* (+ box-height box-h-spacing) i) y-offset)]
              box-width
              box-height
              {:class cl})))

(defn stack-of-boxes
  [render-options x-offset max-n n-boxes cl]
  (let [{:keys [box-height box-h-spacing]} render-options]
    (mapv (partial stacked-box
                   render-options
                   x-offset
                   (* (/ (+ box-height box-h-spacing) 2)
                      (- max-n n-boxes))
                   cl)
          (range n-boxes))))

(defn stacked-line
  [render-options x-start x-end y-offset i]
  (let [{:keys [box-height box-width box-h-spacing]} render-options
        line-y (+ (* (+ box-height box-h-spacing) i) (+ (/ box-height 2) y-offset))]
    (svg/line [x-start line-y] [x-end line-y] {:class "line"})))

(defn stack-of-lines
  [render-options x-start x-end max-n n-lines]
  (let [{:keys [box-height box-h-spacing]} render-options]
    (mapv (partial stacked-line
                   render-options
                   x-start
                   x-end
                   (* (/ (+ box-height box-h-spacing) 2)
                      (- max-n n-lines)))
          (range n-lines))))

(defn group-box
  [render-options x-offset y-offset i-start i-end]
  (let [{:keys [box-height box-width box-h-spacing group-box-padding]} render-options]
    (svg/rect [(- x-offset group-box-padding)
               (- (+ (* (+ box-height box-h-spacing) i-start) y-offset) group-box-padding)]
              (+ box-width (* 2 group-box-padding))
              (+ (* (+ box-height box-h-spacing) (- i-end i-start))
                 (* -1 box-h-spacing)
                 (* 2 group-box-padding))
              {:class "group"})))

(defn group-ranges
  [n-outputs]
  (let [c (reductions + 0 n-outputs)]
    (apply map list [(drop-last c) (rest c)])))

(defn group-boxes
  [render-options x-offset max-n group-sizes]
  (let [{:keys [box-height box-h-spacing]} render-options
        ranges (group-ranges group-sizes)
        n-boxes (apply + group-sizes)]
    (mapv #(group-box
              render-options
              x-offset
              (* (/ (+ box-height box-h-spacing) 2) (- max-n n-boxes))
              (first %)
              (second %))
          ranges)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;model-shape/group-boxes</span>","value":"#'model-shape/group-boxes"}
;; <=

;; **
;;; And a function to put it all together.
;; **

;; @@
(defn render-model
  [render-options model]
  (let [{:keys [canvas-width box-width box-w-spacing box-height box-h-spacing padding]} render-options
        {:keys [n-inputs n-outputs]} model
        total-outputs (apply + n-outputs)
        max-n (max n-inputs total-outputs)]
    (hiccup/html (svg/svg
                   {:width (+ canvas-width (* 2 padding))
                    :height (+ (* max-n (+ box-height box-h-spacing)) (* 2 padding))}
                   (svg/group
                     {:transform (str "translate(" padding ", " padding ")")}
                     (concat
                       (stack-of-boxes render-options 0 max-n n-inputs "input")
                       (stack-of-lines render-options
                                       box-width
                                       (+ box-width box-w-spacing)
                                       max-n
                                       n-inputs)
                       [(svg/rect [(+ box-width box-w-spacing) 0]
                                  box-width
                                  (- (* max-n (+ box-height box-h-spacing)) box-h-spacing)
                                  {:class "model"})]
                       (group-boxes
                         render-options
                         (* 2 (+ box-width box-w-spacing))
                         max-n
                         n-outputs)
                       (stack-of-boxes render-options
                                       (* 2 (+ box-width box-w-spacing))
                                       max-n
                                       total-outputs
                                       "output")
                       (stack-of-lines render-options
                                       (+ (* 2 box-width) box-w-spacing)
                                       (* 2 (+ box-width box-w-spacing))
                                       max-n
                                       total-outputs)))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;model-shape/render-model</span>","value":"#'model-shape/render-model"}
;; <=

;; @@
(def h (render-model render-options example-model))
(println h)
(html/html-view h)
;; @@
;; ->
;;; &lt;svg height=&quot;270&quot; version=&quot;1.1&quot; width=&quot;620&quot; xmlns:xlink=&quot;http://www.w3.org/1999/xlink&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;&gt;&lt;g transform=&quot;translate(10, 10)&quot;&gt;&lt;rect class=&quot;input&quot; height=&quot;30&quot; width=&quot;125&quot; x=&quot;0.00&quot; y=&quot;75.00&quot;&gt;&lt;/rect&gt;&lt;rect class=&quot;input&quot; height=&quot;30&quot; width=&quot;125&quot; x=&quot;0.00&quot; y=&quot;125.00&quot;&gt;&lt;/rect&gt;&lt;line class=&quot;line&quot; x1=&quot;125.00&quot; x2=&quot;200.00&quot; y1=&quot;90.00&quot; y2=&quot;90.00&quot;&gt;&lt;/line&gt;&lt;line class=&quot;line&quot; x1=&quot;125.00&quot; x2=&quot;200.00&quot; y1=&quot;140.00&quot; y2=&quot;140.00&quot;&gt;&lt;/line&gt;&lt;rect class=&quot;model&quot; height=&quot;230&quot; width=&quot;125&quot; x=&quot;200.00&quot; y=&quot;0.00&quot;&gt;&lt;/rect&gt;&lt;rect class=&quot;group&quot; height=&quot;142&quot; width=&quot;137&quot; x=&quot;394.00&quot; y=&quot;-6.00&quot;&gt;&lt;/rect&gt;&lt;rect class=&quot;group&quot; height=&quot;92&quot; width=&quot;137&quot; x=&quot;394.00&quot; y=&quot;144.00&quot;&gt;&lt;/rect&gt;&lt;rect class=&quot;output&quot; height=&quot;30&quot; width=&quot;125&quot; x=&quot;400.00&quot; y=&quot;0.00&quot;&gt;&lt;/rect&gt;&lt;rect class=&quot;output&quot; height=&quot;30&quot; width=&quot;125&quot; x=&quot;400.00&quot; y=&quot;50.00&quot;&gt;&lt;/rect&gt;&lt;rect class=&quot;output&quot; height=&quot;30&quot; width=&quot;125&quot; x=&quot;400.00&quot; y=&quot;100.00&quot;&gt;&lt;/rect&gt;&lt;rect class=&quot;output&quot; height=&quot;30&quot; width=&quot;125&quot; x=&quot;400.00&quot; y=&quot;150.00&quot;&gt;&lt;/rect&gt;&lt;rect class=&quot;output&quot; height=&quot;30&quot; width=&quot;125&quot; x=&quot;400.00&quot; y=&quot;200.00&quot;&gt;&lt;/rect&gt;&lt;line class=&quot;line&quot; x1=&quot;325.00&quot; x2=&quot;400.00&quot; y1=&quot;15.00&quot; y2=&quot;15.00&quot;&gt;&lt;/line&gt;&lt;line class=&quot;line&quot; x1=&quot;325.00&quot; x2=&quot;400.00&quot; y1=&quot;65.00&quot; y2=&quot;65.00&quot;&gt;&lt;/line&gt;&lt;line class=&quot;line&quot; x1=&quot;325.00&quot; x2=&quot;400.00&quot; y1=&quot;115.00&quot; y2=&quot;115.00&quot;&gt;&lt;/line&gt;&lt;line class=&quot;line&quot; x1=&quot;325.00&quot; x2=&quot;400.00&quot; y1=&quot;165.00&quot; y2=&quot;165.00&quot;&gt;&lt;/line&gt;&lt;line class=&quot;line&quot; x1=&quot;325.00&quot; x2=&quot;400.00&quot; y1=&quot;215.00&quot; y2=&quot;215.00&quot;&gt;&lt;/line&gt;&lt;/g&gt;&lt;/svg&gt;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<svg height=\"270\" version=\"1.1\" width=\"620\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\"><g transform=\"translate(10, 10)\"><rect class=\"input\" height=\"30\" width=\"125\" x=\"0.00\" y=\"75.00\"></rect><rect class=\"input\" height=\"30\" width=\"125\" x=\"0.00\" y=\"125.00\"></rect><line class=\"line\" x1=\"125.00\" x2=\"200.00\" y1=\"90.00\" y2=\"90.00\"></line><line class=\"line\" x1=\"125.00\" x2=\"200.00\" y1=\"140.00\" y2=\"140.00\"></line><rect class=\"model\" height=\"230\" width=\"125\" x=\"200.00\" y=\"0.00\"></rect><rect class=\"group\" height=\"142\" width=\"137\" x=\"394.00\" y=\"-6.00\"></rect><rect class=\"group\" height=\"92\" width=\"137\" x=\"394.00\" y=\"144.00\"></rect><rect class=\"output\" height=\"30\" width=\"125\" x=\"400.00\" y=\"0.00\"></rect><rect class=\"output\" height=\"30\" width=\"125\" x=\"400.00\" y=\"50.00\"></rect><rect class=\"output\" height=\"30\" width=\"125\" x=\"400.00\" y=\"100.00\"></rect><rect class=\"output\" height=\"30\" width=\"125\" x=\"400.00\" y=\"150.00\"></rect><rect class=\"output\" height=\"30\" width=\"125\" x=\"400.00\" y=\"200.00\"></rect><line class=\"line\" x1=\"325.00\" x2=\"400.00\" y1=\"15.00\" y2=\"15.00\"></line><line class=\"line\" x1=\"325.00\" x2=\"400.00\" y1=\"65.00\" y2=\"65.00\"></line><line class=\"line\" x1=\"325.00\" x2=\"400.00\" y1=\"115.00\" y2=\"115.00\"></line><line class=\"line\" x1=\"325.00\" x2=\"400.00\" y1=\"165.00\" y2=\"165.00\"></line><line class=\"line\" x1=\"325.00\" x2=\"400.00\" y1=\"215.00\" y2=\"215.00\"></line></g></svg>","value":"#gorilla_repl.html.HtmlView{:content \"<svg height=\\\"270\\\" version=\\\"1.1\\\" width=\\\"620\\\" xmlns:xlink=\\\"http://www.w3.org/1999/xlink\\\" xmlns=\\\"http://www.w3.org/2000/svg\\\"><g transform=\\\"translate(10, 10)\\\"><rect class=\\\"input\\\" height=\\\"30\\\" width=\\\"125\\\" x=\\\"0.00\\\" y=\\\"75.00\\\"></rect><rect class=\\\"input\\\" height=\\\"30\\\" width=\\\"125\\\" x=\\\"0.00\\\" y=\\\"125.00\\\"></rect><line class=\\\"line\\\" x1=\\\"125.00\\\" x2=\\\"200.00\\\" y1=\\\"90.00\\\" y2=\\\"90.00\\\"></line><line class=\\\"line\\\" x1=\\\"125.00\\\" x2=\\\"200.00\\\" y1=\\\"140.00\\\" y2=\\\"140.00\\\"></line><rect class=\\\"model\\\" height=\\\"230\\\" width=\\\"125\\\" x=\\\"200.00\\\" y=\\\"0.00\\\"></rect><rect class=\\\"group\\\" height=\\\"142\\\" width=\\\"137\\\" x=\\\"394.00\\\" y=\\\"-6.00\\\"></rect><rect class=\\\"group\\\" height=\\\"92\\\" width=\\\"137\\\" x=\\\"394.00\\\" y=\\\"144.00\\\"></rect><rect class=\\\"output\\\" height=\\\"30\\\" width=\\\"125\\\" x=\\\"400.00\\\" y=\\\"0.00\\\"></rect><rect class=\\\"output\\\" height=\\\"30\\\" width=\\\"125\\\" x=\\\"400.00\\\" y=\\\"50.00\\\"></rect><rect class=\\\"output\\\" height=\\\"30\\\" width=\\\"125\\\" x=\\\"400.00\\\" y=\\\"100.00\\\"></rect><rect class=\\\"output\\\" height=\\\"30\\\" width=\\\"125\\\" x=\\\"400.00\\\" y=\\\"150.00\\\"></rect><rect class=\\\"output\\\" height=\\\"30\\\" width=\\\"125\\\" x=\\\"400.00\\\" y=\\\"200.00\\\"></rect><line class=\\\"line\\\" x1=\\\"325.00\\\" x2=\\\"400.00\\\" y1=\\\"15.00\\\" y2=\\\"15.00\\\"></line><line class=\\\"line\\\" x1=\\\"325.00\\\" x2=\\\"400.00\\\" y1=\\\"65.00\\\" y2=\\\"65.00\\\"></line><line class=\\\"line\\\" x1=\\\"325.00\\\" x2=\\\"400.00\\\" y1=\\\"115.00\\\" y2=\\\"115.00\\\"></line><line class=\\\"line\\\" x1=\\\"325.00\\\" x2=\\\"400.00\\\" y1=\\\"165.00\\\" y2=\\\"165.00\\\"></line><line class=\\\"line\\\" x1=\\\"325.00\\\" x2=\\\"400.00\\\" y1=\\\"215.00\\\" y2=\\\"215.00\\\"></line></g></svg>\"}"}
;; <=

;; **
;;; This is a somewhat hacky way of injecting some CSS into the Gorilla document. It's not ideal, but does the job.
;; **

;; @@
(def css
  "<style>
  .input {
    fill: #8fb8f7;
    stroke: black;
    stroke-width: 3;
  }
  .model {
    fill: #ffd454;
    stroke: black;
    stroke-width: 3;
  }
  .output {
    fill: #d6d6d6;
    stroke: black;
    stroke-width: 3;
  }
  .group {
    fill: #a9d198;
    stroke: none;
  }
  </style>")

(html/html-view css)
;; @@
;; =>
;;; {"type":"html","content":"<style>\n  .input {\n    fill: #8fb8f7;\n    stroke: black;\n    stroke-width: 3;\n  }\n  .model {\n    fill: #ffd454;\n    stroke: black;\n    stroke-width: 3;\n  }\n  .output {\n    fill: #d6d6d6;\n    stroke: black;\n    stroke-width: 3;\n  }\n  .group {\n    fill: #a9d198;\n    stroke: none;\n  }\n  </style>","value":"#gorilla_repl.html.HtmlView{:content \"<style>\\n  .input {\\n    fill: #8fb8f7;\\n    stroke: black;\\n    stroke-width: 3;\\n  }\\n  .model {\\n    fill: #ffd454;\\n    stroke: black;\\n    stroke-width: 3;\\n  }\\n  .output {\\n    fill: #d6d6d6;\\n    stroke: black;\\n    stroke-width: 3;\\n  }\\n  .group {\\n    fill: #a9d198;\\n    stroke: none;\\n  }\\n  </style>\"}"}
;; <=

;; @@

;; @@
