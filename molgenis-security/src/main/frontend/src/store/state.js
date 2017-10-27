// @flow
import type {State} from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  members: [],
  member: undefined,
  roles: [],
  users: [],
  groups: [],
  filter: undefined,
  context: {id: 'BBMRI-NL', label: 'BBMRI-NL'},
  sort: 'ascending'
}

export default state
