export default {
  getMembers: state => state.members,
  getMember: state => state.member,
  getRoles: state => state.roles,
  getUsersGroups: state => [...state.groups.map(group => ({...group, type: 'group'})),
    ...state.users.map(user => ({...user, type: 'user'}))]
}
