import utils from '@molgenis/molgenis-vue-test-utils'
import td from 'testdouble'
import api from '@molgenis/molgenis-api-client'

import actions from 'store/actions'
import { SET_ERRORS, SET_RESULTS } from 'store/mutations'

describe('actions', () => {
  afterEach(() => td.reset())

  describe('SEARCH_ALL', () => {
    const response = {
      entityTypes: [
        {
          getId: 'it_emx_datatypes_TypeTestRef',
          getLabel: 'TypeTestRef',
          getDescription: 'MOLGENIS Data types test ref entity',
          getPackageId: 'it_emx_datatypes',
          isLabelMatch: false,
          isDescriptionMatch: true,
          getAttributes: [],
          nrOfMatchingEntities: 0
        }, {
          getId: 'it_emx_datatypes_TypeTest',
          getLabel: 'TypeTest',
          getDescription: 'MOLGENIS Data types test entity',
          getPackageId: 'it_emx_datatypes',
          isLabelMatch: false,
          isDescriptionMatch: true,
          getAttributes: [
            {
              label: 'xdate label',
              description: 'Typetest date attribute',
              dataType: 'DATE'
            },
            {
              label: 'xdatenillable label',
              description: 'Typetest nillable date attribute',
              dataType: 'DATE'
            }
          ],
          nrOfMatchingEntities: 12
        }
      ],
      packages: [
        {
          label: 'it_emx_datatypes',
          description: 'MOLGENIS datatypes test package'
        }
      ]
    }

    it('should call search all endpoint and store results in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/api/searchall/search?term=test')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: 'test',
        expectedMutations: [
          {type: SET_RESULTS, payload: response}
        ]
      }

      utils.testAction(actions.__SEARCH_ALL__, options, done)
    })

    it('should fail and set an error in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/api/searchall/search?term=test')).thenReject('ERRORRRRR')
      td.replace(api, 'get', get)

      const options = {
        payload: 'test',
        expectedMutations: [
          {type: SET_ERRORS, payload: 'ERRORRRRR'}
        ]
      }

      utils.testAction(actions.__SEARCH_ALL__, options, done)
    })
  })
})
