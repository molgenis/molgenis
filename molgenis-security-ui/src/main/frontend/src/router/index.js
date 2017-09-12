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
      path: '/user/:id/'
    },
    {
      path: '/user/:id/:entityType'
    },
    {
      path: '/role/'
    },
    {
      path: '/role/:userId/'
    },
    {
      path: '/role/:sid/edit'
    },
    {
      path: '/role/:roleId/:entityType'
    },
    {
      path: '/resource/:entityType/'
    },
    {
      path: '/resource/:entityType/:id',
      component: ResourcePermissionManager
    }
  ]
})
