(ns model-diagram.diagram
  (:require [thi.ng.geom.svg.core :as svg]
            [hiccups.runtime :as hiccupsrt])
  (:require-macros [hiccups.core :as hiccups]))

(def render-options
  {:box-height        30
   :box-h-spacing     20
   :box-width         125
   :box-w-spacing     75
   :group-box-padding 6
   :canvas-width      600
   :padding           10})

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

(defn render-model
  [render-options model]
  (let [{:keys [canvas-width box-width box-w-spacing box-height box-h-spacing padding]} render-options
        {:keys [n-inputs n-outputs]} model
        total-outputs (apply + n-outputs)
        max-n (max n-inputs total-outputs)]
    (hiccups/html (svg/svg
                   {:width  (+ canvas-width (* 2 padding))
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
