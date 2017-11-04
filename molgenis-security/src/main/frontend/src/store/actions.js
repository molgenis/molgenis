// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { CREATE_ALERT, SET_GROUP_MEMBERSHIPS, SET_GROUPS, SET_LOADING, SET_USERS } from './mutations'
import type {
  GroupMembership,
  GroupMembershipMutation,
  GroupMembershipResponse,
  GroupRoleMutation,
  State,
  UserGroupMembershipDeletion
} from './utils/flow.types'

export const FETCH_DATA = '__FETCH_DATA__'
const QUERY_MEMBERS = '__QUERY_MEMBERS__'
const GET_USERS_GROUPS = '__GET_USERS_GROUPS__'
export const UPDATE_GROUP_ROLE = '__UPDATE_GROUP_ROLE__'
export const DELETE_GROUP_ROLE = '__DELETE_GROUP_ROLE__'
export const CREATE_MEMBER = '__CREATE_MEMBER__'
export const DELETE_MEMBER = '__DELETE_MEMBER__'

const getUserLabel = (user) => {
  const nameParts = [user.FirstName, user.MiddleNames, user.LastName].filter(part => !!part)
  if (nameParts.length === 0) {
    return user.username
  }
  return [user.title, ...nameParts].filter(part => !!part).join(' ')
}

const toGroupMembership = (groupMembership: GroupMembershipResponse): GroupMembership => ({
  id: groupMembership.id,
  group: groupMembership.group.id,
  user: groupMembership.user.id,
  start: groupMembership.start,
  end: groupMembership.end || null
})

const withSpinner = (commit: Function, promise: Promise<*>): Promise<*> => {
  commit(SET_LOADING, true)
  return promise.catch(error => {
    commit(CREATE_ALERT, {type: 'error', message: error})
  }).then(() => commit(SET_LOADING, false))
}

export default {
  [FETCH_DATA] ({dispatch}: { dispatch: Function }): void {
    dispatch(GET_USERS_GROUPS)
    dispatch(QUERY_MEMBERS)
  },
  [QUERY_MEMBERS] ({commit, state}: { commit: Function, state: State }) {
    commit(SET_GROUP_MEMBERSHIPS, [])
    if (state.route && state.route.params && state.route.params.groupId) {
      return withSpinner(commit, api.get(`/group/${state.route.params.groupId}/members`).then(response => {
        commit(SET_GROUP_MEMBERSHIPS, response.map(toGroupMembership))
      }))
    }
  },
  [GET_USERS_GROUPS] ({commit}: { commit: Function }) {
    const promises = [
      withSpinner(commit, api.get('/api/v2/sys_sec_User').then(json => {
        const usersByKey = json.items.reduce((map, user) => ({
          ...map,
          [user.id]: ({
            id: user.id,
            label: getUserLabel(user),
            active: user.active
          })
        }), {})
        commit(SET_USERS, usersByKey)
      })),
      withSpinner(commit, api.get('/api/v2/sys_sec_Group').then(json => {
        const groupsByKey = json.items.reduce((map, group) => ({...map, [group.id]: group}), {})
        commit(SET_GROUPS, groupsByKey)
      }))]
    return Promise.all(promises)
  },
  [CREATE_MEMBER] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function }, mutation: GroupMembershipMutation): Promise<*> {
    return withSpinner(commit, api.post('/group/updateUserMembership', {body: JSON.stringify(mutation)}).then(
      () => dispatch(QUERY_MEMBERS)
    ))
  },
  [DELETE_MEMBER] ({commit, dispatch}: { commit: Function, dispatch: Function }, mutation: UserGroupMembershipDeletion) {
    return withSpinner(commit, api.post('/group/removeUserFromGroup', {body: JSON.stringify(mutation)})
      .then(() => dispatch(QUERY_MEMBERS)))
  },
  [DELETE_GROUP_ROLE] ({commit, dispatch}: { commit: Function, dispatch: Function },
                       mutation: GroupRoleMutation): Promise<*> {
    return withSpinner(commit, api.post('/group/removeGroupRole', {body: JSON.stringify(mutation)})
      .then(() => dispatch(FETCH_DATA)))
  },
  [UPDATE_GROUP_ROLE] ({commit, getters, dispatch}: { commit: Function, state: State, getters: Object, dispatch: Function },
                       {groupId, roleId}: GroupRoleMutation): Promise<*> {
    const currentMembership = getters.roleMembers.find(member => member.id === groupId)
    if (currentMembership) {
      const mutation = {groupId, roleId: currentMembership.role.id}
      return withSpinner(commit, api.post('/group/removeGroupRole', {body: JSON.stringify(mutation)})
        .then(() => api.post('/group/addGroupRole', {body: JSON.stringify({groupId, roleId})}))
        .then(() => dispatch(FETCH_DATA)))
    } else {
      return withSpinner(commit, api.post('/group/addGroupRole', {body: JSON.stringify({groupId, roleId})})
        .then(() => dispatch(FETCH_DATA)))
    }
  }
}
