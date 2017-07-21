// @flow
import type {ACE, ACL, EntityType, GrantedAuthoritySid, Role, Row, State} from './utils/flow.types'

export const SELECT_ROLE = '__SELECT_ROLE__'
export const SET_SELECTED_ENTITY_TYPE = '__SET_SELECTED_ENTITY_TYPE__'
export const SET_FILTER = '__SET_FILTER__'
export const SET_ROWS = '__SET_ACLS__'
export const SET_ENTITY_TYPES = '__SET_ENTITY_TYPES__'
export const SET_ROLES = '__SET_ROLES__'
export const TOGGLE_PERMISSION = '__TOGGLE_PERMISSION__'
export const TOGGLE_GRANTING = '__TOGGLE_GRANTING__'
export const CREATE_ROLE = '__CREATE_ROLE__'
export const CANCEL_CREATE_ROLE = '__CANCEL_CREATE_ROLE__'
export const UPDATE_ROLE = '__UPDATE_ROLE__'
export const CANCEL_UPDATE_ROLE = '__CANCEL_UPDATE_ROLE__'

const toRemove = {
  WRITEMETA: ['WRITEMETA'],
  WRITE: ['WRITEMETA', 'WRITE'],
  READ: ['WRITEMETA', 'WRITE', 'READ'],
  COUNT: ['WRITEMETA', 'WRITE', 'READ', 'COUNT']
}

const toSet = {
  WRITEMETA: ['WRITEMETA', 'WRITE', 'READ', 'COUNT'],
  WRITE: ['WRITE', 'READ', 'COUNT'],
  READ: ['READ', 'COUNT'],
  COUNT: ['COUNT']
}

function togglePermission (ace, permission) {
  if (ace.permissions.includes(permission)) {
    // remove alles wat er links van staat
    ace.permissions = ace.permissions.filter(p => !toRemove[permission].includes(p))
  } else {
    ace.permissions = toSet[permission]
  }
}

export default {
  [TOGGLE_PERMISSION] (state: State, payload: { rowIndex: number, aceIndex: number, permission: string }) {
    let {rowIndex, aceIndex, permission} = payload
    if (!state.selectedRole) {
      return
    }
    const sid: GrantedAuthoritySid = {authority: state.selectedRole}
    const acl: ACL = state.rows[rowIndex].acl
    if (aceIndex === -1) {
      const ace: ACE = {
        permissions: [],
        granting: true,
        securityId: sid
      }
      acl.entries.push(ace)
      aceIndex = acl.entries.length - 1
    }
    const ace: ACE = acl.entries[aceIndex]
    togglePermission(ace, permission)
    if (ace.permissions.length === 0) {
      acl.entries = acl.entries.filter(entry => entry !== ace)
    }
  },
  [TOGGLE_GRANTING] (state: State, payload: { rowIndex: number, aceIndex: number }) {
    let {rowIndex, aceIndex} = payload
    if (!state.selectedRole) {
      return
    }
    const sid: GrantedAuthoritySid = {authority: state.selectedRole}
    const acl: ACL = state.rows[rowIndex].acl
    if (aceIndex === -1) {
      const ace: ACE = {
        permissions: [],
        granting: true,
        securityId: sid
      }
      acl.entries.push(ace)
      aceIndex = acl.entries.length - 1
    }
    const ace: ACE = acl.entries[aceIndex]
    ace.granting = !ace.granting
  },
  [SET_ROWS] (state: State, rows: Array<Row>) {
    state.rows = rows
  },
  [SELECT_ROLE] (state: State, role: string) {
    state.selectedSid = role
    state.selectedRole = role
    state.doCreateRole = false
  },
  [CREATE_ROLE] (state: State) {
    state.selectedRole = null
    state.doCreateRole = true
  },
  [CANCEL_CREATE_ROLE] (state: State) {
    state.doCreateRole = false
  },
  [UPDATE_ROLE] (state: State) {
    state.doUpdateRole = true
  },
  [CANCEL_UPDATE_ROLE] (state: State) {
    state.doUpdateRole = false
  },
  [SET_SELECTED_ENTITY_TYPE] (state: State, selectedEntityTypeId: string) {
    state.selectedEntityTypeId = selectedEntityTypeId
  },
  [SET_FILTER] (state: State, filter: string) {
    state.filter = filter
  },
  [SET_ENTITY_TYPES] (state: State, entityTypes: Array<EntityType>) {
    state.entityTypes = entityTypes
  },
  [SET_ROLES] (state: State, roles: Array<Role>) {
    state.roles = roles
  }
}
