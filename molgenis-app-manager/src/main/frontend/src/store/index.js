// @flow
// $FlowFixMe
import Vue from 'vue'
// $FlowFixMe
import Vuex from 'vuex'

import actions from './actions'
import mutations from './mutations'

import type { AppManagerState } from '../flow.types'

const state: AppManagerState = {
  /**
   * List of available apps
   */
  apps: [],

  /**
   * Application wide error message
   */
  error: '',

  /**
   * Switch to indicate if the application is in a loading state
   */
  loading: true
}

Vue.use(Vuex)
export default new Vuex.Store({
  actions,
  mutations,
  state
})
