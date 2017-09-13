import api from '@molgenis/molgenis-api-client'
import { ADD_FINISHED_JOB, UPDATE_JOB } from './mutations'

export const IMPORT_FILE = '__IMPORT_FILE__'

const pollJobUrl = (commit, dispatch, jobUrl) => {
  api.get(jobUrl).then(job => {
    commit(UPDATE_JOB, job)
    if (job.status === 'RUNNING' || job.status === 'PENDING') {
      setTimeout(function () {
        pollJobUrl(commit, dispatch, jobUrl)
      }, 1000)
    } else {
      commit(ADD_FINISHED_JOB, job)
    }
  })
}

export default {
  [IMPORT_FILE] ({commit, dispatch}, file) {
    api.postFile('/plugin/one-click-importer/upload', file).then(response => {
      pollJobUrl(commit, dispatch, response)
    })
  }
}
