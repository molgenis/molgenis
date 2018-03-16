import Vue from 'vue'
import App from './App'
import { sync } from 'vuex-router-sync'
import store from './store'
import router from './router'
import VueTour from 'vue-tour'
import BootstrapVue from 'bootstrap-vue'

import 'font-awesome/css/font-awesome.min.css'

import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import { INITIAL_STATE } from './store/state'
// Keeps the router and the store in sync @https://github.com/vuejs/vuex-router-sync
sync(store, router)

Vue.use(BootstrapVue)

require('vue-tour/dist/vue-tour.css')

Vue.use(VueTour)

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
