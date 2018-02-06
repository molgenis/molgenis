/* Form mutations */
export const SET_FORM_FIELDS = '__SET_FORM_FIELDS__'
export const SET_FORM_DATA = '__SET_FORM_DATA__'

/* Application alert mutations */
export const SET_ALERT = '__SET_ALERT__'

export default {
  [SET_FORM_FIELDS] (state, formFields) {
    state.formFields = formFields
  },
  [SET_FORM_DATA] (state, formData) {
    state.formData = formData
  },
  [SET_ALERT] (state, alert) {
    state.alert = alert
  }
}
