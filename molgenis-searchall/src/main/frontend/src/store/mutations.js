// @flow
import type { Result, State } from './state'

export const SET_SEARCHTERM = 'SET_SEARCHTERM'
export const SET_RESULTS = 'SET_RESULTS'
export const SET_ERRORS = 'SET_ERRORS'

export default {
  [SET_SEARCHTERM] (state: State, searchterm: string) {
    console.log('term: ' + searchterm)
    state.query = searchterm
  },
  [SET_RESULTS] (state: State, result: Result) {
    state.result = result
  },
  [SET_ERRORS] (state: State, message: string) {
    state.error = message
  }
}
