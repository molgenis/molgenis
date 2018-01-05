// @flow

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state = {
  error: undefined,
  token: undefined,
  formFields: [],
  formData: {}
}

export default state
