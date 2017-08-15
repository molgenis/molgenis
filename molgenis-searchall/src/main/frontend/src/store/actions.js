import { get } from '@molgenis/molgenis-api-client'
import { SET_RESULTS, SET_ERRORS } from '../store/mutations'

export const SEARCH_ALL = 'SEARCH_ALL'

function queryPackages (query) {
  return new Promise((resolve, reject) => {
    const uri = '/api/searchall/search?term=' + query
    get(uri).then((response) => {
      resolve(response)
    }).catch((error) => {
      reject(error)
    })
  })
}

export default {
  [SEARCH_ALL] ({commit}, query) {
    return new Promise((resolve, reject) => {
      if (!query) {
        commit(SET_ERRORS, 'No searchterm')
      } else {
        queryPackages(query).then(results => {
          commit(SET_RESULTS, results)
          resolve()
        }, errorMessage => {
          commit(SET_ERRORS, errorMessage)
          reject()
        })
      }
    })
  }
}
