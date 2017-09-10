import fetch from 'isomorphic-fetch'
import { ADD_FINISHED_JOB, UPDATE_JOB } from './mutations'

export const IMPORT_FILE = '__IMPORT_FILE__'
export const POLL_JOB_URL = '__POLL_JOB_URL__'

export default {
  [IMPORT_FILE] ({dispatch}, file) {
    const formData = new FormData()
    formData.append('file', file)

    const options = {
      body: formData,
      method: 'POST',
      credentials: 'same-origin'
    }

    fetch('/plugin/one-click-importer/upload', options).then(response => {
      response.text().then(jobUrl => {
        dispatch(POLL_JOB_URL, jobUrl)
      })
    })
  },
  [POLL_JOB_URL] ({dispatch, commit}, jobUrl) {
    const options = {
      credentials: 'same-origin'
    }

    fetch(jobUrl, options).then(response => {
      response.json().then(job => {
        commit(UPDATE_JOB, job)
        if (job.status === 'RUNNING' || job.status === 'PENDING') {
          setTimeout(function () {
            dispatch(POLL_JOB_URL, jobUrl)
          }, 1000)
        } else {
          commit(ADD_FINISHED_JOB, job)
        }
      })
    })
  }
}
