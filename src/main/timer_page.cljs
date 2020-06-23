(ns timer-page
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [state.subs :as sub]
   [clojure.string :refer [join]]
   [date-fns :as date-fns]
   [util.time :refer [seconds->duration diff-in-duration]]
   [util.dev :refer [dev-panel]]
   [component.timer :as clock]
   [component.input :as input]
   [component.icon :as icon]
   ))


; TODO: can't put "compound-duration" above in with-let. Atom seems to not get evalutated so need second let? Or anther way?
; TODO: More control on which unit time when shown. But do I really need days?
(defn timer-simple []
  (reagent/with-let [state (reagent/atom {:start (.now js/Date)
                                          :now (.now js/Date)
                                          :running-length 0 ; diff in seconds

                                          :start? false
                                          :clean? true ; Inital state of running not have happened at all. E.g user interaction Clean
                                          :running? false

                                          :ms-visible? false
                                          :ms-placement "bottom"
                                          :dev? (rf/subscribe [:dev?])}) ; No button currently for dev?
                     timer-fn     (fn [] (js/setInterval
                                          (fn []
                                            ; (js/console.log "hello from timer-fn")
                                            (swap! state assoc-in [:now] (.now js/Date))) 1000))

                    ;  timer-fn     (js/setInterval
                    ;                #(swap! state assoc-in [:now] (.now js/Date)) 70)

                     title-atom (reagent/atom nil)
                     timer-id (reagent/atom nil)

                     ;;  Initial Start
                     fn-start (fn [e]
                                (let [
                                      ; pomo-next-length (date-fns/differenceInMinutes (get-in @state [:value-next-end]) (get-in @state [:value-next-start]))
                                      ]
                                  ; (js/console.log "hello from fn-start")
                                  (swap! state assoc-in [:clean?] false)
                                  (swap! state assoc-in [:running?] true)
                                  (swap! state assoc-in [:now] (.now js/Date))
                                  (swap! state assoc-in [:start] (.now js/Date))
                                  (swap! timer-id timer-fn)))

                     fn-reset (fn [e]
                                (js/clearInterval @timer-id)
                                (swap! state assoc-in [:clean?] true)
                                (swap! state assoc-in [:running?] false)
                                (swap! state assoc-in [:start] (.now js/Date))
                                (swap! state assoc-in [:end] (.now js/Date))
                                )

                     fn-resume (fn [e]
                                 (let [
                                      ;  pomo-left-length (date-fns/differenceInMilliseconds (get-in @state [:end]) (get-in @state [:start]))
                                       ]
                                   (swap! state assoc-in [:clean?] false)
                                   (swap! state assoc-in [:running?] true)
                                   (swap! state assoc-in [:start] (date-fns/addMinutes (.now js/Date) 0))
                                   (swap! timer-id timer-fn)))

                     fn-pause (fn [e]
                                (js/clearInterval @timer-id)
                                (swap! state assoc-in [:running?] false))
                     
                     ]  ;refreshed every 70ms. 1000ms = 1sec
    (let [compound-duration-all (diff-in-duration (get-in @state [:now]) (get-in @state [:start]))
          compound-duration-filtered (dissoc compound-duration-all :w :d)  ; Remove week, days. (Maybe add back if needed one day but it disables showing those two then.)
          ms (when (get-in @state [:running?]) (mod (date-fns/differenceInMilliseconds (get-in @state [:now]) (get-in @state [:start])) 1000))
          compound-duration-plus-ms (assoc compound-duration-filtered :ms ms)
          compound-duration (if (get-in @state [:running?]) compound-duration-plus-ms {:h 0 :m 0 :s 0 :ms 0})  ; Although component has default explictly choosing when on/off this way.
          
          clean? (get-in @state [:clean?])
          running? (get-in @state [:running?])
          ]
      [:div.flex.flex-col.items-center.justify-center.content-center.self-center
       (let [class-bg @(rf/subscribe [:theme/general-bg 100])
             class-text @(rf/subscribe [:theme/general-text 400])
             hover (str "hover:" class-bg)
             class (join  " " ["mb-3" "rounded" #_"bg-gray-100" hover class-text])]
         [input/title {:value @title-atom
                       :class class
                       :on-change (fn [e] (reset! title-atom (-> e .-target .-value)))}])
       [:div.flex.flex-row.text-6xl.tracking-wide.leading-none.text-opacity-100.cursor-pointer.select-none
        {:on-click #(swap! state update-in [:ms-visible?] not)}
        
        [clock/digital-clean {:compound-duration compound-duration
                              :ms-placement (get-in @state [:ms-placement])
                              :ms-visible? (get-in @state [:ms-visible?])}]]
       
       [:div.flex.flex-row.mt-10.text-xl.transition-25to100.opacity-50
        [:button#reset.btn.btn-nav.mr-8 {:on-click (fn [e] (fn-reset e))} [icon/stop]]
        [:button.btn.btn-nav {:on-click (fn [e] (if clean?
                                                  (fn-start e)
                                                  (if running? (fn-pause e) (fn-resume e))))}
         (if (get-in @state [:running?]) [icon/pause] [icon/play])]
        
        #_[:button#start_stop.btn.btn-nav {:on-click (fn [e] (if clean?
                                                             (fn-start e)
                                                             (if running? (fn-pause e) (fn-resume e))))
                                         :disabled finished?
                                         :class (when finished? "cursor-not-allowed opacity-50")}
         (if clean?
           [icon/play]
           (if running? [icon/pause] [icon/play]))]]
       
       (when @(get-in @state [:dev?]) [dev-panel [state]])])
    (finally (js/clearInterval timer-fn))))

(defn timer-panel-nav []
  ; below is non-used attempt to auto add. User can press + and it will add new component (without destorying older)
  #_[:div.flex-center
   (doall
    (for [i (range 7)]
      [:div {:class "w-1/3"}
       [timer-simple]]))
   ]
  [:div.flex-center.h-full
   [timer-simple]]
  )

(defn timer-page-container []
  [:main [timer-panel-nav]])


(comment
  ; https://cljs.github.io/api/cljs.core/clj-GTjs
  (clj->js {"foo" 1 "bar" 2})
  (clj->js {:foo 1 :bar 2})
  (clj->js [:foo "bar" 'baz])
  (clj->js [1 {:foo "bar"} 4])
  
  (.stringify js/JSON (clj->js {:key "value"}))
  (.stringify js/JSON (clj->js (clj->js {"foo" 1 "bar" 2})))
  (.stringify js/JSON (clj->js (clj->js {:foo 1 :bar 2})))
  
  (js/console.log (clj->js {:foo 1 :bar 2})) ; So this does what I want! JS object json.
  
  (date-fns/formatDuration (clj->js {:months 1 :days 2}))
  (date-fns/formatDuration (clj->js {:seconds 55}))
  (date-fns/formatDuration (clj->js {:seconds 5555}))
  
  (date-fns/formatDuration (clj->js {:seconds 5555}) ["minutes" "seconds"])
  (date-fns/formatDuration (clj->js {:minutes 22 :seconds 5555}) ["minutes" "seconds"])
  
  (date-fns/formatDistance (.now js/Date) (.now js/Date))
  (date-fns/formatDistanceStrict (.now js/Date) (.now js/Date) (clj->js {:unit "minute"}))
  (date-fns/formatDistanceStrict (date-fns/addSeconds (.now js/Date) 123213) (.now js/Date) (clj->js {:unit "minute"}))
  (date-fns/formatDistanceStrict (date-fns/addSeconds (.now js/Date) 123213) (.now js/Date) (clj->js {:unit "second"}))
  
  (seconds->duration 56)
  (seconds->duration 60)
  (seconds->duration 61)
  (seconds->duration 3605)
  )