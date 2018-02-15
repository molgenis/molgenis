import Vue from 'vue'
import Router from 'vue-router'
// List of questionnaires
import QuestionnaireList from '../components/QuestionnaireList'
// Questionnaire + child routes
import QuestionnaireContainer from '../components/QuestionnaireContainer'
import QuestionnaireStart from '../components/QuestionnaireStart'
import QuestionnaireThankYou from '../components/QuestionnaireThankYou'
import QuestionnaireOverview from '../components/QuestionnaireOverview'
import QuestionnaireChapter from '../components/QuestionnaireChapter'

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
          redirect: 'start'
        },
        {
          path: 'start',
          props: true,
          component: QuestionnaireStart
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
        },
        {
          path: ':chapter_id',
          props: true,
          component: QuestionnaireChapter
        }
      ]
    }
  ]
})
