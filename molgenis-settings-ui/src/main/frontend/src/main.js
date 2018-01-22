// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import store from './store'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import { INITIAL_STATE } from './store/state'

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
      template: '<App />',
      components: {App}
    })
  }
})
