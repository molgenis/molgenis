import type { GroupMembership, Member, State } from './utils/flow.types'
import moment from 'moment'

const matches = (string: string, terms: Array<string>): boolean =>
  terms.some(term => string.toLowerCase().indexOf(term) >= 0)

export default {
  terms: (state: State) => state.filter ? state.filter.toLowerCase().split(' ').filter(part => part.trim().length > 0) : [],
  members:
    (state: State, getters) => [...getters.roleMembers, ...getters.groupMembers]
      .filter(member => !state.filter || matches(member.label, getters.terms) || matches(member.role.label, getters.terms)),
  member:
    (state: State, getters) => getters.members.find(member => member.id === state.route.params.membershipId),
  groupsOutOfContext: (state: State, getters) => state.loading ? [] : Object.values(state.groups)
    .filter(group => group.id !== getters.contextId)
    .filter(group => !group.parent),
  roleMembers:
    (state: State, getters) => getters.groupsOutOfContext
      .map(group => ({
        type: 'group',
        id: group.id,
        label: group.label,
        role: group.roles.find(groupRole => getters.roles.some(role => role.id === groupRole.id))
      }))
      .filter(result => !!result.role) // group should have role within context
      .sort((member1, member2) => member1.label.localeCompare(member2.label)),
  groupMembers:
    (state: State) => state.loading ? [] : state.groupMemberships
      .filter(candidate => !candidate.end || moment(candidate.end).isAfter(moment()))
      .map((membership: GroupMembership): Member => ({
        type: 'user',
        role: state.groups && state.groups[membership.group],
        id: membership.user,
        label: state.users[membership.user] && state.users[membership.user].label,
        from: membership.start,
        until: membership.end
      }))
      .sort((member1, member2) => member1.from.localeCompare(member2.from) * (state.sort === 'ascending' ? 1 : -1)),
  nrMembers:
    (state: State, getters) => getters.groupMembers.length,
  nrGroups:
    (state: State, getters) => getters.roleMembers.length,
  contextId: (state: State) => state.route && state.route.params && state.route.params.groupId,
  context:
    (state: State, getters) => getters.contextId && state.groups[getters.contextId],
  roles:
    (state: State, getters) => (state.loading || !getters.context) ? [] : getters.context.children
      .map(child => state.groups[child.id])
      .flatMap(group => group.roles),
  unassignedUsers:
    (state: State, getters) => Object.values(state.users)
      .filter(user => user.active)
      .filter(candidate => !getters.members.some(member => member.id === candidate.id)),
  unassignedGroups:
    (state: State, getters) => getters.groupsOutOfContext
      .filter(candidate => !getters.members.some(member => member.id === candidate.id))
}
