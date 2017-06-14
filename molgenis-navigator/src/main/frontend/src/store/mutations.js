// @flow
import type {Package, State, Entity} from './state'
export const SET_PACKAGES = 'SET_PACKAGES'
export const SET_ENTITIES = 'SET_ENTITIES'
export const SET_PATH = 'APPEND_PATH'
export const RESET_PATH = 'RESET_PATH'
export const SET_QUERY = 'SET_QUERY'
export const SET_ERROR = 'SET_ERROR'

export default {
  [SET_PACKAGES] (state: State, packages: Array<Package>) {
    state.packages = packages
  },
  [SET_PATH] (state: State, packages: Array<Package>) {
    state.path = packages
  },
  [RESET_PATH] (state: State) {
    state.path.splice(0, state.path.length)
  },
  [SET_ENTITIES] (state: State, entities: Array<Entity>) {
    state.entities = entities
  },
  [SET_QUERY] (state: State, query: string) {
    state.query = query
  },
  [SET_ERROR] (state: State, error: any) {
    state.error = error
  }
}
