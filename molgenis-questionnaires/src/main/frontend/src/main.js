import Vue from 'vue'
import App from './App'

import router from './router'

import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'

Vue.use(i18n, {
  lng: window.QUESTIONNAIRE_STATE.lng,
  fallbackLng: window.QUESTIONNAIRE_STATE.fallbackLng,
  namespace: 'questionnaire',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#app',
      router,
      template: '<App />',
      components: {App}
    })
  }
})
