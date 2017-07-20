// @flow
import type { EntityType, GrantedAuthoritySid, Row, State } from './utils/flow.types'

export const SELECT_SID = '__SELECT_SID__'
export const SET_SELECTED_ENTITY_TYPE = '__SET_SELECTED_ENTITY_TYPE__'
export const SET_FILTER = '__SET_FILTER__'
export const SET_ROWS = '__SET_ACLS__'
export const SET_ENTITY_TYPES = '__SET_ENTITY_TYPES__'
export const SET_SIDS = '__SET_SIDS__'

export default {
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
