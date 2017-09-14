// @flow
import type {ACE, ACL, EntityType, Group, Permission, Role, Row, Sid, SidType, State, User} from './utils/flow.types'

export const SET_SELECTED_SID = '__SET_SELECTED_SID__'
export const SET_USERS = '__SET_USERS__'
export const SET_ROLES = '__SET_ROLES__'
export const SET_GROUPS = '__SET_GROUPS__'
export const SET_USERS_IN_ROLE = '__SET_USERS_IN_ROLE__'
export const SET_GROUPS_IN_ROLE = '__SET_GROUPS_IN_ROLE__'
export const SET_SELECTED_ENTITY_TYPE = '__SET_SELECTED_ENTITY_TYPE__'
export const SET_SID_TYPE = '__SET_SID_TYPE__'
export const SET_FILTER = '__SET_FILTER__'
export const SET_ROWS = '__SET_ACLS__'
export const SET_ENTITY_TYPES = '__SET_ENTITY_TYPES__'
export const TOGGLE_PERMISSION = '__TOGGLE_PERMISSION__'
export const TOGGLE_GRANTING = '__TOGGLE_GRANTING__'
export const EDIT_ROLE = '__EDIT_ROLE__'
export const CANCEL_EDIT_ROLE = '__CANCEL_EDIT_ROLE__'
export const SET_ACL = '__SET_ACL__'
export const TOGGLE_PERMISSION_IN_ACL = '__TOGGLE_PERMISSION_IN_ACL__'
export const ADD_ACL_ENTRY = '__ADD_ACL_ENTRY__'

const toRemove = {
  WRITEMETA: ['WRITEMETA'],
  DELETE: ['WRITEMETA', 'DELETE'],
  WRITE: ['WRITEMETA', 'DELETE', 'WRITE'],
  READ: ['WRITEMETA', 'DELETE', 'WRITE', 'READ'],
  COUNT: ['WRITEMETA', 'DELETE', 'WRITE', 'READ', 'COUNT']
}

const toSet = {
  WRITEMETA: ['WRITEMETA', 'DELETE', 'WRITE', 'READ', 'COUNT'],
  DELETE: ['DELETE', 'WRITE', 'READ', 'COUNT'],
  WRITE: ['WRITE', 'READ', 'COUNT'],
  READ: ['READ', 'COUNT'],
  COUNT: ['COUNT']
}

function togglePermission (ace: ACE, permission: Permission) {
  if (ace.permissions.includes(permission)) {
    // remove alles wat er links van staat
    ace.permissions = ace.permissions.filter(p => !toRemove[permission].includes(p))
  } else {
    ace.permissions = toSet[permission]
  }
}

export default {
  [TOGGLE_PERMISSION_IN_ACL] (state: State, payload: { aceIndex: number, permission: string }) {
    const {aceIndex, permission} = payload
    if (!state.acl) {
      return
    }
    const acl: ACL = state.acl
    const ace: ACE = acl.entries[aceIndex]
    togglePermission(ace, permission)
    if (ace.permissions.length === 0) {
      acl.entries = acl.entries.filter(entry => entry !== ace)
    }
  },
  [TOGGLE_PERMISSION] (state: State, payload: { rowIndex: number, aceIndex: number, permission: string }) {
    let {rowIndex, aceIndex, permission} = payload
    if (!state.selectedSid) {
      return
    }
    const sid: Sid = state.type === 'role' ? {authority: state.selectedSid} : {username: state.selectedSid}
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
    if (!state.selectedSid) {
      return
    }
    const sid: Sid = state.type === 'role' ? {authority: state.selectedSid} : {username: state.selectedSid}
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
  [SET_ACL] (state: State, acl: ACL) {
    state.acl = acl
  },
  [ADD_ACL_ENTRY] (state: State, securityId: Sid) {
    if (!state.acl) {
      return
    }
    const ace: ACE = {
      permissions: [],
      granting: true,
      securityId
    }
    state.acl.entries.push(ace)
  },
  [SET_ROLES] (state: State, roles: Role[]) {
    state.roles = roles.map(role => ({...role, users: undefined, groups: undefined}))
  },
  [SET_USERS] (state: State, users: User[]) {
    state.users = users
  },
  [SET_GROUPS] (state: State, groups: Group[]) {
    state.groups = groups
  },
  [SET_SID_TYPE] (state: State, sidType: SidType) {
    state.selectedSid = null
    state.sidType = sidType
  },
  [SET_SELECTED_SID] (state: State, sid: string) {
    state.selectedSid = sid
  },
  [SET_USERS_IN_ROLE] (state: State, users: string[]) {
    const index = state.roles.findIndex(role => role.id === state.selectedSid)
    if (index >= 0) {
      state.roles[index].users = users
    }
  },
  [SET_GROUPS_IN_ROLE] (state: State, groups: string[]) {
    const index = state.roles.findIndex(role => role.id === state.selectedSid)
    if (index >= 0) {
      state.roles[index].groups = groups
    }
  },
  [EDIT_ROLE] (state: State) {
    state.editRole = true
  },
  [CANCEL_EDIT_ROLE] (state: State) {
    state.editRole = false
  },
  [SET_SELECTED_ENTITY_TYPE] (state: State, selectedEntityTypeId: string) {
    state.selectedEntityTypeId = selectedEntityTypeId
    state.filter = ''
  },
  [SET_FILTER] (state: State, filter: string) {
    state.filter = filter
  },
  [SET_ENTITY_TYPES] (state: State, entityTypes: Array<EntityType>) {
    state.entityTypes = entityTypes
  }
}
