import actions from '../../../../src/store/actions'
import td from 'testdouble'
import api from '@molgenis/molgenis-api-client'
import testUtils from '@molgenis/molgenis-vue-test-utils'

describe('actions', () => {
  beforeEach(() => {
    td.reset()
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
})
