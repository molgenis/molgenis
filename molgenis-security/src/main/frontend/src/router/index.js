import Vue from 'vue'
import Router from 'vue-router'
import MembersView from '../components/MembersView.vue'
import MemberEdit from '../components/MemberEdit.vue'
import { INITIAL_STATE } from '../store/state'

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: INITIAL_STATE.baseUrl,
  routes: [
    {
      name: 'members',
      path: '/:groupId',
      component: MembersView
    },
    {
      path: '/:groupId/:membershipId',
      component: MemberEdit,
      name: 'edit'
    },
    {
      path: '/:groupId/create',
      component: MemberEdit,
      name: 'create'
    }
  ]
})
