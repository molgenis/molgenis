// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import store from './store'
import router from './router/index'
import { sync } from 'vuex-router-sync'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import { INITIAL_STATE } from './store/state'

sync(store, router)

/* eslint-disable no-new */
Vue.use(i18n, {
  lng: INITIAL_STATE.lng,
  fallbackLng: INITIAL_STATE.fallbackLng,
  namespace: 'settings',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#app',
      store,
      router,
      template: '<App />',
      components: {App}
    })
  }
})
