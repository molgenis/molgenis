// @flow

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state = {
  error: undefined,
  message: undefined,
  token: undefined,
  formFields: [],
  formData: {},
  settings: []
}

export default state
