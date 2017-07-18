// @flow
import type { GrantedAuthoritySid, State } from './utils/flow.types'

export const SET_SELECTED_SID = 'setSelectedSid'

export default {
  [SET_SELECTED_SID] (state: State, sid: GrantedAuthoritySid) {
    state.selectedSid = sid
  }
}
