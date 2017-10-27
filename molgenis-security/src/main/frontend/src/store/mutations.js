// @flow
import type {Member, Role, Sort, State, UserOrGroup} from './utils/flow.types'

export const SET_QUERY = '__SET_QUERY__'
export const SET_MEMBERS = '__SET_MEMBERS__'
export const SET_MEMBER = '__SET_MEMBER__'
export const SET_ROLES = '__SET_ROLES__'
export const SET_SORT = '__SET_SORT__'
export const SET_USERS_GROUPS = '__SET_USERS_GROUPS__'

export default {
  [SET_QUERY](state: State, query: string) {
    state.query = query
  },
  [SET_SORT](state: State, sort: Sort) {
    state.sort = sort
  },
  [SET_MEMBERS](state: State, members: Array<Member>) {
    state.members = members
  },
  [SET_MEMBER](state: State, member: Member) {
    state.member = member
  },
  [SET_ROLES](state: State, roles: Array<Role>) {
    state.roles = roles
  },
  [SET_USERS_GROUPS](state: State, usersGroups: Array<UserOrGroup>) {
    state.usersGroups = usersGroups
  }
}
