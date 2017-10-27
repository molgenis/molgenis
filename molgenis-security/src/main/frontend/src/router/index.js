import Vue from 'vue'
import Router from 'vue-router'
import MembersView from '../components/MembersView.vue'
import MemberEdit from '../components/MemberEdit.vue'
import MemberCreate from '../components/MemberCreate.vue'
import {INITIAL_STATE} from '../store/state'

Vue.use(Router)
export default new Router({
  mode: 'history',
  base: INITIAL_STATE.baseUrl,
  routes: [
    {
      path: '/',
      component: MembersView
    },
    {
      path: '/edit/:type/:id',
      component: MemberEdit
    },
    {
      path: '/create',
      component: MemberCreate
    }
  ]
})
