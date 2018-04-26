import Vue from 'vue'
import Router from 'vue-router'
import SearchAll from 'components/SearchAll'
import { searchall } from '../store/state'

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: searchall.baseUrl,
  routes: [
    {
      path: '/',
      component: SearchAll
    }
  ]
})
