// @flow
import type { State } from '../flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  error: null,
  token: null,
  query: null,
  path: [],
  items: [],
  selectedItems: [],
  clipboard: {}
}

export default state
