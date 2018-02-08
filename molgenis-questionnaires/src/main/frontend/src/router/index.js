import Vue from 'vue'
import Router from 'vue-router'
// List of questionnaires
import QuestionnaireList from '../components/QuestionnaireList'
// Questionnaire + child routes
import QuestionnaireContainer from '../components/QuestionnaireContainer'
import QuestionnaireForm from '../components/QuestionnaireForm'
import QuestionnaireThankYou from '../components/QuestionnaireThankYou'
import QuestionnaireOverview from '../components/QuestionnaireOverview'

const {baseUrl} = window.QUESTIONNAIRE_STATE

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: baseUrl,
  routes: [
    {
      path: '/',
      component: QuestionnaireList
    },
    {
      path: '/:questionnaireName',
      props: true,
      component: QuestionnaireContainer,
      children: [
        {
          path: '',
          props: true,
          component: QuestionnaireForm
        },
        {
          path: 'thanks',
          props: true,
          component: QuestionnaireThankYou
        },
        {
          path: 'overview',
          props: true,
          component: QuestionnaireOverview
        }
      ]
    }
  ]
})
