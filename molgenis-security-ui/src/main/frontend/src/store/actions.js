// @flow
import api from '@molgenis/molgenis-api-client'

const SECURITY_API_ROUTE = 'api/plugin/security'
const SECURITY_API_VERSION = ''

const actions = {
  'fetchGroups' ({commit}: { commit: Function }) {
    return api.get(SECURITY_API_ROUTE + SECURITY_API_VERSION + '/group').then(response => {
      commit('setGroups', response)
    }, error => {
      commit('setToast', { type: 'danger', message: 'Error when calling backend' })
    })
  }
}
export default actions
