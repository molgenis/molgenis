// @flow
import type {Toast, User, Group, SecurityModel} from '../flow.type'

const mutations = {
  setUser (state: SecurityModel, user: User) {
    state.user = user
  },
  setGroups (state: SecurityModel, groups: Array<Group>) {
    state.groups = groups
  },
  clearToast (state: SecurityModel) {
    state.toast = null
  },
  setToast (state: SecurityModel, toast: Toast) {
    state.toast = toast
  }
}
export default mutations
