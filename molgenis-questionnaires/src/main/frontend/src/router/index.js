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

const {baseUrl} = window.QUESTIONNAIRE_STATE || ''

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: baseUrl,
  scrollBehavior () {
    return {x: 0, y: 0}
  },
  routes: [
    {
      path: '/',
      component: QuestionnaireList
    },
    {
      path: '/:questionnaireId',
      props: true,
      component: QuestionnaireContainer,
      children: [
        {
          path: '',
          props: true,
          component: QuestionnaireStart
        },
        {
          path: 'chapter/:chapterId',
          props: (route) => {
            return {
              questionnaireId: route.params.questionnaireId,
              chapterId: parseInt(route.params.chapterId)
            }
          },
          component: QuestionnaireChapter
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
