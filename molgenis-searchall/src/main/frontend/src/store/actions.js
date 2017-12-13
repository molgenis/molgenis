// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { SET_ERRORS, SET_LOADING, SET_RESULTS } from '../store/mutations'

export const SEARCH_ALL = '__SEARCH_ALL__'

export default {
  [SEARCH_ALL] ({commit}: { commit: Function }, query: string) {
    commit(SET_LOADING, true)

    const uri = '/api/searchall/search?term=' + encodeURIComponent(query)

    api.get(uri).then(response => {
      commit(SET_RESULTS, {query: query, response: response})
      commit(SET_LOADING, false)
    }, error => {
      commit(SET_ERRORS, error)
      commit(SET_LOADING, false)
    })
  }
}
