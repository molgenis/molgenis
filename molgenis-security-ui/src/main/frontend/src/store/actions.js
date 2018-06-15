// @flow
import api from '@molgenis/molgenis-api-client'

const SECURITY_API_ROUTE = 'api/plugin/'
const SECURITY_API_VERSION = ''

const actions = {
  'FETCH_GROUPS' (commit: Function) {
    return api.get(SECURITY_API_ROUTE + SECURITY_API_VERSION + 'group').then(response => {
      // todo translate
      commit('SET_GROUPS', response)
    }, error => {
      console.error('error during api call get groups')
      console.error(error)
    })
  }
}
export default actions
