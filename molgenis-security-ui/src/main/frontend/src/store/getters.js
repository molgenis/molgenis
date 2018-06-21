// @flow
import type {Group, GroupMember, SecurityModel, Toast, User} from '../flow.type'

const getters = {
  getUser: (state: SecurityModel): User => {
    return state.user
  },
  groups: (state: SecurityModel): Array<Group> => {
    return state.groups
  },
  groupMembers: (state: SecurityModel): Array<GroupMember> => {
    return state.groupMembers
  },
  toast: (state: SecurityModel): Toast => {
    return state.toast
  }
}
export default getters
