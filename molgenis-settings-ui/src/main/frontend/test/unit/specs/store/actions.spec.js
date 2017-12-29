import api from '@molgenis/molgenis-api-client'
import td from 'testdouble'
import actions from '@/store/actions'
import { SET_RAW_SETTINGS } from '@/store/mutations'
import utils from '@molgenis/molgenis-vue-test-utils'

describe('actions', () => {
  afterEach(() => { td.reset() })

  describe('GET_SETTINGS', function () {
    it('should fetch the application settings', done => {
      const setSysAppEntity = {id: 'set_sys_app', label: 'Application settings'}
      const response = {
        items: [setSysAppEntity]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_set_app?start=0&num=0')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: SET_RAW_SETTINGS, payload: {items: [setSysAppEntity]}}
        ]
      }

      utils.testAction(actions.__GET_SETTINGS__, options, done)
    })
  })
})
