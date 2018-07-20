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

const buildErrorMessage = (response) => {
  if (response.errors) {
    return response.errors.map((error) => {
      return error.message + ' (' + error.code + ')'
    }).join(', ')
  } else {
    // fallback if error is not in expected format
    return 'An error has occurred.'
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
    }, (response) => {
      commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
    })
  },

  'fetchGroupRoles' ({commit}: { commit: Function }, groupName: String) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/role'
    return api.get(url).then(response => {
      commit('setGroupRoles', {groupName, groupRoles: response})
    }, (response) => {
      commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
    })
  },

  'tempFetchUsers' ({commit}: { commit: Function }) {
    return api.get(TEMP_USER_ENDPOINT).then(response => {
      commit('setUsers', response)
    }, (response) => {
      commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
    })
  },

  'fetchGroupMembers' ({commit}: { commit: Function }, groupName: String) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/member'
    return api.get(url).then(response => {
      const groupMembers = response.map(toGroupMember)
      commit('setGroupMembers', {groupName, groupMembers})
    }, (response) => {
      commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
    })
  },

  'fetchGroupPermissions' ({commit}: { commit: Function }, groupName: String) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/permission'
    return api.get(url).then(groupPermissions => {
      commit('setGroupPermissions', { groupName, groupPermissions })
    }, (response) => {
      commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
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
      }, (response) => {
        commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
        reject(response)
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
      }, (response) => {
        commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
        reject(response)
      })
    })
  },

  'removeMember' ({commit, dispatch}: { commit: Function, dispatch: Function }, {groupName, memberName}) {
    const url = GROUP_ENDPOINT + '/' + encodeURIComponent(groupName) + '/member/' + encodeURIComponent(memberName)

    return new Promise((resolve, reject) => {
      api.delete_(url).then(() => {
        handleSuccess(commit, 'Member removed from group')
        resolve()
      }, (response) => {
        commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
        reject(response)
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
      }, (response) => {
        commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
        reject(response)
      })
    })
  },

  'checkRootPackageExists' ({commit, dispatch}: { commit: Function, dispatch: Function }, packageName) {
    const url = '/api/v2/sys_md_Package?&num=1&q=label=="' + encodeURIComponent(packageName) + '";parent==""'

    return new Promise((resolve, reject) => {
      api.get(url).then((response) => {
        const exists = response.items.length > 0
        resolve(exists)
      }, (response) => {
        commit('setToast', { type: 'danger', message: buildErrorMessage(response) })
        reject(response)
      })
    })
  }
}
export default actions
