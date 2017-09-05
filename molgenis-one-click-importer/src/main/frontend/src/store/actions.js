import api from '@molgenis/molgenis-api-client'
import { ADD_FINISHED_JOB, UPDATE_JOB } from './mutations'

export const IMPORT_FILE = '__IMPORT_FILE__'
export const POLL_JOB_URL = '__POLL_JOB_URL__'

export default {
  [IMPORT_FILE] ({dispatch}, file) {
    api.postFile('/plugin/one-click-importer/upload', file).then(response => {
      dispatch(POLL_JOB_URL, response)
    })
  },
  [POLL_JOB_URL] ({dispatch, commit}, jobUrl) {
    api.get(jobUrl).then(job => {
      commit(UPDATE_JOB, job)
      if (job.status === 'RUNNING' || job.status === 'PENDING') {
        setTimeout(function () {
          dispatch(POLL_JOB_URL, jobUrl)
        }, 1000)
      } else {
        commit(ADD_FINISHED_JOB, job)
      }
    })
  }
}
