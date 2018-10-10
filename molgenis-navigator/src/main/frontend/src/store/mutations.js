// @flow
import type { Clipboard, Item, Package, State } from '../flow.types'

export const SET_ERROR = '__SET_ERROR__'
export const SET_PATH = '__SET_PATH__'
export const SET_ITEMS = '__SET_ITEMS__'
export const SET_SELECTED_ITEMS = '__SET_SELECTED_ITEMS__'
export const SET_CLIPBOARD = '__SET_CLIPBOARD__'
export const RESET_CLIPBOARD = '__RESET_CLIPBOARD__'

export default {
  [SET_ERROR] (state: State, error: any) {
    state.error = error
  },
  [SET_PATH] (state: State, packages: Array<Package>) {
    state.path = packages
    state.route.query.q = undefined
  },
  [SET_ITEMS] (state: State, items: Array<Item>) {
    state.selectedItems = []
    state.items = items
  },
  [SET_SELECTED_ITEMS] (state: State, items: Array<Item>) {
    state.selectedItems = items
  },
  [SET_CLIPBOARD] (state: State, clipboard: Clipboard) {
    state.clipboard = clipboard
  },
  [RESET_CLIPBOARD] (state: State) {
    state.clipboard = {}
  }
}
