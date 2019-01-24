import Vue from 'vue'
import App from './App'
import { sync } from 'vuex-router-sync'
import store from './store'
import router from './router'

import BootstrapVue from 'bootstrap-vue'

import { library } from '@fortawesome/fontawesome-svg-core'
import { faCheckCircle, faFolderOpen, faHourglass, faTimesCircle } from '@fortawesome/free-regular-svg-icons'
import { faClone, faCut, faEdit, faDownload, faHome, faList, faPaste, faPlus, faSearch, faTimes, faTrash, faUpload } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

import i18n from '@molgenis/molgenis-i18n-js'
import {
  SET_SHOW_HIDDEN_RESOURCES
} from './store/mutations'

const {lng, fallbackLng, isSuperUser} = window.__INITIAL_STATE__

// Keeps the router and the store in sync @https://github.com/vuejs/vuex-router-sync
sync(store, router)

library.add(faCheckCircle, faClone, faCut, faEdit, faDownload, faFolderOpen, faHome, faHourglass, faList, faPaste, faPlus, faSearch, faTimes, faTimesCircle, faTrash, faUpload)

Vue.component('font-awesome-icon', FontAwesomeIcon)

Vue.use(BootstrapVue)

Vue.use(i18n, {
  lng: lng,
  fallbackLng: fallbackLng,
  namespace: 'navigator',
  callback () {
    /* eslint-disable no-new */
    new Vue({
      el: '#app',
      store,
      router,
      components: { App },
      template: '<App />'
    })
    store.commit(SET_SHOW_HIDDEN_RESOURCES, isSuperUser)
  }
})
