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
