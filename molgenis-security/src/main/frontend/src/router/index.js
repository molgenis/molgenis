import Vue from 'vue'
import Router from 'vue-router'
import MembersView from '../components/MembersView.vue'
import MemberCreate from '../components/MemberCreate.vue'
import { INITIAL_STATE } from '../store/state'

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: INITIAL_STATE.baseUrl,
  routes: [
    {
      path: '/:groupId',
      component: MembersView
    },
    {
      path: '/:groupId/:membershipId',
      component: MemberCreate,
      name: 'edit'
    },
    {
      path: '/:groupId/create',
      component: MemberCreate
    }
  ]
})
