import Vue from 'vue'
import Router from 'vue-router'
import Settings from '../components/Settings'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Settings',
      component: Settings
    }
  ]
})
