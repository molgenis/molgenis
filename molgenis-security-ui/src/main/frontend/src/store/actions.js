// @flow
import type { CreateGroupCommand } from '../flow.type'
import api from '@molgenis/molgenis-api-client'

const SECURITY_API_ROUTE = '/api/plugin/security'
const SECURITY_API_VERSION = ''
const GROUP_ENDPOINT = SECURITY_API_ROUTE + SECURITY_API_VERSION + '/group'

const actions = {
  'fetchGroups' ({commit}: { commit: Function }) {
    return api.get(GROUP_ENDPOINT).then(response => {
      commit('setGroups', response)
    }, () => {
      commit('setToast', { type: 'danger', message: 'Error when calling backend' })
    })
  },

  'createGroup' ({commit}: { commit: Function }, createGroupCmd: CreateGroupCommand) {
    const payload = {
      body: JSON.stringify({
        name: createGroupCmd.groupIdentifier,
        label: createGroupCmd.name
      })
    }
    return new Promise((resolve, reject) => {
      api.post(GROUP_ENDPOINT, payload).then(response => {
        commit('setGroups', response)
        commit('setToast', { type: 'success', message: 'Group created: [ ' + createGroupCmd.name + ' ]' })
        resolve()
      }, (error) => {
        commit('setToast', { type: 'danger', message: 'Unable to create group; ' + error })
        reject(error)
      })
    })
  }
}
export default actions
