(ns state.subs
  (:require
   [re-frame.core :as rf]))


(rf/reg-sub
 :name
 (fn [db]
   (:name db)))

(rf/reg-sub
 :dev?
 (fn [db]
   (get-in db [:dev :dev?])))

(rf/reg-sub
 :dev-panel?
 (fn [db]
   (get-in db [:dev :dev-panel?])))

(rf/reg-sub
 :theme-time-style
 (fn [db]
   (get-in db [:theme :time :style])))

(rf/reg-sub
 :theme/general
 (fn [db]
   (get-in db [:theme :general])))


(comment
  (pr-str @(rf/subscribe [:dev?])) 
  )