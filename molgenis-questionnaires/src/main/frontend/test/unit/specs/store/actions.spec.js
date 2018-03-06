import actions from 'src/store/actions'
import td from 'testdouble'
import api from '@molgenis/molgenis-api-client'
import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'

const testAction = (action, payload, state, expectedMutations, done) => {
  let count = 0

  // mock commit
  const commit = (type, payload) => {
    const mutation = expectedMutations[count]

    try {
      expect(mutation.type).to.equal(type)
      if (payload) {
        expect(mutation.payload).to.deep.equal(payload)
      }
    } catch (error) {
      done(error)
    }

    count++
    if (count >= expectedMutations.length) {
      done()
    }
  }

  // call the action with mocked store and arguments
  action({commit, state}, payload)

  // check if no mutations should have been dispatched
  if (expectedMutations.length === 0) {
    expect(count).to.equal(0)
    done()
  }
}

describe('actions', () => {
  describe('GET_QUESTIONNAIRE_LIST', () => {
    it('should [GET] a list of questionnaires and commit them to the store', done => {
      const questionnaireList = ['questionnaire']

      const get = td.function('api.get')
      td.when(get('/menu/plugins/questionnaires/list')).thenResolve(questionnaireList)
      td.replace(api, 'get', get)

      const expectedMutations = [
        {type: 'SET_QUESTIONNAIRE_LIST', payload: questionnaireList}
      ]

      testAction(actions.GET_QUESTIONNAIRE_LIST, {}, {}, expectedMutations, done)
    })
  })

  describe('START_QUESTIONNAIRE', () => {
    const questionnaire = 'questionnaire'
    const questionnaireId = 'test_quest'

    const get = td.function('api.get')
    td.when(get('/menu/plugins/questionnaires/start/' + questionnaireId)).thenResolve(questionnaire)
    td.replace(api, 'get', get)

    it('should [GET] a questionnaire to start it', done => {
      const expectedMutations = []
      const state = {
        questionnaireId: questionnaireId
      }

      testAction(actions.START_QUESTIONNAIRE, questionnaireId, state, expectedMutations, done)
    })

    it('should clear the state before starting a new questionnaire', done => {
      const expectedMutations = [
        {type: 'CLEAR_STATE'}
      ]

      const state = {
        questionnaireId: 'other_quest'
      }

      testAction(actions.START_QUESTIONNAIRE, questionnaireId, state, expectedMutations, done)
    })
  })

  describe('GET_QUESTIONNAIRE', () => {
    it('should [GET] a questionnaire and store data in the state', done => {
      const questionnaireId = 'test_quest'
      const questionnaire = {
        meta: {
          label: 'Test Questionnaire',
          description: 'A questionnaire to test',
          idAttribute: 'id'
        },
        items: []
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/' + questionnaireId)).thenResolve(questionnaire)
      td.replace(api, 'get', get)

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
        {type: 'SET_QUESTIONNAIRE_ID', payload: 'test_quest'},
        {type: 'SET_QUESTIONNAIRE_LABEL', payload: 'Test Questionnaire'},
        {type: 'SET_QUESTIONNAIRE_DESCRIPTION', payload: 'A questionnaire to test'},
        {type: 'SET_CHAPTER_FIELDS', payload: chapters},
        {type: 'SET_QUESTIONNAIRE_ROW_ID', payload: 'id'},
        {type: 'SET_FORM_DATA', payload: {id: 'id', field: undefined}}
      ]

      testAction(actions.GET_QUESTIONNAIRE, questionnaireId, state, expectedMutations, done)
    })
  })

  describe('GET_QUESTIONNAIRE_OVERVIEW', () => {
    it('should return return a questionnaire', done => {
      const questionnaireId = 'test_quest'
      const questionnaire = 'questionnaire'

      const get = td.function('api.get')
      td.when(get('/api/v2/' + questionnaireId)).thenResolve(questionnaire)
      td.replace(api, 'get', get)

      const response = actions.GET_QUESTIONNAIRE_OVERVIEW({}, questionnaireId)
      response.then(actual => {
        expect(actual).to.equal(questionnaire)
      })

      done()
    })
  })

  describe('GET_SUBMISSION_TEXT', () => {
    it('should commit a piece of text to the state', done => {
      const questionnaireId = 'test_quest'
      const submissionText = 'thanks'

      const get = td.function('api.get')
      td.when(get('/menu/plugins/questionnaires/' + questionnaireId + '/thanks')).thenResolve(submissionText)
      td.replace(api, 'get', get)

      const expectedMutations = [
        {type: 'SET_SUBMISSION_TEXT', payload: submissionText}
      ]

      testAction(actions.GET_SUBMISSION_TEXT, questionnaireId, {}, expectedMutations, done)
    })
  })

  describe('AUTO_SAVE_QUESTIONNAIRE', () => {
    it('should post data for a single attribute', done => {
      const updatedAttribute = {
        attribute: 'attribute',
        value: 'value'
      }

      const state = {
        questionnaireRowId: 'test_row',
        route: {
          params: {
            questionnaireId: 'test_quest'
          }
        }
      }

      const options = {
        body: JSON.stringify(updatedAttribute.value),
        method: 'PUT'
      }

      const post = td.function('api.post')
      td.when(post('/api/v1/test_quest/test_row/attribute', options)).thenResolve('OK')
      td.replace(api, 'post', post)

      const result = actions.AUTO_SAVE_QUESTIONNAIRE({state}, updatedAttribute)

      result.then(actual => {
        expect(actual).to.equal('OK')
      })

      done()
    })
  })

  describe('SUBMIT_QUESTIONNAIRE', () => {
    it('should submit the questionnaire', done => {
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

      const post = td.function('api.post')
      td.when(post('/api/v2/test_quest', options)).thenResolve('OK')
      td.replace(api, 'post', post)

      const state = {
        formData: {
          field: 'value'
        },
        route: {
          params: {
            questionnaireId: 'test_quest'
          }
        }
      }

      const result = actions.SUBMIT_QUESTIONNAIRE({state}, '2020-01-01')
      result.then(actual => {
        expect(actual).to.equal('OK')
      })

      done()
    })
  })
})
