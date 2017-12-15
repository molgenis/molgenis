// @flow
import type { Result, State } from './state'

export const SET_RESULTS = '__SET_RESULTS__'
export const SET_ERRORS = '__SET_ERRORS__'
export const SET_LOADING = '__SET_LOADING__'
export const RESET_RESPONSE = '__RESET_RESPONSE__'

export default {
  [SET_RESULTS] (state: State, result: Result) {
    state.result = result
  },
  [SET_ERRORS] (state: State, message: string) {
    state.error = message
  },
  [SET_LOADING] (state: State, loading: boolean) {
    state.loading = loading
  },
  [RESET_RESPONSE] (state: State) {
    state.result = {
      query: '',
      response: null
    }
  }
}
