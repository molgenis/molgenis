// @flow
import type { Group, SecurityModel, Toast, User } from '../flow.type'

const getters = {
  getUser: (state: SecurityModel): User => {
    return state.user
  },
  groups: (state: SecurityModel): Array<Group> => {
    return state.groups
  },
  toast: (state: SecurityModel): Toast => {
    return state.toast
  }
}
export default getters
