// @flow
// $FlowFixMe
import {SET_MEMBER, SET_MEMBERS, SET_ROLES, SET_USERS_GROUPS} from './mutations'

import type {Member, State} from './utils/flow.types'

export const QUERY_MEMBERS = '__QUERY_MEMBERS__'
export const GET_MEMBER = '__GET_MEMBER__'
export const GET_ROLES = '__  GET_ROLES__'
export const GET_USERS_GROUPS = '__GET_USERS_GROUPS__'
export const CREATE_MEMBER = '__CREATE_MEMBER__'
export const UPDATE_MEMBER = '__UPDATE_MEMBER__'
export const DELETE_MEMBER = '__DELETE_MEMBER__'

const roles = [
  {id: 'role0', label: 'Limited view'},
  {id: 'role1', label: 'Directory'},
  {id: 'role2', label: 'Admin'},
  {id: 'role3', label: 'Manager'},
  {id: 'role4', label: 'Radboud editor'}
]

const usersAndGroups = [
  {type: 'group', id: 'group0', label: 'Authenticated user'},
  {type: 'group', id: 'group1', label: 'Anonyous users'},
  {type: 'group', id: 'group2', label: 'BBMRI-ERIC directory'},
  {type: 'user', id: 'user0', label: 'David van Enckevort'},
  {type: 'user', id: 'user1', label: 'Morris Swertz'},
  {type: 'user', id: 'user2', label: 'Mariska Slofstra'},
  {type: 'user', id: 'user3', label: 'Marieke Bijlsma'},
  {type: 'user', id: 'user4', label: 'Remco den Ouden'}
]

let members = [{
  id: 'group0',
  role: 'role0',
  from: '2010-12-30T12:34',
  until: '2020-11-20T21:43'
}, {
  id: 'group1',
  role: 'role0',
  from: '2010-12-30T12:34',
  until: '2020-11-20T21:43'
}, {
  id: 'group2',
  role: 'role1',
  from: '2010-12-30T12:34',
  until: '2020-11-20T21:43'
}, {
  id: 'user0',
  role: 'role2',
  from: '2010-12-30T12:34',
  until: '2020-11-20T21:43'
}, {
  id: 'user1',
  role: 'role2',
  from: '2010-12-30T12:34',
  until: '2020-11-20T21:43'
}, {
  id: 'user2',
  role: 'role3',
  from: '2010-12-30T12:34',
  until: '2020-11-20T21:43'
}, {
  id: 'user3',
  role: 'role3',
  from: '2010-12-30T12:34',
  until: '2020-11-20T21:43'
}, {
  id: 'user4',
  role: 'role4',
  from: '2010-12-30T12:34',
  until: '2020-11-20T21:43'
}]

export default {
  [QUERY_MEMBERS]({commit}: { commit: Function }, query: Object) {
    let newMembers
    if (query.query === '' || query.query === undefined) {
      newMembers = members.slice()
    } else {
      newMembers = members.filter(member => {
        const userGroup = usersAndGroups.find(userGroup => userGroup.id === member.id)
        return userGroup ? userGroup.label.toLowerCase().indexOf(query.query) !== -1 : false
      })
    }
    if (query.sort === 'descending') {
      newMembers = newMembers.reverse()
    }
    commit(SET_MEMBERS, newMembers)
  },
  [GET_MEMBER]({commit}: { commit: Function }, id: string) {
    const member = JSON.parse(JSON.stringify(members.find(member => member.id === id)))
    commit(SET_MEMBER, member)
  },
  [GET_ROLES]({commit}: { commit: Function }) {
    commit(SET_ROLES, roles)
  },
  [GET_USERS_GROUPS]({commit}: { commit: Function }) {
    commit(SET_USERS_GROUPS, usersAndGroups)
  },
  [CREATE_MEMBER]({commit}: { commit: Function }, member: Member) {
    members.push(JSON.parse(JSON.stringify(member)))
  },
  [UPDATE_MEMBER]({commit, state}: { commit: Function, state: State }, member: Member) {
    for (let i = 0; i < members.length; i++) {
      if (members[i].type === member.type && members[i].id === member.id) {
        members[i] = JSON.parse(JSON.stringify(member))
      }
    }
  },
  [DELETE_MEMBER]({commit, state}: { commit: Function, state: State }, memberId: Object) {
    members = members.filter(dummyMember => !(dummyMember.type === memberId.type && dummyMember.id === memberId.id))
  }
}
