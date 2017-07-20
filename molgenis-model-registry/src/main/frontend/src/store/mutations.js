// @flow
import type { State } from './utils/flow.types'

export const SET_UMLDATA = '__SET_UMLDATA__'

export default {
  [SET_UMLDATA] (state: State, umlData: any) {
    state.umlData = umlData
  }
}
