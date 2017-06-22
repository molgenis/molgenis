import 'es6-promise/auto'
import 'babel-polyfill'
import Vue from 'vue'

import App from './App'
import { sync } from 'vuex-router-sync'
import store from './store'
import router from './router'
import BootstrapVue from 'bootstrap-vue/dist/bootstrap-vue.esm'
import VueNotie from 'vue-notie'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import 'font-awesome/css/font-awesome.min.css'

sync(store, router)

Vue.use(BootstrapVue)
Vue.use(VueNotie)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  store,
  router,
  template: '<App />',
  components: { App }
})
