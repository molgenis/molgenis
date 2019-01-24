// @flow
import type {Group, GroupMember, SecurityModel, Toast, LoginUser, User, GroupRole} from '../flow.type'

const getters = {
  getLoginUser: (state: SecurityModel): LoginUser => {
    return state.loginUser
  },
  groups: (state: SecurityModel): Array<Group> => {
    return state.groups
  },
  groupMembers: (state: SecurityModel): Array<GroupMember> => {
    return state.groupMembers
  },
  groupRoles: (state: SecurityModel): Array<GroupRole> => {
    return state.groupRoles
  },
  groupPermissions: (state: SecurityModel): Array<string> => {
    return state.groupPermissions
  },
  users: (state: SecurityModel): Array<User> => {
    return state.users
  },
  toast: (state: SecurityModel): Toast => {
    return state.toast
  }
}
export default getters
