// @flow
import type { Folder, Resource, Job, State } from '../flow.types'
import {
  fetchJob,
  getResourcesByFolderId,
  getResourcesByQuery,
  createResource,
  copyResources,
  deleteResources,
  moveResources,
  updateResource,
  downloadResources
} from '../utils/api.js'
import {
  ADD_ALERTS,
  RESET_CLIPBOARD,
  SET_FOLDER,
  SET_RESOURCES,
  ADD_JOB,
  UPDATE_JOB,
  SET_SELECTED_RESOURCES
} from './mutations'

export const FETCH_RESOURCES = '__FETCH_RESOURCES__'
export const FETCH_RESOURCES_BY_QUERY = '__FETCH_RESOURCES_BY_QUERY__'
export const FETCH_RESOURCES_BY_FOLDER = '__FETCH_RESOURCES_BY_FOLDER__'
export const SELECT_RESOURCE = '__SELECT_RESOURCE__'
export const DESELECT_RESOURCE = '__DESELECT_RESOURCE__'
export const SELECT_ALL_RESOURCES = '__SELECT_ALL_RESOURCES__'
export const DESELECT_ALL_RESOURCES = '__DESELECT_ALL_RESOURCES__'
export const DELETE_SELECTED_RESOURCES = '__DELETE_SELECTED_RESOURCES__'
export const CREATE_RESOURCE = '__CREATE_RESOURCE__'
export const UPDATE_RESOURCE = '__UPDATE_RESOURCE__'
export const MOVE_CLIPBOARD_RESOURCES = '__MOVE_CLIPBOARD_RESOURCES__'
export const COPY_CLIPBOARD_RESOURCES = '__COPY_CLIPBOARD_RESOURCES__'
export const POLL_JOB = '__POLL_JOB__'
export const DOWNLOAD_SELECTED_RESOURCES = '__DOWNLOAD_SELECTED_RESOURCES__'

function finishJob (commit: Function, dispatch: Function, state: State,
  job: Job) {
  switch (job.type) {
    case 'COPY':
    case 'DELETE':
      if (job.status === 'SUCCESS') {
        dispatch(FETCH_RESOURCES)
      }
      break
    case 'DOWNLOAD':
      break
    default:
      throw new Error('unexpected job type \'' + job.type + '\'')
  }
}

function pollJob (commit: Function, dispatch: Function, state: State,
  job: Job) {
  fetchJob(job).then(updatedJob => {
    commit(UPDATE_JOB, updatedJob)
    switch (updatedJob.status) {
      case 'RUNNING':
        setTimeout(() => pollJob(commit, dispatch, state, updatedJob), 500)
        break
      case 'SUCCESS':
      case 'FAILED':
        finishJob(commit, dispatch, state, updatedJob)
        break
    }
  }).catch(error => {
    commit(ADD_ALERTS, error.alerts)
  })
}

export default {
  [FETCH_RESOURCES] ({state, dispatch}: { state: State, dispatch: Function }) {
    if (state.query) {
      dispatch(FETCH_RESOURCES_BY_QUERY, state.query)
    } else {
      dispatch(FETCH_RESOURCES_BY_FOLDER, state.route.params.folderId)
    }
  },
  [FETCH_RESOURCES_BY_QUERY] ({commit}: { commit: Function }, query: string) {
    getResourcesByQuery(query).then(data => {
      commit(SET_RESOURCES, data.resources)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [FETCH_RESOURCES_BY_FOLDER] ({commit, dispatch}: { commit: Function, dispatch: Function }, folderId: ?string) {
    getResourcesByFolderId(folderId).then(data => {
      // if folder changed, then remove selection
      // if folder same, then update selection

      commit(SET_FOLDER, data.folder)
      commit(SET_RESOURCES, data.resources)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [SELECT_ALL_RESOURCES] ({commit, state}: { commit: Function, state: State }) {
    commit(SET_SELECTED_RESOURCES, state.resources.slice())
  },
  [DESELECT_ALL_RESOURCES] ({commit}: { commit: Function }) {
    commit(SET_SELECTED_RESOURCES, [])
  },
  [SELECT_RESOURCE] ({commit, state}: { commit: Function, state: State }, resource: Resource) {
    commit(SET_SELECTED_RESOURCES, state.selectedResources.concat(resource))
  },
  [DESELECT_RESOURCE] ({commit, state}: { commit: Function, state: State }, resource: Resource) {
    commit(SET_SELECTED_RESOURCES, state.selectedResources.filter(selectedResource => !(selectedResource.type === resource.type && selectedResource.id === resource.id)))
  },
  [DELETE_SELECTED_RESOURCES] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function }) {
    if (state.selectedResources.length > 0) {
      deleteResources(state.selectedResources).then(job => {
        commit(SET_SELECTED_RESOURCES, [])
        commit(ADD_JOB, job)
        setTimeout(() => dispatch(POLL_JOB, job), 500)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  },
  [CREATE_RESOURCE] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    resource: Resource) {
    createResource(resource, state.folder).then(() => {
      dispatch(FETCH_RESOURCES)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [UPDATE_RESOURCE] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    updatedResource: Resource) {
    const resource = state.resources.find(
      resource => resource.type === updatedResource.type && resource.id === updatedResource.id)
    if (resource !== undefined) {
      updateResource(resource, updatedResource).then(() => {
        dispatch(FETCH_RESOURCES)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    } else {
      throw new Error(
        'UPDATE_RESOURCE requires updated resource to refer to existing resource')
    }
  },
  [MOVE_CLIPBOARD_RESOURCES] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    folder: ?Folder) {
    if (state.clipboard && state.clipboard.resources.length > 0) {
      moveResources(state.clipboard.resources, folder).then(() => {
        commit(RESET_CLIPBOARD)
        dispatch(FETCH_RESOURCES)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  },
  [COPY_CLIPBOARD_RESOURCES] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    folder: ?Folder) {
    if (state.clipboard && state.clipboard.resources.length > 0) {
      copyResources(state.clipboard.resources, folder).then(job => {
        commit(RESET_CLIPBOARD)
        commit(ADD_JOB, job)
        dispatch(POLL_JOB, job)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  },
  [POLL_JOB] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    job: Job) {
    pollJob(commit, dispatch, state, job)
  },
  [DOWNLOAD_SELECTED_RESOURCES] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function }) {
    if (state.selectedResources.length > 0) {
      downloadResources(state.selectedResources).then(job => {
        commit(SET_SELECTED_RESOURCES, [])
        commit(ADD_JOB, job)
        setTimeout(() => dispatch(POLL_JOB, job), 500)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  }
}
