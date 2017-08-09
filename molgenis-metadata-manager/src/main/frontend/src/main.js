import 'es6-promise/auto'
import 'babel-polyfill'

import Vue from 'vue'
import store from './store'
import router from './router'

import App from './App'
import VueSweetAlert from 'vue-sweetalert'
import Toaster from 'v-toaster'

import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import { sync } from 'vuex-router-sync'
import { INITIAL_STATE } from './store/state'

import 'bootstrap/dist/css/bootstrap.css'
import 'font-awesome/css/font-awesome.min.css'
import 'v-toaster/dist/v-toaster.css'

sync(store, router)

Vue.use(VueSweetAlert)
Vue.use(Toaster)

Vue.use(i18n, {
  lng: INITIAL_STATE.lng,
  fallbackLng: INITIAL_STATE.fallbackLng,
  namespace: 'metadata-manager',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#app',
      store,
      router,
      template: '<App />',
      components: { App }
    })
  }
})
