// @flow
import type {Package, State, Entity} from '../flow.types'
export const SET_PACKAGES = '__SET_PACKAGES__'
export const SET_ENTITIES = '__SET_ENTITIES__'
export const SET_PATH = '__SET_PATH__'
export const RESET_PATH = '__RESET_PATH__'
export const SET_QUERY = '__SET_QUERY__'
export const SET_ERROR = '__SET_ERROR__'

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
