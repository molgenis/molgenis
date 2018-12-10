// @flow
import type { Alert, Clipboard, Folder, Resource, Job, State } from '../flow.types'

export const ADD_ALERTS = '__ADD_ALERTS__'
export const REMOVE_ALERT = '__REMOVE_ALERT__'
export const ADD_JOB = '__ADD_JOB__'
export const UPDATE_JOB = '__UPDATE_JOB__'
export const REMOVE_JOB = '__REMOVE_JOB__'
export const SET_FOLDER = '__SET_FOLDER__'
export const SET_RESOURCES = '__SET_RESOURCES__'
export const SET_SELECTED_RESOURCES = '__SET_SELECTED_RESOURCES__'
export const SET_SHOW_HIDDEN_RESOURCES = '__SET_SHOW_HIDDEN_RESOURCES__'
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
  [REMOVE_JOB] (state: State, job: Job) {
    state.jobs = state.jobs.filter(existingJob => !(existingJob.type === job.type && existingJob.id === job.id))
  },
  [SET_FOLDER] (state: State, folder: Folder) {
    state.folder = folder
  },
  [SET_RESOURCES] (state: State, resources: Array<Resource>) {
    state.selectedResources = []
    state.resources = resources
  },
  [SET_SELECTED_RESOURCES] (state: State, resources: Array<Resource>) {
    state.selectedResources = resources
  },
  [SET_SHOW_HIDDEN_RESOURCES] (state: State, showHiddenResources: boolean) {
    state.showHiddenResources = showHiddenResources
  },
  [SET_CLIPBOARD] (state: State, clipboard: Clipboard) {
    state.selectedResources = []
    state.clipboard = clipboard
  },
  [RESET_CLIPBOARD] (state: State) {
    state.clipboard = null
  }
}
