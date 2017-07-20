// @flow
import type { State } from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  umlData: {},
  molgenisPackage: INITIAL_STATE.molgenisPackage
}

export default state
