(ns model-diagram.core
  (:require [om.core :as om]
            [om.dom :as dom]
            [model-diagram.diagram :as diagram]))

(def example-model
  {:n-inputs  3
   :n-outputs [3 2]})

(defn diagram [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:style #js {:display "inline-block"}
                    :dangerouslySetInnerHTML
                           #js {:__html (diagram/render-model diagram/render-options data)}}))))

(om/root diagram example-model
         {:target (. js/document (getElementById "diagram"))})