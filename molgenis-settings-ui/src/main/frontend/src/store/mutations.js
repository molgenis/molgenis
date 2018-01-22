// @flow

export const SET_FORM_FIELDS = '__SET_FORM_FIELDS__'
export const SET_FORM_DATA = '__SET_FORM_DATA__'
export const SET_SETTINGS = '__SET_SETTINGS__'
export const SET_ERROR = '__SET_ERROR__'
export const SET_MESSAGE = '__SET_MESSAGE__'

export default {
  [SET_FORM_FIELDS] (state, formFields) {
    state.formFields = formFields
  },
  [SET_FORM_DATA] (state, formData) {
    state.formData = formData
  },
  [SET_SETTINGS] (state, settings) {
    state.settings = settings
  },
  [SET_ERROR] (state, error) {
    state.error = error
  },
  [SET_MESSAGE] (state, message) {
    state.message = message
  }
}
