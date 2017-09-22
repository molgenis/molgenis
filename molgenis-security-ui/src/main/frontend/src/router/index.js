import Vue from 'vue'
import Router from 'vue-router'
import PermissionManager from 'components/PermissionManager'
import ResourcePermissionManager from 'components/ResourcePermissionManager'
import {INITIAL_STATE} from '../store/state'

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: INITIAL_STATE.baseUrl,
  routes: [
    {
      path: '/',
      component: PermissionManager
    },
    {
      path: '/resource/:entityType/:id',
      component: ResourcePermissionManager
    }
  ]
})
