import Vue from 'vue'
import App from './App'

import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import store from './store'
import { INITIAL_STATE } from './store/state'

import 'font-awesome/css/font-awesome.min.css' // Font awesome import

Vue.use(i18n, {
  lng: INITIAL_STATE.lng,
  fallbackLng: INITIAL_STATE.fallbackLng,
  namespace: 'one-click-importer',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#app',
      store,
      template: '<App />',
      components: {App}
    })
  }
})
