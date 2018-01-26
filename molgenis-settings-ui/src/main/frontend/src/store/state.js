// @flow

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state = {
  error: undefined,
  message: undefined,
  token: undefined,
  formFields: [],
  formData: null,
  settings: [],
  selectedSetting: 'sys_set_app'
}

export default state
