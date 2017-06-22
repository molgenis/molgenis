import 'es6-promise/auto'
import 'babel-polyfill'

import Vue from 'vue'
import store from './store'
import router from './router'

import App from './App'
import VueNotie from 'vue-notie'

import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import { sync } from 'vuex-router-sync'
import { INITIAL_STATE } from './store/state'

import 'bootstrap/dist/css/bootstrap.css'
import 'font-awesome/css/font-awesome.min.css'

sync(store, router)

Vue.use(VueNotie)

Vue.use(i18n, {
  lng: INITIAL_STATE.lng,
  fallbackLng: INITIAL_STATE.fallbackLng,
  namespace: 'molgenis-metadata-manager',
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
