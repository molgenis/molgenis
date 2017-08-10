export const SET_MESSAGE = '__SET_MESSAGE__'

export const SET_SEARCHTERM = 'SET_SEARCHTERM'

export default {
  [SET_SEARCHTERM] (state, searchterm) {
    state.searchterm = searchterm
  }
}
