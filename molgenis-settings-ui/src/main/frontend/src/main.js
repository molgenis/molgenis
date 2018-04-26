import Vue from 'vue'
import Router from 'vue-router'
import App from './App'
import SettingsUi from './components/SettingsUi'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import 'font-awesome/css/font-awesome.min.css'

Vue.use(Router)

const {lng, fallbackLng} = window.__INITIAL_STATE__

const settingsRouter = new Router({
  mode: 'history',
  base: window.__INITIAL_STATE__.baseUrl,
  routes: [
    {
      path: '/',
      redirect: '/sys_set_app'
    },
    {
      path: '/:setting',
      component: SettingsUi
    }
  ]
})

/* eslint-disable no-new */
Vue.use(i18n, {
  lng: lng,
  fallbackLng: fallbackLng,
  namespace: 'settings',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#settings-plugin',
      router: settingsRouter,
      template: '<App />',
      components: {App}
    })
  }
})
