import type {QuestionnaireEntityResponse} from '../../../../src/flow.types'
import questionnaireService from '../../../../src/services/questionnaireService'

describe('Questionniare service', () => {

  const questionnaireOverView = {
    title: 'test questionnaire',
    chapters:
      [{
        id: 'Chapter1',
        title: 'General questions',
        chapterSections: [
          {
            questionId: 'qMref',
            questionLabel: 'mref type question',
            answerLabel: 'MREF answer'
          },
          {
            questionId: 'qBoolTrue',
            questionLabel: 'Please answer yes or no',
            answerLabel: 'ja'
          },
          {
            questionId: 'qBoolFalse',
            questionLabel: 'Please answer no',
            answerLabel: 'nee'
          },
          {
            questionId: 'qEnum',
            questionLabel: 'Please choose',
            answerLabel: 'Red, Green'
          },
          {
            questionId: 'qXRef',
            questionLabel: 'Please choose one',
            answerLabel: 'XREF answer'
          },
          {
            questionId: 'qNumber',
            questionLabel: 'What is the answer to life the universe and everything',
            answerLabel: '42'
          },
          {
            questionId: 'qDefault',
            questionLabel: 'What\'s my name',
            answerLabel: 'John Doe'
          }
        ]
      },
        {
          id: 'Chapter2',
          title: 'Other questions',
          chapterSections: [
            {
              title: 'Sub section',
              chapterSections: [
                {
                  title: 'Sub sub section',
                  chapterSections: [
                    {
                      questionId: 'qDeepSub',
                      questionLabel: 'What is deep question',
                      answerLabel: 'deepSub'
                    }
                  ]
                }
              ]
            }
          ]
        }]
  }

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

  describe('buildOverViewObject', () => {
    const response: QuestionnaireEntityResponse = {
      href: 'http://foo.bar',
      items: [{
        qMref: [{
          label: 'MREF answer',
        }],
        qBoolTrue: true,
        qBoolFalse: false,
        qEnum: ['Red', 'Green'],
        qXRef: {
          label: 'XREF answer',
        },
        qNumber: 42,
        qDefault: 'John Doe',
        qEmpty: '',
        qDeepSub: 'deepSub'
      }],
      meta: {
        label: 'test questionnaire',
        attributes: [
          {
            fieldType: 'COMPOUND',
            label: 'General questions',
            name: 'Chapter1',
            attributes: [
              {
                attributes: [],
                fieldType: 'MREF',
                label: 'mref type question',
                refEntity: {
                  labelAttribute: 'label'
                },
                name: 'qMref'
              },
              {
                attributes: [],
                fieldType: 'BOOL',
                label: 'Please answer yes or no',
                name: 'qBoolTrue'
              },
              {
                attributes: [],
                fieldType: 'BOOL',
                label: 'Please answer no',
                name: 'qBoolFalse'
              },
              {
                attributes: [],
                fieldType: 'ENUM',
                label: 'Please choose',
                name: 'qEnum'
              },
              {
                attributes: [],
                fieldType: 'XREF',
                label: 'Please choose one',
                refEntity: {
                  labelAttribute: 'label'
                },
                name: 'qXRef'
              },
              {
                attributes: [],
                fieldType: 'INT',
                label: 'What is the answer to life the universe and everything',
                name: 'qNumber'
              },
              {
                attributes: [],
                fieldType: 'String',
                label: 'What\'s my name',
                name: 'qDefault'
              },
              {
                attributes: [],
                fieldType: 'String',
                label: 'What is empty',
                name: 'qEmpty'
              }
            ]
          },
          {
            fieldType: 'COMPOUND',
            label: 'Other questions',
            name: 'Chapter2',
            attributes: [
              {
                fieldType: 'COMPOUND',
                label: 'Sub section',
                name: 'empty subSection',
                attributes: [
                  {
                    fieldType: 'COMPOUND',
                    label: 'empty sub sub section',
                    name: 'subSection',
                    attributes: [
                      {
                        attributes: [],
                        fieldType: 'String',
                        label: 'What is empty',
                        name: 'qEmpty'
                      }
                    ]
                  }
                ]
              },
              {
                fieldType: 'COMPOUND',
                label: 'Sub section',
                name: 'subSection',
                attributes: [
                  {
                    fieldType: 'COMPOUND',
                    label: 'Sub sub section',
                    name: 'subSection',
                    attributes: [
                      {
                        attributes: [],
                        fieldType: 'String',
                        label: 'What is deep question',
                        name: 'qDeepSub'
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      },
      num: 0,
      start: 0,
      total: 100
    }

    it('should build overview object containing the filled out questionnaire', () => {
      const translations = {
        trueLabel: 'ja',
        falseLabel: 'nee'
      }

      const result = questionnaireService.buildOverViewObject(response, translations)
      expect(result).to.deep.equal(questionnaireOverView)
    })
  })

  describe('buildPdfContent', () => {

    it('should build a content object describing the pdf content', () => {

      const pdfContent = questionnaireService.buildPdfContent(questionnaireOverView)
      const expectedPdfContent = [
        {
          'text': 'General questions',
          'style': 'chapterTitle'
        },
        {
          'text': 'mref type question',
          'style': 'questionLabel'
        },
        {
          'text': 'MREF answer',
          'style': 'answerLabel'
        },
        {
          'text': 'Please answer yes or no',
          'style': 'questionLabel'
        },
        {
          'text': 'ja',
          'style': 'answerLabel'
        },
        {
          'text': 'Please answer no',
          'style': 'questionLabel'
        },
        {
          'text': 'nee',
          'style': 'answerLabel'
        },
        {
          'text': 'Please choose',
          'style': 'questionLabel'
        },
        {
          'text': 'Red, Green',
          'style': 'answerLabel'
        },
        {
          'text': 'Please choose one',
          'style': 'questionLabel'
        },
        {
          'text': 'XREF answer',
          'style': 'answerLabel'
        },
        {
          'text': 'What is the answer to life the universe and everything',
          'style': 'questionLabel'
        },
        {
          'text': '42',
          'style': 'answerLabel'
        },
        {
          'text': 'What\'s my name',
          'style': 'questionLabel'
        },
        {
          'text': 'John Doe',
          'style': 'answerLabel'
        },
        {
          'text': 'Other questions',
          'style': 'chapterTitle'
        },
        {
          'text': 'Sub section',
          'style': 'sectionTitle'
        },
        {
          'text': 'Sub sub section',
          'style': 'sectionTitle'
        },
        {
          'text': 'What is deep question',
          'style': 'questionLabel'
        },
        {
          'text': 'deepSub',
          'style': 'answerLabel'
        }
      ]

      // console.log(JSON.stringify(expectedPdfContent, null, 2))

      expect(pdfContent).to.deep.equal(expectedPdfContent)
    })
  })
})
