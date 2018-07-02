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
      const error = 'error'

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: 'setToast', payload: { type: 'danger', message: 'Error when calling backend' }}
        ]
      }

      testUtils.testAction(actions.fetchGroups, options, done)
    })
  })

  describe('fetchGroupRoles', () => {

    const groupName = 'my-group'

    it('should fetch a list of groups roles for a given group and commit them to the store', done => {
      const roles = [
        { roleName: 'VIEWER', roleLabel: 'Viewer' },
        { roleName: 'ADMIN', roleLabel: 'Admin' }
      ]

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group/my-group/role')).thenResolve(roles)
      td.replace(api, 'get', get)

      const options = {
        payload: groupName,
        expectedMutations: [
          { type: 'setGroupRoles', payload: { groupName, groupRoles: roles } }
        ]
      }

      testUtils.testAction(actions.fetchGroupRoles, options, done)
    })

    it('should commit any errors to the store', done => {
      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group/error-group/role')).thenReject()
      td.replace(api, 'get', get)

      const options = {
        payload: 'error-group',
        expectedMutations: [
          {type: 'setToast', payload: { type: 'danger', message: 'Error when calling backend' }}
        ]
      }

      testUtils.testAction(actions.fetchGroupRoles, options, done)
    })
  })

  describe('tempFetchUsers', () => {

    it('should fetch a list of all users ( until we have a invite system)', done => {
      const users = [
        { id: 'dsds-34324-2', username: 'user1' },
        { id: 'dsds-34324-3', username: 'user2' }
      ]

      const get = td.function('api.get')
      td.when(get('/api/plugin/security/user')).thenResolve(users)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          { type: 'setUsers', payload: users }
        ]
      }

      testUtils.testAction(actions.tempFetchUsers, options, done)
    })

    it('should commit any errors to the store', done => {
      const get = td.function('api.get')
      td.when(get('/api/plugin/security/user')).thenReject()
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: 'setToast', payload: { type: 'danger', message: 'Error when calling backend' }}
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
          { type: 'setGroupMembers', payload: { groupName, groupMembers } }
        ]
      }

      testUtils.testAction(actions.fetchGroupMembers, options, done)
    })

    it('should commit any errors to the store', done => {
      const get = td.function('api.get')
      td.when(get('/api/plugin/security/group/error-group/member')).thenReject()
      td.replace(api, 'get', get)

      const options = {
        payload: 'error-group',
        expectedMutations: [
          {type: 'setToast', payload: { type: 'danger', message: 'Error when calling backend' }}
        ]
      }

      testUtils.testAction(actions.fetchGroupMembers, options, done)
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
          {type: 'setToast', payload: { type: 'success', message: 'Created test-name group' }},
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

      const error = 'error'

      const post = td.function('api.post')
      td.when(post('/api/plugin/security/group', generatedPayload)).thenReject(error)
      td.replace(api, 'post', post)

      const options = {
        payload: createGroupCommand,
        expectedMutations: [
          {type: 'setToast', payload: { type: 'danger', message: error }}
        ]
      }

      testUtils.testAction(actions.createGroup, options, done)
    })
  })
})
