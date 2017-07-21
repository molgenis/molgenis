// @flow
import type { ACE, EntityType, State } from './utils/flow.types'

export default {
  /**
   * Returns the selected EntityType
   * @param state: State the state
   */
  selectedEntityType: (state: State): ?EntityType => state.entityTypes.find(entityType => entityType.id === state.selectedEntityTypeId),
  tableRows (state: State) {
    if (!state.selectedRole) {
      return []
    }
    const selectedRole: string = state.selectedRole
    return state.rows.map(row => {
      const {entityLabel, acl: {owner: {username}, entries}} = row
      const emptyAce: ACE = {
        permissions: [],
        securityId: {authority: selectedRole},
        granting: true
      }
      const isGrantedAuthority = (sid: string) => (candidate: ACE) => candidate.securityId.authority && candidate.securityId.authority === sid
      const aceIndex = entries.findIndex(isGrantedAuthority(selectedRole))
      const ace = entries.find(isGrantedAuthority(selectedRole)) || emptyAce
      const addPermission = (row, permission) => ({...row, [permission]: ace.permissions.includes(permission)})
      return state.permissions.reduce(addPermission, {entityLabel, owner: username, granting: ace.granting, aceIndex})
    })
  }
}
