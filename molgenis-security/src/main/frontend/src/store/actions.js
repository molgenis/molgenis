// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { CREATE_ALERT, SET_GROUP_MEMBERSHIPS, SET_GROUPS, SET_LOADING, SET_USERS } from './mutations'
import type { GroupMembership, GroupMembershipResponse, Member, Optional, State } from './utils/flow.types'

export const FETCH_DATA = '__FETCH_DATA__'
const QUERY_MEMBERS = '__QUERY_MEMBERS__'
const GET_USERS_GROUPS = '__GET_USERS_GROUPS__'
export const CREATE_MEMBER = '__CREATE_MEMBER__'
export const UPDATE_MEMBER = '__UPDATE_MEMBER__'
export const DELETE_MEMBER = '__DELETE_MEMBER__'

const getUserLabel = (user) => {
  const nameParts = [user.FirstName, user.MiddleNames, user.LastName].filter(part => !!part)
  if (nameParts.length === 0) {
    return user.username
  }
  return [user.title, ...nameParts].filter(part => !!part).join(' ')
}

function getOrThrow<T> (optional: Optional<T>): T {
  if (!optional.value) {
    throw new Error('Optional value not specified')
  }
  return optional.value
}

function getOrElse<T> (optional: Optional<T>, otherwise: ?T): ?T {
  return optional.value || otherwise
}

const toGroupMembership = (groupMembership: GroupMembershipResponse): GroupMembership => ({
  id: getOrThrow(groupMembership.id),
  group: getOrThrow(groupMembership.group.id),
  user: getOrThrow(groupMembership.user.id),
  start: groupMembership.start,
  end: getOrElse(groupMembership.end, null)
})

const withSpinner = (commit, promise) => {
  commit(SET_LOADING, true)
  promise.catch(error => {
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
      withSpinner(commit, api.get(`/group/${state.route.params.groupId}/members`).then(response => {
        commit(SET_GROUP_MEMBERSHIPS, response.map(toGroupMembership))
      }))
    }
  },
  [GET_USERS_GROUPS] ({commit}: { commit: Function }) {
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
    }))
    withSpinner(commit, api.get('/api/v2/sys_sec_Group').then(json => {
      const groupsByKey = json.items.reduce((map, group) => ({...map, [group.id]: group}), {})
      commit(SET_GROUPS, groupsByKey)
    }))
  },
  [CREATE_MEMBER] ({commit}: { commit: Function }, member: Member) {
    // state.groupMemberships.push(JSON.parse(JSON.stringify(member)))
  },
  [UPDATE_MEMBER] ({commit, state}: { commit: Function, state: State }, member: Member) {
    // for (let i = 0; i < members.length; i++) {
    //   if (members[i].type === member.type && members[i].id === member.id) {
    //     members[i] = JSON.parse(JSON.stringify(member))
    //   }
    // }
  },
  [DELETE_MEMBER] ({commit, state}: { commit: Function, state: State }, memberId: Object) {
    // members = members.filter(dummyMember => !(dummyMember.type === memberId.type && dummyMember.id === memberId.id))
  }
}
