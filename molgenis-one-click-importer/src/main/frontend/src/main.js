import 'es6-promise/auto'
import 'babel-polyfill'

import Vue from 'vue'
import store from './store'
import router from './router'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'

import { sync } from 'vuex-router-sync'
import { INITIAL_STATE } from './store/state'

import App from './App'
import VueSweetAlert from 'vue-sweetalert'
import VueClip from 'vue-clip'

import 'bootstrap/dist/css/bootstrap.css' // Bootstrap import
import 'font-awesome/css/font-awesome.min.css' // Font awesome import

sync(store, router)

Vue.use(VueSweetAlert)
Vue.use(VueClip)

Vue.use(i18n, {
  lng: INITIAL_STATE.lng,
  fallbackLng: INITIAL_STATE.fallbackLng,
  namespace: 'molgenis-one-click-importer',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#molgenis-one-click-importer',
      store,
      router,
      template: '<App />',
      components: { App }
    })
  }
})
