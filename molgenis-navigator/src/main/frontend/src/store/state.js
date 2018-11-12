// @flow
import type { State } from '../flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

// $FlowFixMe: suppressing this error until we can add route
const state: State = {
  alerts: [],
  token: null,
  query: null,
  folder: null,
  resources: [],
  selectedResources: [],
  clipboard: null,
  jobs: []
}

export default state
