import 'es6-promise/auto'
if (!window._babelPolyfill) {
  require('babel-polyfill')
}

import Vue from 'vue'
import store from './store'
import router from './router'

import { sync } from 'vuex-router-sync'

import App from './App'

import 'font-awesome/css/font-awesome.min.css' // Font awesome import

import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import { INITIAL_STATE } from './store/state'

sync(store, router)

Vue.use(i18n, {
  lng: INITIAL_STATE.lng,
  fallbackLng: INITIAL_STATE.fallbackLng,
  molgenisPackage: INITIAL_STATE.molgenisPackage,
  namespace: 'model-registry-uml-viewer',
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
