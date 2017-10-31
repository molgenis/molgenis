import type { GroupMembership, Member, State } from './utils/flow.types'

export default {
  members:
    (state: State): Array<Member> =>
      state.loading ? [] : state.groupMemberships
        .map((membership: GroupMembership): Member => ({
          type: 'user',
          role: state.groups && state.groups[membership.group].label,
          id: membership.user,
          label: state.users[membership.user] && state.users[membership.user].label,
          from: membership.start,
          until: membership.end
        }))
        .filter(member => !state.filter || member.label.toLowerCase().indexOf(state.filter.toLowerCase()) >= 0)
        .sort((member1, member2) => member1.from.localeCompare(member2.from) * (state.sort === 'ascending' ? 1 : -1)),
  member:
    (state: State, getters) => getters.members.find(member => member.id === state.route.params.membershipId),
  getRoles:
    (state: State, getters) => state.groups && Object.values(state.groups).filter(group => group.parent && group.parent.id === getters.contextId),
  nrMembers:
    (state: State) => state.groupMemberships && state.groupMemberships.length,
  nrGroups:
    (state: State) => 0,
  contextId: (state: State) => state.route && state.route.params && state.route.params.groupId,
  context:
    (state: State, getters) => getters.contextId && state.groups[getters.contextId],
  unassignedUsers:
    (state: State, getters) => Object.values(state.users).filter(user => user.active && !getters.members.find(member => member.id === user.id))
}
