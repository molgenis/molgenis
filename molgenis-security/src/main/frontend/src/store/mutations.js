// @flow
import type { ACL, GrantedAuthoritySid, State } from './utils/flow.types'

export const TOGGLE_SID = '__TOGGLE_SID__'
export const SET_SELECTED_ENTITY_TYPE = '__SET_SELECTED_ENTITY_TYPE__'
export const SET_FILTER = '__SET_FILTER__'
export const SET_ACLS = '__SET_ACLS__'
export const SET_ENTITY_TYPES = '__SET_ENTITY_TYPES__'
export const SET_SIDS = '__SET_SIDS__'

export default {
  [SET_ACLS] (state: State, acls: Array<ACL>) {
    state.acls = acls
  },
  [TOGGLE_SID] (state: State, sid: GrantedAuthoritySid) {
    state.selectedSids = [sid]
    // if (state.selectedSids.includes(sid)) {
    //   state.selectedSids.splice(state.selectedSids.indexOf(sid), 1)
    // } else {
    //   state.selectedSids.push(sid)
    // }
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
