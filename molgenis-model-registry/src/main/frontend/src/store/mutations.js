// @flow
import type { State } from './utils/flow.types'

export const SET_UMLDATA = '__SET_UMLDATA__'
export const SET_ERROR = '__SET_ERROR__'

export default {
  [SET_UMLDATA] (state: State, umlData: any) {
    state.umlData = umlData
  },
  [SET_ERROR] (state: State, errorMessage: string) {
    state.error = errorMessage
  }
}
