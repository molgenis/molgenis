// @flow
import type {Toast, User, Group, SecurityModel} from '../flow.type'
import Vue from 'vue'

const mutations = {
  setUser (state: SecurityModel, user: User) {
    state.user = user
  },
  setGroups (state: SecurityModel, groups: Array<Group>) {
    state.groups = groups
  },
  setGroupMembers (state: SecurityModel, { groupName, groupMembers }) {
    Vue.set(state.groupMembers, groupName, groupMembers)
  },
  clearToast (state: SecurityModel) {
    state.toast = null
  },
  setToast (state: SecurityModel, toast: Toast) {
    state.toast = toast
  }
}
export default mutations
