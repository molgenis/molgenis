// @flow
import type { Alert, Group, GroupMembership, Sort, State, User } from './utils/flow.types'

export const SET_FILTER = '__SET_FILTER__'
export const SET_GROUP_MEMBERSHIPS = '__SET_GROUP_MEMBERSHIPS__'
export const SET_SORT = '__SET_SORT__'
export const SET_GROUPS = '__SET_GROUPS__'
export const SET_USERS = '__SET_USERS__'
export const SET_LOADING = '__SET_LOADING__'
export const CREATE_ALERT = '__CREATE_ALERT__'
export const REMOVE_ALERT = '__REMOVE_ALERT__'

export default {
  /**
   * Alert mutations
   * @param alert Object containing 'type' and 'message' Strings
   */
  [CREATE_ALERT] (state: State, alert: Alert) {
    state.alerts.push(alert)
  },
  [REMOVE_ALERT] (state: State, index: number) {
    state.alerts.splice(index, 1)
  },
  [SET_LOADING] (state: State, loading: boolean) {
    state.loading = state.loading + (loading ? 1 : -1)
  },
  [SET_FILTER] (state: State, filter: string) {
    state.filter = filter
  },
  [SET_SORT] (state: State, sort: Sort) {
    state.sort = sort
  },
  [SET_GROUP_MEMBERSHIPS] (state: State, members: Array<GroupMembership>) {
    state.groupMemberships = members
  },
  [SET_USERS] (state: State, users: { [string]: User }) {
    state.users = users
  },
  [SET_GROUPS] (state: State, groups: { [string]: Group }) {
    state.groups = groups
  }
}
