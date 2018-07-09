// @flow
import type {CreateGroupCommand, GroupMember} from '../flow.type'
import api from '@molgenis/molgenis-api-client'
import asyncUtilService from '../service/asyncUtilService'

const SECURITY_API_ROUTE = '/api/plugin/security'
const SECURITY_API_VERSION = ''
const GROUP_ENDPOINT = SECURITY_API_ROUTE + SECURITY_API_VERSION + '/group'
const TEMP_USER_ENDPOINT = SECURITY_API_ROUTE + SECURITY_API_VERSION + '/user'

const toGroupMember = (response): GroupMember => {
  return {
    userId: response.user.id,
    username: response.user.username,
    roleName: response.role.roleName,
    roleLabel: response.role.roleLabel
  }
}

const handleSuccess = (commit: Function, message: string) => {
  commit('setToast', {type: 'success', message})
  asyncUtilService.callAfter(() => {
    commit('clearToast')
  }, 3000)
}

const actions = {
  'fetchGroups' ({commit}: { commit: Function }) {
    return api.get(GROUP_ENDPOINT).then(response => {
      commit('setGroups', response)
    }, () => {
      commit('setToast', { type: 'danger', message: 'Error when calling backend' })
    })
  },

  'fetchGroupRoles' ({commit}: { commit: Function }, groupName: String) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/role'
    return api.get(url).then(response => {
      commit('setGroupRoles', {groupName, groupRoles: response})
    }, () => {
      commit('setToast', { type: 'danger', message: 'Error when calling backend' })
    })
  },

  'tempFetchUsers' ({commit}: { commit: Function }) {
    return api.get(TEMP_USER_ENDPOINT).then(response => {
      commit('setUsers', response)
    }, () => {
      commit('setToast', { type: 'danger', message: 'Error when calling backend' })
    })
  },

  'fetchGroupMembers' ({commit}: { commit: Function }, groupName: String) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/member'
    return api.get(url).then(response => {
      const groupMembers = response.map(toGroupMember)
      commit('setGroupMembers', {groupName, groupMembers})
    }, () => {
      commit('setToast', { type: 'danger', message: 'Error when calling backend' })
    })
  },

  'createGroup' ({commit, dispatch}: { commit: Function, dispatch: Function }, createGroupCmd: CreateGroupCommand) {
    const payload = {
      body: JSON.stringify({
        name: createGroupCmd.groupIdentifier,
        label: createGroupCmd.name
      })
    }
    return new Promise((resolve, reject) => {
      api.post(GROUP_ENDPOINT, payload).then(response => {
        commit('setGroups', response)
        handleSuccess(commit, 'Created ' + createGroupCmd.name + ' group')
        resolve()
      }, (error) => {
        commit('setToast', { type: 'danger', message: error })
        reject(error)
      })
    })
  },

  'addMember' ({commit, dispatch}: { commit: Function, dispatch: Function }, {groupName, addMemberCommand}) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/member'
    const payload = {body: JSON.stringify(addMemberCommand)}

    return new Promise((resolve, reject) => {
      api.post(url, payload).then(() => {
        handleSuccess(commit, 'Added member')
        resolve()
      }, (error) => {
        commit('setToast', {type: 'danger', message: error})
        reject(error)
      })
    })
  },

  'removeMember' ({commit, dispatch}: { commit: Function, dispatch: Function }, {groupName, memberName}) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/member/' + encodeURIComponent(memberName)

    return new Promise((resolve, reject) => {
      api.delete_(url).then(() => {
        handleSuccess(commit, 'Member removed from group')
        resolve()
      }, (error) => {
        commit('setToast', {type: 'danger', message: error})
        reject(error)
      })
    })
  },

  'updateMember' ({commit, dispatch}: { commit: Function, dispatch: Function }, {groupName, memberName, updateMemberCommand}) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/member/' + encodeURIComponent(memberName)
    const payload = {body: JSON.stringify(updateMemberCommand)}

    return new Promise((resolve, reject) => {
      api.put(url, payload).then(() => {
        handleSuccess(commit, 'Member updated')
        resolve()
      }, (error) => {
        commit('setToast', {type: 'danger', message: error})
        reject(error)
      })
    })
  }
}
export default actions
