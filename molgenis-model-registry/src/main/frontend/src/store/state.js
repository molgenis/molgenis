// @flow
import type { State } from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  error: undefined,
  umlData: {},
  molgenisPackage: {
    name: 'sys_md'
  }
}

export default state
