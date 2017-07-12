// @flow
import type { State } from './utils/flow.types'

export const SET_MESSAGE = '__SET_MESSAGE__'

export const SET_RAWDATA = '__SET_RAWDATA__'

export default {
  [SET_MESSAGE] (state: State, message: string) {
    state.message = message
  },
  [SET_RAWDATA] (state: State, rawData: any) {
    state.rawData = rawData
  }

}
