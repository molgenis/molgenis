// @flow
import type { EntityType, GrantedAuthoritySid, State } from './utils/flow.types'

export const SET_SELECTED_SID = '__SET_SELECTED_SID__'
export const SET_SELECTED_ENTITY_TYPE = '__SET_SELECTED_ENTITY_TYPE__'
export const SET_FILTER = '__SET_FILTER__'
export const SET_ENTITY_TYPES = '__SET_ENTITY_TYPES__'

export default {
  [SET_SELECTED_SID] (state: State, sid: GrantedAuthoritySid) {
    state.selectedSid = sid
  },
  [SET_SELECTED_ENTITY_TYPE] (state: State, selectedEntityTypeId: string) {
    state.selectedEntityTypeId = selectedEntityTypeId
  },
  [SET_FILTER] (state: State, filter: string) {
    state.filter = filter
  },
  [SET_ENTITY_TYPES] (state: State, entityTypes: Array<EntityType>) {
    state.entityTypes = entityTypes
  }
}
