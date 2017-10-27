// @flow
import type {State} from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  members: [],
  member: undefined,
  roles: [],
  usersGroups: [],
  query: undefined,
  sort: 'ascending'
}

export default state
