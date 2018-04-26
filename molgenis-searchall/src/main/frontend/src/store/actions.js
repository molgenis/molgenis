// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { SET_ERRORS, SET_RESULTS } from '../store/mutations'

export const SEARCH_ALL = '__SEARCH_ALL__'

export default {
  [SEARCH_ALL] ({commit}: { commit: Function }, query: string) {
    if (query) {
      const uri = '/api/searchall/search?term=' + encodeURIComponent(query)
      api.get(uri).then(response => {
        commit(SET_RESULTS, response)
      }, error => {
        commit(SET_ERRORS, error)
      })
    }
  }
}
