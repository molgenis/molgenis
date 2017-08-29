// @flow
// $FlowFixMe
import { get } from '@molgenis/molgenis-api-client'
import { SET_ERRORS, SET_RESULTS } from '../store/mutations'

export const SEARCH_ALL = 'SEARCH_ALL'

function searchAllData (query: string) {
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
  [SEARCH_ALL] ({commit}: { commit: Function }, query: string) {
    return new Promise((resolve, reject) => {
      if (!query) {
        commit(SET_ERRORS, 'No searchterm')
      } else {
        searchAllData(query).then(results => {
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
