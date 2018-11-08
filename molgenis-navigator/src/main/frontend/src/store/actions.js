// @flow
import type { Folder, Item, Job, State } from '../flow.types'
import {
  fetchJob,
  getItemsByFolderId,
  getItemsByQuery,
  createItem,
  copyItems,
  deleteItems,
  moveItems,
  updateItem,
  downloadItems
} from '../utils/api.js'
import {
  ADD_ALERTS,
  RESET_CLIPBOARD,
  SET_FOLDER,
  SET_ITEMS,
  ADD_JOB,
  UPDATE_JOB,
  SET_SELECTED_ITEMS
} from './mutations'

export const FETCH_ITEMS = '__FETCH_ITEMS__'
export const FETCH_ITEMS_BY_QUERY = '__FETCH_ITEMS_BY_QUERY__'
export const FETCH_ITEMS_BY_FOLDER = '__FETCH_ITEMS_BY_FOLDER__'
export const SELECT_ITEM = '__SELECT_ITEM__'
export const DESELECT_ITEM = '__DESELECT_ITEM__'
export const SELECT_ALL_ITEMS = '__SELECT_ALL_ITEMS__'
export const DESELECT_ALL_ITEMS = '__DESELECT_ALL_ITEMS__'
export const DELETE_SELECTED_ITEMS = '__DELETE_SELECTED_ITEMS__'
export const CREATE_ITEM = '__CREATE_ITEM__'
export const UPDATE_ITEM = '__UPDATE_ITEM__'
export const MOVE_CLIPBOARD_ITEMS = '__MOVE_CLIPBOARD_ITEMS__'
export const COPY_CLIPBOARD_ITEMS = '__COPY_CLIPBOARD_ITEMS__'
export const POLL_JOB = '__POLL_JOB__'
export const DOWNLOAD_SELECTED_ITEMS = '__DOWNLOAD_SELECTED_ITEMS__'

function pollJob (commit: Function, job: Job) {
  fetchJob(job).then(updatedJob => {
    // TODO progress change
    if (job.status !== updatedJob.status) {
      commit(UPDATE_JOB, updatedJob)
    }
    switch (updatedJob.status) {
      case 'pending':
      case 'running':
        setTimeout(() => pollJob(commit, updatedJob), 500)
        break
      case 'success':
      case 'failed':
      case 'canceled':
        // TODO discuss with TdB: what to do when job is done?
        // 1) user is currently in target folder of job
        //    --> dispatch FETCH_ITEMS_BY_FOLDER
        //        BUG: current selection is removed
        // 2) user is somewhere else (search context or normal context with other folder)
        //    --> do nothing
        console.log('job finished with status ' + updatedJob.status)
        break
    }
  })
}

export default {
  [FETCH_ITEMS] ({state, dispatch}: { state: State, dispatch: Function }) {
    if (state.query) {
      dispatch(FETCH_ITEMS_BY_QUERY, state.query)
    } else {
      dispatch(FETCH_ITEMS_BY_FOLDER, state.route.params.folderId)
    }
  },
  // TODO rename to FETCH_STATE_BY_QUERY
  [FETCH_ITEMS_BY_QUERY] ({commit}: { commit: Function }, query: string) {
    getItemsByQuery(query).then(data => {
      commit(SET_ITEMS, data.resources)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  // TODO rename to FETCH_STATE_BY_FOLDER
  [FETCH_ITEMS_BY_FOLDER] ({commit, dispatch}: { commit: Function, dispatch: Function }, folderId: ?string) {
    getItemsByFolderId(folderId).then(data => {
      // if folder changed, then remove selection
      // if folder same, then update selection

      commit(SET_FOLDER, data.folder)
      commit(SET_ITEMS, data.resources)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [SELECT_ALL_ITEMS] ({commit, state}: { commit: Function, state: State }) {
    commit(SET_SELECTED_ITEMS, state.items.slice())
  },
  [DESELECT_ALL_ITEMS] ({commit}: { commit: Function }) {
    commit(SET_SELECTED_ITEMS, [])
  },
  [SELECT_ITEM] ({commit, state}: { commit: Function, state: State }, item: Item) {
    commit(SET_SELECTED_ITEMS, state.selectedItems.concat(item))
  },
  [DESELECT_ITEM] ({commit, state}: { commit: Function, state: State }, item: Item) {
    commit(SET_SELECTED_ITEMS, state.selectedItems.filter(selectedItem => !(selectedItem.type === item.type && selectedItem.id === item.id)))
  },
  [DELETE_SELECTED_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function }) {
    if (state.selectedItems.length > 0) {
      deleteItems(state.selectedItems).then(() => {
        dispatch(FETCH_ITEMS)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  },
  [CREATE_ITEM] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    item: Item) {
    createItem(item, state.folder).then(() => {
      dispatch(FETCH_ITEMS)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [UPDATE_ITEM] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    updatedItem: Item) {
    const item = state.items.find(
      item => item.type === updatedItem.type && item.id === updatedItem.id)
    if (item !== undefined) {
      updateItem(item, updatedItem).then(() => {
        dispatch(FETCH_ITEMS)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    } else {
      throw new Error(
        'UPDATE_ITEM requires updated item to refer to existing item')
    }
  },
  [MOVE_CLIPBOARD_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    folder: Folder) {
    if (state.clipboard && state.clipboard.items.length > 0) {
      moveItems(state.clipboard.items, folder).then(() => {
        commit(RESET_CLIPBOARD)
        dispatch(FETCH_ITEMS)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  },
  [COPY_CLIPBOARD_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    folder: Folder) {
    if (state.clipboard && state.clipboard.items.length > 0) {
      copyItems(state.clipboard.items, folder).then(job => {
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
    pollJob(commit, job)
  },
  [DOWNLOAD_SELECTED_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function }) {
    if (state.selectedItems.length > 0) {
      downloadItems(state.selectedItems).then(job => {
        commit(SET_SELECTED_ITEMS, [])
        commit(ADD_JOB, job)
        dispatch(POLL_JOB, job)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  }
}
