import Vue from 'vue'
import Router from 'vue-router'
import App from './App'
import DataRowEdit from './components/DataRowEdit'
import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import 'font-awesome/css/font-awesome.min.css'

Vue.use(Router)

const {lng, fallbackLng, baseUrl, dataExplorerBaseUrl} = window.__INITIAL_STATE__

const dataRowEditRouter = new Router({
  mode: 'history',
  base: baseUrl,
  routes: [
    {
      path: '/:dataTableId/:rowId',
      component: DataRowEdit
    },
    {
      path: '/',
      redirect: to => {
        window.location.href = window.location.origin + dataExplorerBaseUrl
      }
    }
  ]
})

/* eslint-disable no-new */
Vue.use(i18n, {
  lng: lng,
  fallbackLng: fallbackLng,
  namespace: 'data-row-edit',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#data-row-edit-plugin',
      router: dataRowEditRouter,
      template: '<App />',
      components: {App}
    })
  }
})
