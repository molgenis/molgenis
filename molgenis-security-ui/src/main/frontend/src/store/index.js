// @flow
import Vue from 'vue'
import Vuex from 'vuex'

import actions from './actions'
import getters from './getters'
import mutations from './mutations'
import type {SecurityModel} from '../flow.type'

const state: SecurityModel = {
  loginUser: {},
  groups: [],
  groupMembers: {},
  groupRoles: {},
  groupPermissions: {},
  users: [],
  toast: null
}

Vue.use(Vuex)
export default new Vuex.Store({
  actions,
  getters,
  mutations,
  state,
  strict: true
})
