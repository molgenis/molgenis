// @flow
import type {Toast, Group, SecurityModel} from '../flow.type'

const mutations = {
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
