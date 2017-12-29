// @flow

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state = {
  error: undefined,
  token: undefined,
  rawSettings: [],
  settings: []
}

export default state
