// @flow
import type { State } from './utils/flow.types'

export const SET_SELECTED_SID = 'setSelectedSid'

export default {
  [SET_SELECTED_SID] (state: State, sid: any) {
    console.log('setSectedSid', sid)
    state.selectedSid = sid
  }
}
