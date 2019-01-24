import Vue from 'vue'
import QuestionnaireApp from './QuestionnaireApp'

import store from './store'
import router from './router'

import i18n from '@molgenis/molgenis-i18n-js'
import 'font-awesome/css/font-awesome.min.css'

store.commit('SET_USERNAME', window.QUESTIONNAIRE_STATE.username)
store.commit('SET_LANGUAGE', window.QUESTIONNAIRE_STATE.lng || window.QUESTIONNAIRE_STATE.fallbackLng)

Vue.use(i18n, {
  lng: window.QUESTIONNAIRE_STATE.lng,
  fallbackLng: window.QUESTIONNAIRE_STATE.fallbackLng,
  namespace: ['questionnaire', 'ui-form'],
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#questionnaire-app',
      store,
      router,
      template: '<QuestionnaireApp />',
      components: {QuestionnaireApp}
    })
  }
})
