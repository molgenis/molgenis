import actions from '../../../../src/store/actions'
import td from 'testdouble'
import api from '@molgenis/molgenis-api-client'
import testUtils from '@molgenis/molgenis-vue-test-utils'
import asyncUtilService from '../../../../src/service/asyncUtilService'

describe('actions', () => {
  beforeEach(() => {
    td.reset()
    td.replace(asyncUtilService, 'callAfter', (f) => f())
  })

  describe('fetchGroups', () => {
    it('should fetch a list of groups and commit them to the store', done => {
      const groupList = ['groups']

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group')).thenResolve(groupList)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: 'setGroups', payload: groupList}
        ]
      }

      testUtils.testAction(actions.fetchGroups, options, done)
    })

    it('should commit any errors to the store', done => {
      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.fetchGroups, options, done)
    })
  })

  describe('fetchGroupRoles', () => {
    const groupName = 'my-group'

    it('should fetch a list of groups roles for a given group and commit them to the store', done => {
      const roles = [
        {roleName: 'VIEWER', roleLabel: 'Viewer'},
        {roleName: 'ADMIN', roleLabel: 'Admin'}
      ]

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group/my-group/role')).thenResolve(roles)
      td.replace(api, 'get', get)

      const options = {
        payload: groupName,
        expectedMutations: [
          {type: 'setGroupRoles', payload: {groupName, groupRoles: roles}}
        ]
      }

      testUtils.testAction(actions.fetchGroupRoles, options, done)
    })

    it('should commit any errors to the store', done => {
      const get = td.function('api.get')
      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }
      td.when(get('/api/plugin/security/group/error-group/role')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        payload: 'error-group',
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.fetchGroupRoles, options, done)
    })
  })

  describe('tempFetchUsers', () => {
    it('should fetch a list of all users ( until we have a invite system)', done => {
      const users = [
        {id: 'dsds-34324-2', username: 'user1'},
        {id: 'dsds-34324-3', username: 'user2'}
      ]

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/user')).thenResolve(users)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: 'setUsers', payload: users}
        ]
      }

      testUtils.testAction(actions.tempFetchUsers, options, done)
    })

    it('should commit any errors to the store', done => {
      const get = td.function('api.get')
      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }
      td.when(get('/api/plugin/security/user')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.tempFetchUsers, options, done)
    })
  })

  describe('fetchGroupMembers', () => {
    const groupName = 'my-group'

    it('should fetch a list of groups roles for a given group and commit them to the store', done => {
      const members = [
        {
          user: {id: '123-abc', username: 'user1'},
          role: {roleName: 'ADMIN', roleLabel: 'Admin'}
        },
        {
          user: {id: '456-dfg', username: 'user2'},
          role: {roleName: 'VIEWER', roleLabel: 'Viewer'}
        }
      ]

      const groupMembers = [
        {
          userId: members[0].user.id,
          username: members[0].user.username,
          roleName: members[0].role.roleName,
          roleLabel: members[0].role.roleLabel
        }, {
          userId: members[1].user.id,
          username: members[1].user.username,
          roleName: members[1].role.roleName,
          roleLabel: members[1].role.roleLabel
        }
      ]

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group/my-group/member')).thenResolve(members)
      td.replace(api, 'get', get)

      const options = {
        payload: groupName,
        expectedMutations: [
          {type: 'setGroupMembers', payload: {groupName, groupMembers}}
        ]
      }

      testUtils.testAction(actions.fetchGroupMembers, options, done)
    })

    it('should commit any errors to the store', done => {
      const get = td.function('api.get')
      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }
      td.when(get('/api/plugin/security/group/error-group/member')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        payload: 'error-group',
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.fetchGroupMembers, options, done)
    })
  })

  describe('fetchGroupPermissions', () => {
    const groupName = 'my-group'

    it('should fetch a list of groups roles for a given group and commit them to the store', done => {
      const groupPermissions = ['ADD_MEMBERSHIP', 'REMOVE_MEMBERSHIP']

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group/my-group/permission')).thenResolve(groupPermissions)
      td.replace(api, 'get', get)

      const options = {
        payload: groupName,
        expectedMutations: [
          {type: 'setGroupPermissions', payload: {groupName, groupPermissions}}
        ]
      }

      testUtils.testAction(actions.fetchGroupPermissions, options, done)
    })

    it('should commit any errors to the store', done => {
      const get = td.function('api.get')
      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }
      td.when(get('/api/plugin/security/group/error-group/permission')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        payload: 'error-group',
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.fetchGroupPermissions, options, done)
    })
  })

  describe('createGroup', () => {
    it('should create a group and displays toast', done => {
      const createGroupCommand = {
        groupIdentifier: 'test',
        name: 'test-name'
      }

      const generatedPayload = {
        body: JSON.stringify({
          name: 'test',
          label: 'test-name'
        })
      }

      const response = {
        name: 'test',
        label: 'test-name'
      }

      const post = td.function('api.post')
      td.when(post('/api/plugin/security/group', generatedPayload)).thenResolve(response)
      td.replace(api, 'post', post)

      const options = {
        payload: createGroupCommand,
        expectedMutations: [
          {type: 'setGroups', payload: response},
          {type: 'setToast', payload: {type: 'success', message: 'Created test-name group'}},
          {type: 'clearToast'}
        ]
      }

      testUtils.testAction(actions.createGroup, options, done)
    })

    it('should commit any errors to the store', done => {
      const createGroupCommand = {
        groupIdentifier: 'test',
        name: 'test group'
      }

      const generatedPayload = {
        body: JSON.stringify({
          name: 'test',
          label: 'test group'
        })
      }

      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }

      const post = td.function('api.post')
      td.when(post('/api/plugin/security/group', generatedPayload)).thenReject(error)
      td.replace(api, 'post', post)

      const options = {
        payload: createGroupCommand,
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.createGroup, options, done)
    })
  })

  describe('addMember', () => {
    const groupName = 'my-group'

    it('should add a member to the group and displays toast', done => {
      const addMemberCommand = {username: 'user1', roleName: 'VIEWER'}

      const generatedPayload = {
        body: JSON.stringify(addMemberCommand)
      }

      const post = td.function('api.post')
      td.when(post('/api/plugin/security/group/my-group/member', generatedPayload)).thenResolve()
      td.replace(api, 'post', post)

      const options = {
        payload: {groupName, addMemberCommand},
        expectedMutations: [
          {type: 'setToast', payload: {type: 'success', message: 'Added member'}},
          {type: 'clearToast'}
        ]
      }

      testUtils.testAction(actions.addMember, options, done)
    })

    it('should commit any errors to the store', done => {
      const addMemberCommand = {username: 'user1', roleName: 'ERROR-ROLE'}

      const generatedPayload = {
        body: JSON.stringify(addMemberCommand)
      }

      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }

      const post = td.function('api.post')
      td.when(post('/api/plugin/security/group/my-group/member', generatedPayload)).thenReject(error)
      td.replace(api, 'post', post)

      const options = {
        payload: {groupName, addMemberCommand},
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.addMember, options, done)
    })
  })

  describe('removeMember', () => {
    const groupName = 'my-group'
    const memberName = 'user1'

    it('should remove a member from the group and displays toast', done => {
      const delete_ = td.function('api.delete_')
      td.when(delete_('/api/plugin/security/group/my-group/member/user1')).thenResolve()
      td.replace(api, 'delete_', delete_)

      const options = {
        payload: {groupName, memberName},
        expectedMutations: [
          {type: 'setToast', payload: {type: 'success', message: 'Member removed from group'}},
          {type: 'clearToast'}
        ]
      }

      testUtils.testAction(actions.removeMember, options, done)
    })

    it('should commit any errors to the store', done => {
      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }

      const delete_ = td.function('api.delete_')
      td.when(delete_('/api/plugin/security/group/my-group/member/user1')).thenReject(error)
      td.replace(api, 'delete_', delete_)

      const options = {
        payload: {groupName, memberName},
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.removeMember, options, done)
    })
  })

  describe('updateMember', () => {
    const groupName = 'my-group'
    const memberName = 'user1'

    const updateMemberCommand = {roleName: 'NEW-ROLE'}

    it('should updateMember a member from the group and displays toast', done => {
      const generatedPayload = {
        body: JSON.stringify(updateMemberCommand)
      }

      const put = td.function('api.put')
      td.when(put('/api/plugin/security/group/my-group/member/user1', generatedPayload)).thenResolve()
      td.replace(api, 'put', put)

      const options = {
        payload: {groupName, memberName, updateMemberCommand},
        expectedMutations: [
          {type: 'setToast', payload: {type: 'success', message: 'Member updated'}},
          {type: 'clearToast'}
        ]
      }

      testUtils.testAction(actions.updateMember, options, done)
    })

    it('should commit any errors to the store', done => {
      const error = {
        errors: [{
          message: 'Error when calling',
          code: 'backend'
        }]
      }

      const generatedPayload = {
        body: JSON.stringify(updateMemberCommand)
      }

      const put = td.function('api.put')
      td.when(put('/api/plugin/security/group/my-group/member/user1', generatedPayload)).thenReject(error)
      td.replace(api, 'put', put)

      const options = {
        payload: {groupName, memberName, updateMemberCommand},
        expectedMutations: [
          {type: 'setToast', payload: {type: 'danger', message: 'Error when calling (backend)'}}
        ]
      }

      testUtils.testAction(actions.updateMember, options, done)
    })
  })
})
