// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'

const {lng, fallbackLng} = window.__INITIAL_STATE__

/* eslint-disable no-new */
Vue.use(i18n, {
  lng: lng,
  fallbackLng: fallbackLng,
  namespace: 'settings',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#settings-plugin',
      template: '<App />',
      components: {App}
    })
  }
})
