// @flow

export const SET_RAW_SETTINGS = '__SET_RAW_SETTINGS__'
export const SET_SETTINGS = '__SET_SETTINGS__'
export const SET_ERROR = '__SET_ERROR__'

export default {
  [SET_RAW_SETTINGS] (state, response) {
    state.rawSettings = response
  },
  [SET_SETTINGS] (state, settings) {
    state.settings = settings
  },
  [SET_ERROR] (state, error) {
    state.error = error
  }
}
