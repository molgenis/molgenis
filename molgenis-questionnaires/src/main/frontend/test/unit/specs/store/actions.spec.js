import actions from 'src/store/actions'
import td from 'testdouble'
import api from '@molgenis/molgenis-api-client'
import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'

const getters = {
  getQuestionnaireId: 'test_quest'
}

const testAction = (action, payload, state, expectedMutations, expectedActions, done) => {
  let mutationCount = 0
  let actionCount = 0

  const commit = (type, payload) => {
    const mutation = expectedMutations[mutationCount]

    try {
      expect(mutation.type).to.equal(type)
      if (payload) {
        expect(mutation.payload).to.deep.equal(payload)
      }

      mutationCount++
      if (mutationCount >= expectedMutations.length && actionCount >= expectedActions.length) {
        done()
      }
    } catch (error) {
      done(error)
    }
  }

  const dispatch = (type, payload) => {
    const action = expectedActions[actionCount]

    try {
      expect(action.type).to.equal(type)
      if (payload) {
        expect(action.payload).to.deep.equal(payload)
      }

      actionCount++
      if (actionCount >= expectedActions.length && mutationCount >= expectedMutations.length) {
        done()
      }
    } catch (error) {
      done(error)
    }
  }

  action({commit, dispatch, getters, state}, payload)

  if (expectedMutations.length === 0 && expectedActions.length === 0) {
    expect(mutationCount).to.equal(0)
    expect(actionCount).to.equal(0)
    done()
  }
}

const mockApiGetSuccess = (url, response) => {
  const get = td.function('api.get')
  td.when(get(url)).thenResolve(response)
  td.replace(api, 'get', get)
}

const mockApiGetError = (url, error) => {
  const get = td.function('api.get')
  td.when(get(url)).thenReject(error)
  td.replace(api, 'get', get)
}

const mockApiPostSuccess = (uri, options, response) => {
  const post = td.function('api.post')
  td.when(post(uri, options)).thenResolve(response)
  td.replace(api, 'post', post)
}

const mockApiPostError = (uri, options, error) => {
  const post = td.function('api.post')
  td.when(post(uri, options)).thenReject(error)
  td.replace(api, 'post', post)
}

describe('actions', () => {
  beforeEach(() => {
    td.reset()
  })

  describe('GET_QUESTIONNAIRE_LIST', () => {
    it('should [GET] a list of questionnaires and commit it to the store', done => {
      const questionnaireList = ['questionnaire']
      mockApiGetSuccess('/menu/plugins/questionnaires/list', questionnaireList)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: ''},
        {type: 'SET_LOADING', payload: true},
        {type: 'SET_QUESTIONNAIRE_LIST', payload: questionnaireList},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.GET_QUESTIONNAIRE_LIST, {}, {}, expectedMutations, [], done)
    })

    it('should commit any errors to the store', done => {
      const error = 'error'
      mockApiGetError('/menu/plugins/questionnaires/list', error)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: ''},
        {type: 'SET_LOADING', payload: true},
        {type: 'SET_ERROR', payload: error},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.GET_QUESTIONNAIRE_LIST, {}, {}, expectedMutations, [], done)
    })
  })

  describe('START_QUESTIONNAIRE', () => {
    it('should call the start questionnaire uri', done => {
      const questionnaireId = 'test_quest'
      mockApiGetSuccess('/menu/plugins/questionnaires/start/test_quest', 'OK')
      const response = actions.START_QUESTIONNAIRE({}, questionnaireId)

      response.then(actual => {
        expect(actual).to.equal('OK')
        done()
      })
    })

    it('should commit any errors to the store', done => {
      const questionnaireId = 'test_quest'
      const error = 'error'
      mockApiGetError('/menu/plugins/questionnaires/start/test_quest', error)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: error},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.START_QUESTIONNAIRE, questionnaireId, {}, expectedMutations, [], done)
    })
  })

  describe('GET_QUESTIONNAIRE', () => {
    it('should [GET] a questionnaire and store data in the state', done => {
      const questionnaireId = 'other_test_quest'
      const questionnaire = {
        meta: {
          label: 'Test Questionnaire',
          description: 'A questionnaire to test',
          idAttribute: 'id'
        },
        items: []
      }

      mockApiGetSuccess('/api/v2/other_test_quest?includeCategories=true', questionnaire)

      const generatedForm = {
        formFields: [
          {
            id: 'id'
          },
          {
            id: 'compound',
            type: 'field-group',
            children: [
              {
                id: 'field'
              }
            ]
          }
        ],
        formData: {
          id: 'id',
          field: undefined
        }
      }

      const state = {
        mapperOptions: {
          booleanLabels: {
            trueLabel: 'Yes',
            falseLabel: 'No',
            nillLabel: 'No idea'
          }
        }
      }

      const generateForm = td.function('EntityToFormMapper.generateForm')
      td.when(generateForm(questionnaire.meta, {}, state.mapperOptions)).thenReturn(generatedForm)
      td.replace(EntityToFormMapper, 'generateForm', generateForm)

      const chapters = [{
        id: 'compound',
        type: 'field-group',
        children: [
          {
            id: 'field'
          }
        ]
      }]

      const expectedMutations = [
        {type: 'SET_ERROR', payload: ''},
        {type: 'SET_LOADING', payload: true},
        {type: 'SET_QUESTIONNAIRE', payload: questionnaire},
        {type: 'SET_QUESTIONNAIRE_ROW_ID', payload: 'id'},
        {type: 'SET_FORM_DATA', payload: {id: 'id', field: undefined}},
        {type: 'SET_CHAPTER_FIELDS', payload: chapters},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.GET_QUESTIONNAIRE, questionnaireId, state, expectedMutations, [], done)
    })

    it('should commit any errors to the store', done => {
      const questionnaireId = 'other_test_quest'
      const error = 'error'
      mockApiGetError('/api/v2/other_test_quest?includeCategories=true', error)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: ''},
        {type: 'SET_LOADING', payload: true},
        {type: 'SET_ERROR', payload: error},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.GET_QUESTIONNAIRE, questionnaireId, {}, expectedMutations, [], done)
    })
  })

  describe('GET_QUESTIONNAIRE_OVERVIEW', () => {
    it('should return return a questionnaire', done => {
      const questionnaireId = 'test_quest'
      const questionnaire = 'questionnaire'
      mockApiGetSuccess('/api/v2/test_quest', questionnaire)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: ''},
        {type: 'SET_LOADING', payload: true},
        {type: 'SET_QUESTIONNAIRE', payload: questionnaire},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.GET_QUESTIONNAIRE_OVERVIEW, questionnaireId, {}, expectedMutations, [], done)
    })

    it('should commit any errors to the store', done => {
      const questionnaireId = 'test_quest'
      const error = 'error'
      mockApiGetError('/api/v2/test_quest', error)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: ''},
        {type: 'SET_LOADING', payload: true},
        {type: 'SET_ERROR', payload: error},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.GET_QUESTIONNAIRE_OVERVIEW, questionnaireId, {}, expectedMutations, [], done)
    })
  })

  describe('GET_SUBMISSION_TEXT', () => {
    it('should commit a piece of text to the state', done => {
      const questionnaireId = 'test_quest'
      const submissionText = 'thanks'
      mockApiGetSuccess('/menu/plugins/questionnaires/submission-text/test_quest', submissionText)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: ''},
        {type: 'SET_LOADING', payload: true},
        {type: 'SET_SUBMISSION_TEXT', payload: submissionText},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.GET_SUBMISSION_TEXT, questionnaireId, {}, expectedMutations, [], done)
    })

    it('should commit any errors to the store', done => {
      const questionnaireId = 'test_quest'
      const error = 'error'
      mockApiGetError('/menu/plugins/questionnaires/submission-text/test_quest', error)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: ''},
        {type: 'SET_LOADING', payload: true},
        {type: 'SET_ERROR', payload: error},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.GET_SUBMISSION_TEXT, questionnaireId, {}, expectedMutations, [], done)
    })
  })

  describe('AUTO_SAVE_QUESTIONNAIRE', () => {
    const formData = {
      field1: 'updated value'
    }

    const state = {
      questionnaire: {
        meta: {
          name: 'test_quest'
        }
      },
      formData: {
        field1: 'value',
        field2: 'value'
      },
      questionnaireRowId: 'test_row'
    }

    const options = {
      body: JSON.stringify(formData['field1']),
      method: 'PUT'
    }

    it('should post data for a single attribute', done => {
      mockApiPostSuccess('/api/v1/test_quest/test_row/field1', options, 'OK')

      const expectedMutations = [
        {type: 'INCREMENT_SAVING_QUEUE'},
        {type: 'SET_FORM_DATA', payload: formData},
        {type: 'DECREMENT_SAVING_QUEUE'},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.AUTO_SAVE_QUESTIONNAIRE, formData, state, expectedMutations, [], done)
    })
  })

  describe('SUBMIT_QUESTIONNAIRE', () => {
    const submitData = {
      field: 'value',
      submitDate: '2020-01-01'
    }

    const options = {
      body: JSON.stringify({
        entities: [
          submitData
        ]
      }),
      method: 'PUT'
    }

    const state = {
      formData: {
        field: 'value'
      },
      questionnaire: {
        meta: {
          name: 'test_quest'
        }
      }
    }

    it('should submit the questionnaire', done => {
      mockApiPostSuccess('/api/v2/test_quest', options, 'OK')

      const result = actions.SUBMIT_QUESTIONNAIRE({state}, '2020-01-01')
      result.then(actual => {
        expect(actual).to.equal('OK')
      })

      done()
    })

    it('should commit any errors to the store', done => {
      const error = 'error'
      mockApiPostError('/api/v2/test_quest', options, error)

      const expectedMutations = [
        {type: 'SET_ERROR', payload: error},
        {type: 'SET_LOADING', payload: false}
      ]

      testAction(actions.SUBMIT_QUESTIONNAIRE, '2020-01-01', state, expectedMutations, [], done)
    })
  })
})
