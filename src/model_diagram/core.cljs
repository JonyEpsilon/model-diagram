(ns model-diagram.core
  (:require [om.core :as om]
            [om.dom :as dom]
            [model-diagram.diagram :as diagram]))

(def example-model
  {:n-inputs  3
   :n-outputs [3 2]})

(def render-options
  {:box-height        30
   :box-h-spacing     20
   :box-width         125
   :box-w-spacing     75
   :group-box-padding 6
   :canvas-width      600
   :padding           10})

(defn diagram [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:style                   #js {:display "inline-block"}
                    :dangerouslySetInnerHTML #js
                                                 {:__html (diagram/render-model render-options data)}}))))

(om/root diagram example-model
         {:target (. js/document (getElementById "diagram"))})