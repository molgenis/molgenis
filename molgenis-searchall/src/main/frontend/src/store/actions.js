import { get } from '@molgenis/molgenis-api-client'

export const SEARCH_ALL = 'SEARCH_ALL'

function queryPackages (query) {
  return new Promise((resolve, reject) => {
    const uri = '/api/v2/seachall?term=' + query
    get(uri).then((response) => {
      resolve(response.items)
    }).catch((error) => {
      reject(error)
    })
  })
}

export default {
  [SEARCH_ALL] ({commit}, query) {
    return new Promise((resolve, reject) => {
      if (!query) {
        // FIXME
      } else {
        queryPackages().then(results => {
          // commit(SET_RESULTS, results)
          resolve()
        }, errorMessage => {
          // commit(SET_ERROR, errorMessage)
          reject()
        })
      }
    })
  }
}
