import 'es6-promise/auto'
import 'babel-polyfill'

import Vue from 'vue'
import store from './store'
import router from './router'

import { sync } from 'vuex-router-sync'

import App from './App'

import 'bootstrap/dist/css/bootstrap.css' // Bootstrap import
import 'font-awesome/css/font-awesome.min.css' // Font awesome import

sync(store, router)

/* eslint-disable no-new */
new Vue({
  // determine in which div you want to load the VUE-application
  el: '#app',
  store,
  router,
  template: '<App />',
  components: {App}
})
