import Vue from 'vue'
import Router from 'vue-router'
import QuestionnaireList from '../pages/QuestionnaireList'
import QuestionnaireStart from '../pages/QuestionnaireStart'
import QuestionnaireThankYou from '../pages/QuestionnaireThankYou'
import QuestionnaireOverview from '../pages/QuestionnaireOverview'
import QuestionnaireChapter from '../pages/QuestionnaireChapter'

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
      component: QuestionnaireStart
    },
    {
      path: '/:questionnaireId/chapter/:chapterId',
      props: (route) => {
        return {
          questionnaireId: route.params.questionnaireId,
          chapterId: parseInt(route.params.chapterId)
        }
      },
      component: QuestionnaireChapter
    },
    {
      path: '/:questionnaireId/submitted',
      props: true,
      component: QuestionnaireThankYou
    },
    {
      path: '/:questionnaireId/overview',
      props: true,
      component: QuestionnaireOverview
    }
  ]
})
