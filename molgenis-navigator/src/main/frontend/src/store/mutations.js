// @flow
import type { Alert, Clipboard, Folder, Item, Job, State } from '../flow.types'

export const ADD_ALERTS = '__ADD_ALERTS__'
export const REMOVE_ALERT = '__REMOVE_ALERT__'
export const SET_JOBS = '__SET_JOBS__'
export const SET_FOLDER = '__SET_FOLDER__'
export const SET_ITEMS = '__SET_ITEMS__'
export const SET_SELECTED_ITEMS = '__SET_SELECTED_ITEMS__'
export const SET_CLIPBOARD = '__SET_CLIPBOARD__'
export const RESET_CLIPBOARD = '__RESET_CLIPBOARD__'

export default {
  [ADD_ALERTS] (state: State, alerts: Array<Alert>) {
    state.alerts = state.alerts.concat(alerts)
  },
  [REMOVE_ALERT] (state: State, index: number) {
    let alerts = state.alerts.slice()
    alerts.splice(index, 1)
    state.alerts = alerts
  },
  [SET_JOBS] (state: State, jobs: Array<Job>) {
    state.jobs = jobs
  },
  [SET_FOLDER] (state: State, folder: Folder) {
    state.folder = folder
  },
  [SET_ITEMS] (state: State, items: Array<Item>) {
    state.selectedItems = []
    state.items = items
  },
  [SET_SELECTED_ITEMS] (state: State, items: Array<Item>) {
    state.selectedItems = items
  },
  [SET_CLIPBOARD] (state: State, clipboard: Clipboard) {
    state.selectedItems = []
    state.clipboard = clipboard
  },
  [RESET_CLIPBOARD] (state: State) {
    state.clipboard = null
  }
}
