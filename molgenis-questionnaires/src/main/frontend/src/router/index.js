import Vue from 'vue'
import Router from 'vue-router'

import Questionnaire from '../components/Questionnaire'
import QuestionnaireForm from '../components/QuestionnaireForm'

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: window.QUESTIONNAIRE_STATE.baseUrl,
  routes: [
    {
      path: '/',
      component: Questionnaire
    },
    {
      path: '/:questionnaireName',
      props: true,
      component: QuestionnaireForm
    }
  ]
})
