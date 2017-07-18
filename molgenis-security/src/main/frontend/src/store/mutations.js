// @flow
import type { GrantedAuthoritySid, State } from './utils/flow.types'

export const TOGGLE_SID = '__TOGGLE_SID__'
export const SET_SELECTED_ENTITY_TYPE = '__SET_SELECTED_ENTITY_TYPE__'
export const SET_FILTER = '__SET_FILTER__'

export default {
  [TOGGLE_SID] (state: State, sid: GrantedAuthoritySid) {
    if (state.selectedSids.includes(sid)) {
      state.selectedSids.splice(state.selectedSids.indexOf(sid), 1)
    } else {
      state.selectedSids.push(sid)
    }
  },
  [SET_SELECTED_ENTITY_TYPE] (state: State, selectedEntityTypeId: string) {
    state.selectedEntityTypeId = selectedEntityTypeId
  },
  [SET_FILTER] (state: State, filter: string) {
    state.filter = filter
  }
}
