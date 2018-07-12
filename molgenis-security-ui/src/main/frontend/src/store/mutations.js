// @flow
import type {Toast, LoginUser, Group, SecurityModel, User} from '../flow.type'
import Vue from 'vue'

const mutations = {
  setLoginUser (state: SecurityModel, loginUser: LoginUser) {
    state.loginUser = loginUser
  },
  setGroups (state: SecurityModel, groups: Array<Group>) {
    state.groups = groups
  },
  setUsers (state: SecurityModel, users: Array<User>) {
    state.users = users
  },
  setGroupMembers (state: SecurityModel, { groupName, groupMembers }) {
    Vue.set(state.groupMembers, groupName, groupMembers)
  },
  setGroupRoles (state: SecurityModel, { groupName, groupRoles }) {
    Vue.set(state.groupRoles, groupName, groupRoles)
  },
  setGroupPermissions (state: SecurityModel, { groupName, groupPermissions }) {
    Vue.set(state.groupPermissions, groupName, groupPermissions)
  },
  clearToast (state: SecurityModel) {
    state.toast = null
  },
  setToast (state: SecurityModel, toast: Toast) {
    state.toast = toast
  }
}
export default mutations
