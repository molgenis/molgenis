// @flow

export const SET_FORM_FIELDS = '__SET_FORM_FIELDS__'
export const SET_ERROR = '__SET_ERROR__'

export default {
  [SET_FORM_FIELDS] (state, formFields) {
    state.formFields = formFields
  },
  [SET_ERROR] (state, error) {
    state.error = error
  }
}
