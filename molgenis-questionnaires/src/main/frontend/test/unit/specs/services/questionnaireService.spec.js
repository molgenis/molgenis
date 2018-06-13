import type {QuestionnaireEntityResponse} from '../../../../src/flow.types'
import questionnaireService from '../../../../src/services/questionnaireService'

describe('Questionniare service', () => {
  describe('buildFormDataObject', () => {
    const response: QuestionnaireEntityResponse = {
      href: 'http://foo.bar',
      items: [],
      meta: {
        attributes: [
          {
            attributes: [],
            fieldType: 'BOOL',
            name: 'question1'
          },
          {
            attributes: [
              {
                attributes: [],
                fieldType: 'STRING',
                name: 'question2'
              }
            ],
            fieldType: 'COMPOUND',
            name: 'section1'
          }
        ]
      },
      num: 0,
      start: 0,
      total: 100
    }

    it('should build the empty formData object', () => {
      const formData = questionnaireService.buildFormDataObject(response)
      expect(formData).to.deep.equal({question1: [], question2: []})
    })
  })
})
