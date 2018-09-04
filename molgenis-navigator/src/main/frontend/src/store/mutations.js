// @flow
import type { Entity, Package, State } from '../flow.types'

export const SET_PACKAGES = '__SET_PACKAGES__'
export const SET_ENTITIES = '__SET_ENTITIES__'
export const SET_PATH = '__SET_PATH__'
export const RESET_PATH = '__RESET_PATH__'
export const SET_QUERY = '__SET_QUERY__'
export const SET_ERROR = '__SET_ERROR__'
export const SET_SELECTED_PACKAGES = '__SET_SELECTED_PACKAGES__'
export const SET_SELECTED_ENTITY_TYPES = '__SET_SELECTED_ENTITY_TYPES__'

export default {
  [SET_PACKAGES] (state: State, packages: Array<Package>) {
    state.selectedPackageIds = []
    state.packages = packages
  },
  [SET_PATH] (state: State, packages: Array<Package>) {
    state.path = packages
  },
  [RESET_PATH] (state: State) {
    state.path.splice(0, state.path.length)
  },
  [SET_ENTITIES] (state: State, entities: Array<Entity>) {
    state.selectedEntityTypeIds = []
    state.entities = entities
  },
  [SET_QUERY] (state: State, query: string) {
    state.query = query
  },
  [SET_ERROR] (state: State, error: any) {
    state.error = error
  },
  [SET_SELECTED_PACKAGES] (state: State, packageIds: Array<string>) {
    state.selectedPackageIds = packageIds
  },
  [SET_SELECTED_ENTITY_TYPES] (state: State, entityTypeIds: Array<string>) {
    state.selectedEntityTypeIds = entityTypeIds
  }
}
