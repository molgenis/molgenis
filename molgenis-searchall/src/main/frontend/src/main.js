import Vue from 'vue'
import store from './store'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'

import { searchall } from './store/state'

import App from './App'

import 'font-awesome/css/font-awesome.min.css' // Font awesome import

Vue.use(i18n, {
  lng: searchall.lng,
  fallbackLng: searchall.fallbackLng,
  namespace: 'searchall',
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
