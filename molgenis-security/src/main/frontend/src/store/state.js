// @flow
import type { State } from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  groupMemberships: [],
  users: {},
  groups: {},
  filter: undefined,
  sort: 'ascending',
  loading: 0,
  alerts: []
}

export default state
