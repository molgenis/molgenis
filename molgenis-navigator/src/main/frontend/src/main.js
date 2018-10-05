import Vue from 'vue'
import App from './App'
import { sync } from 'vuex-router-sync'
import store from './store'
import router from './router'

import BootstrapVue from 'bootstrap-vue'

import { library } from '@fortawesome/fontawesome-svg-core'
import { faFolderOpen } from '@fortawesome/free-regular-svg-icons'
import { faClone, faCut, faEdit, faDownload, faHome, faList, faPaste, faPlus, faSearch, faTimes, faTrash, faUpload } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

import i18n from '@molgenis/molgenis-i18n-js/dist/molgenis-i18n.esm'
import { INITIAL_STATE } from './store/state'
// Keeps the router and the store in sync @https://github.com/vuejs/vuex-router-sync
sync(store, router)

library.add(faClone, faCut, faEdit, faDownload, faFolderOpen, faHome, faList, faPaste, faPlus, faSearch, faTimes, faTrash, faUpload)

Vue.component('font-awesome-icon', FontAwesomeIcon)

Vue.use(BootstrapVue)

Vue.use(i18n, {
  lng: INITIAL_STATE.lng,
  fallbackLng: INITIAL_STATE.fallbackLng,
  namespace: 'navigator',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#app',
      store,
      router,
      template: '<App />',
      components: { App }
    })
  }
})
