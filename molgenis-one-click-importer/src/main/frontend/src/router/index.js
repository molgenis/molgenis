import Vue from 'vue'
import Router from 'vue-router'
import OneClickImporter from 'components/OneClickImporter'
import { INITIAL_STATE } from '../store/state'

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: INITIAL_STATE.baseUrl,
  routes: [
    {
      path: '/',
      component: OneClickImporter
    }
  ]
})
