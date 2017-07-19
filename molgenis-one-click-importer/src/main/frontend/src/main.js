import 'es6-promise/auto'
import 'babel-polyfill'

import Vue from 'vue'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'

import App from './App'
import VueSweetAlert from 'vue-sweetalert'

import 'bootstrap/dist/css/bootstrap.css' // Bootstrap import
import 'font-awesome/css/font-awesome.min.css' // Font awesome import

Vue.use(VueSweetAlert)

Vue.use(i18n, {
  lng: 'en',
  fallbackLng: 'en',
  namespace: 'molgenis-one-click-importer',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#molgenis-one-click-importer',
      template: '<App />',
      components: {App}
    })
  }
})
