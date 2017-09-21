// @flow
import type { ACE, EntityType, Role, State } from './utils/flow.types'

export default {
  /**
   * Returns the selected EntityType
   * @param state: State the state
   */
  selectedEntityType: (state: State): ?EntityType => state.entityTypes.find(entityType => entityType.id === state.selectedEntityTypeId),
  tableRows (state: State) {
    if (!state.selectedSid) {
      return []
    }
    const selectedSid: string = state.selectedSid
    return state.rows.map(row => {
      const {entityLabel, acl: {owner: {username}, entries}} = row
      const emptyAce: ACE = {
        permissions: [],
        securityId: state.sidType === 'role' ? {authority: selectedSid} : {username: selectedSid},
        granting: true
      }
      const isAuthority = (sid: string) => (candidate: ACE) => candidate.securityId.authority && candidate.securityId.authority === sid
      const isUser = (sid: string) => (candidate: ACE) => candidate.securityId.username && candidate.securityId.username === sid
      const isGrantedAuthority = state.sidType === 'role' ? isAuthority : isUser
      const aceIndex = entries.findIndex(isGrantedAuthority(selectedSid))
      const ace = entries.find(isGrantedAuthority(selectedSid)) || emptyAce
      const addPermission = (row, permission) => ({...row, [permission]: ace.permissions.includes(permission)})
      return state.permissions.reduce(addPermission, {entityLabel, owner: username, granting: ace.granting, aceIndex})
    })
  },
  role (state: State): ?Role {
    return state.roles && state.roles.find(role => role.id === state.selectedSid)
  }
}
