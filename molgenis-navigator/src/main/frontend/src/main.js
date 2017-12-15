import Vue from 'vue'
import App from './App'
import store from './store'
import router from './router'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'

import { sync } from 'vuex-router-sync'
import { INITIAL_STATE } from './store/state'

import BootstrapVue from 'bootstrap-vue'
import 'font-awesome/css/font-awesome.min.css'

sync(store, router)
Vue.use(BootstrapVue)

Vue.use(i18n, {
  lng: INITIAL_STATE.lng,
  fallbackLng: INITIAL_STATE.fallbackLng,
  namespace: 'navigator',
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
