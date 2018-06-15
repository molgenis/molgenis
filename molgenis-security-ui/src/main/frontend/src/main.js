import Vue from 'vue'
import Router from 'vue-router'
import SecurityApp from './SecurityApp'
import GroupOverview from './components/GroupOverview'
import i18n from '@molgenis/molgenis-i18n-js'

Vue.use(Router)

const {lng, fallbackLng, baseUrl} = window.__INITIAL_STATE__

const router = new Router({
  mode: 'history',
  base: baseUrl,
  routes: [
    {
      path: '/',
      component: GroupOverview
    }
  ]
})

/* eslint-disable no-new */
Vue.use(i18n, {
  lng: lng,
  fallbackLng: fallbackLng,
  namespace: ['security-ui'],
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#security-ui-plugin',
      router,
      template: '<SecurityApp />',
      components: {SecurityApp}
    })
  }
})
