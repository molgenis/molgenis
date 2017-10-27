// @flow
import type {Member, Role, Sort, State, User, Group} from './utils/flow.types'

export const SET_FILTER = '__SET_FILTER__'
export const SET_MEMBERS = '__SET_MEMBERS__'
export const SET_MEMBER = '__SET_MEMBER__'
export const SET_ROLES = '__SET_ROLES__'
export const SET_SORT = '__SET_SORT__'
export const SET_GROUPS = '__SET_GROUPS__'
export const SET_USERS = '__SET_USERS__'

export default {
  [SET_FILTER] (state: State, filter: string) {
    state.filter = filter
  },
  [SET_SORT] (state: State, sort: Sort) {
    state.sort = sort
  },
  [SET_MEMBERS] (state: State, members: Array<Member>) {
    state.members = members
  },
  [SET_MEMBER] (state: State, member: Member) {
    state.member = member
  },
  [SET_ROLES] (state: State, roles: Array<Role>) {
    state.roles = roles
  },
  [SET_USERS] (state: State, users: Array<User>) {
    state.users = users
  },
  [SET_GROUPS] (state: State, groups: Array<Group>) {
    state.groups = groups
  }
}
