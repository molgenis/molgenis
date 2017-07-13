// @flow
import type { State } from './utils/flow.types'

export const SET_MESSAGE = '__SET_MESSAGE__'
export const SET_UMLDATA = '__SET_UMLDATA__'

export default {
  [SET_MESSAGE] (state: State, message: string) {
    state.message = message
  },
  [SET_UMLDATA] (state: State, umlData: any) {
    state.umlData = umlData
  }
}
