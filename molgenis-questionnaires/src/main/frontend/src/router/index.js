import Vue from 'vue'
import Router from 'vue-router'
import QuestionnaireList from '../pages/QuestionnaireList'
import QuestionnaireStart from '../pages/QuestionnaireStart'
import QuestionnaireSubmitted from '../pages/QuestionnaireSubmitted'
import QuestionnaireOverview from '../pages/QuestionnaireOverview'
import QuestionnaireChapter from '../pages/QuestionnaireChapter'
import ChangePage from '../pages/ChangePage'

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
          // Make sure chapterId is of type number
          chapterId: parseInt(route.params.chapterId)
        }
      },
      component: QuestionnaireChapter
    },
    {
      path: '/:questionnaireId/change/:chapterId',
      props: (route) => {
        return {
          questionnaireId: route.params.questionnaireId,
          chapterId: parseInt(route.params.chapterId)
        }
      },
      component: ChangePage
    },
    {
      path: '/:questionnaireId/submitted',
      props: true,
      component: QuestionnaireSubmitted
    },
    {
      path: '/:questionnaireId/overview',
      props: true,
      component: QuestionnaireOverview
    }
  ]
})
