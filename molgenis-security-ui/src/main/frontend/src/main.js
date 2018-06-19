import Vue from 'vue'
import Router from 'vue-router'
import store from './store'
import SecurityApp from './SecurityApp'
import GroupOverview from './components/GroupOverview'
import GroupCreate from './components/GroupCreate'
import i18n from '@molgenis/molgenis-i18n-js'

Vue.use(Router)

const {lng, fallbackLng, baseUrl, isSuperUser} = window.__INITIAL_STATE__

const router = new Router({
  mode: 'history',
  base: baseUrl,
  routes: [
    {
      path: '/group',
      name: 'groupOverView',
      component: GroupOverview
    },
    {
      path: '/group/create',
      component: GroupCreate
    },
    {
      path: '/',
      redirect: '/group'
    }
  ]
})

/* eslint-disable no-new */
Vue.use(i18n, {
  lng: lng,
  fallbackLng: fallbackLng,
  namespace: ['security-ui'],
  isSuperUser: isSuperUser,
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#security-ui-plugin',
      router,
      store,
      template: '<SecurityApp />',
      components: {SecurityApp}
    })
  }
})
