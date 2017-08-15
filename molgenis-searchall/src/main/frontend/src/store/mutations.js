export const SET_MESSAGE = '__SET_MESSAGE__'

export const SET_SEARCHTERM = 'SET_SEARCHTERM'
export const SET_RESULTS = 'SET_RESULTS'
export const SET_ERRORS = 'SET_ERRORS'

export default {
  [SET_SEARCHTERM] (state, searchterm) {
    console.log('term: ' + searchterm)
    state.query = searchterm
  },
  [SET_RESULTS] (state, result) {
    state.result = result
  },
  [SET_ERRORS] (state, message) {
    state.error = message
  }
}
