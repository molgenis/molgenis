// @flow
import type { Alert, Clipboard, Folder, Item, Job, State } from '../flow.types'

export const ADD_ALERTS = '__ADD_ALERTS__'
export const REMOVE_ALERT = '__REMOVE_ALERT__'
export const ADD_JOB = '__ADD_JOB__'
export const UPDATE_JOB = '__UPDATE_JOB__'
export const SET_FOLDER = '__SET_FOLDER__'
export const SET_ITEMS = '__SET_ITEMS__'
export const SET_SELECTED_ITEMS = '__SET_SELECTED_ITEMS__'
export const SET_SHOW_HIDDEN_ITEMS = '__SET_SHOW_HIDDEN_ITEMS__'
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
  [ADD_JOB] (state: State, job: Job) {
    state.jobs = state.jobs.concat([job])
  },
  [UPDATE_JOB] (state: State, job: Job) {
    state.jobs = state.jobs.map(
      existingJob => existingJob.type === job.type && existingJob.id === job.id
        ? job : existingJob)
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
  [SET_SHOW_HIDDEN_ITEMS] (state: State, showHiddenItems: boolean) {
    state.showHiddenItems = showHiddenItems
  },
  [SET_CLIPBOARD] (state: State, clipboard: Clipboard) {
    state.selectedItems = []
    state.clipboard = clipboard
  },
  [RESET_CLIPBOARD] (state: State) {
    state.clipboard = null
  }
}
