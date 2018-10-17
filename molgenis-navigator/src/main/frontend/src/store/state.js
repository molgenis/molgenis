// @flow
import type { State } from '../flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

// $FlowFixMe: suppressing this error until we can add route
const state: State = {
  alerts: [],
  token: null,
  query: null,
  package: null,
  items: [],
  selectedItems: [],
  clipboard: null,
  jobs: [{type: 'download', id: 'aaaacztxsh4nqabegilmlgaaae', status: 'pending'}] // TODO remove
}

export default state
