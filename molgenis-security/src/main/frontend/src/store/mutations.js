// @flow
import type { ACE, ACL, EntityType, GrantedAuthoritySid, Row, State } from './utils/flow.types'

export const SELECT_SID = '__SELECT_SID__'
export const SET_SELECTED_ENTITY_TYPE = '__SET_SELECTED_ENTITY_TYPE__'
export const SET_FILTER = '__SET_FILTER__'
export const SET_ROWS = '__SET_ACLS__'
export const SET_ENTITY_TYPES = '__SET_ENTITY_TYPES__'
export const SET_SIDS = '__SET_SIDS__'
export const TOGGLE_PERMISSION = '__TOGGLE_PERMISSION__'

export default {
  [TOGGLE_PERMISSION] (state: State, payload: { rowIndex: number, aceIndex: number, permission: string }) {
    console.log('toggle permission', payload)
    let {rowIndex, aceIndex, permission} = payload
    const sid: ?GrantedAuthoritySid = state.sids.find(sid => sid.authority === state.selectedSid)
    if (!sid) {
      state.selectedSid = null
      return
    }
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
    console.log(aceIndex, acl)
    const ace: ACE = acl.entries[aceIndex]
    if (ace.permissions.includes(permission)) {
      ace.permissions = ace.permissions.filter(p => p !== permission)
      if (ace.permissions.length === 0) {
        acl.entries = acl.entries.filter(entry => entry !== ace)
      }
    } else {
      ace.permissions.push(permission)
    }
  },
  [SET_ROWS] (state: State, rows: Array<Row>) {
    state.rows = rows
  },
  [SELECT_SID] (state: State, sid: GrantedAuthoritySid) {
    state.selectedSid = sid.authority
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
  [SET_SIDS] (state: State, sids: Array<GrantedAuthoritySid>) {
    state.sids = sids
  }
}
