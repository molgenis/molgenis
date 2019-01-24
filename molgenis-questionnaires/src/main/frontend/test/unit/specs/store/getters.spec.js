import getters from 'src/store/getters'

describe('getters', () => {
  const state = {
    chapters: [
      {
        id: 'chapter-1',
        label: 'First chapter',
        type: 'field-group',
        visible: () => true,
        children: [
          {
            id: 'chapter-1-field-1',
            type: 'text',
            visible: (data) => true,
            required: () => true,
            validate: () => true
          },
          {
            id: 'chapter-1-field-2',
            type: 'number',
            visible: (data) => true,
            required: () => true,
            validate: () => true,
            range: {
              min: 3,
              max: 7
            }
          },
          {
            id: 'chapter-1-field-3',
            type: 'number',
            visible: (data) => false,
            required: () => true,
            validate: () => true
          }
        ]
      },
      {
        id: 'chapter-2',
        label: 'Second chapter',
        type: 'field-group',
        visible: () => true,
        children: [
          {
            id: 'chapter-2-field-1',
            type: 'text',
            visible: (data) => true,
            required: () => true,
            validate: () => true
          }
        ]
      },
      {
        id: 'chapter-3',
        label: 'Third chapter',
        type: 'field-group',
        visible: () => true,
        children: [
          {
            id: 'chapter-3-field-1',
            type: 'field-group',
            visible: () => true,
            children: [
              {
                id: 'chapter-3-field-2',
                label: 'chapter-3-field-2-label',
                type: 'text',
                visible: (data) => true,
                required: () => false,
                validate: () => true
              }
            ]
          }
        ]
      },
      {
        id: 'chapter-4',
        label: 'Fourth chapter',
        type: 'field-group',
        visible: () => true,
        children: [
          {
            id: 'chapter-4-field-1',
            type: 'field-group',
            visible: () => true,
            children: [
              {
                id: 'chapter-4-field-2',
                type: 'text',
                visible: (data) => true,
                required: () => false,
                validate: () => true
              }
            ]
          },
          {
            id: 'chapter-4-field-3',
            type: 'field-group',
            visible: () => true,
            children: [
              {
                id: 'chapter-4-field-4',
                type: 'text',
                visible: (data) => true,
                required: () => true,
                validate: () => true
              }
            ]
          },
          {
            id: 'chapter-4-field-5',
            type: 'text',
            visible: (data) => data['chapter-1-field-1'] === 'value',
            required: () => true,
            validate: () => true
          },
          {
            id: 'chapter-4-field-6',
            type: 'text',
            visible: (data) => true,
            required: () => true,
            validate: () => true
          }
        ]
      },
      {
        id: 'chapter-5',
        label: 'Fifth chapter',
        type: 'field-group',
        visible: () => true,
        children: [
          {
            id: 'chapter-5-field-1',
            type: 'field-group',
            visible: () => true,
            children: [
              {
                id: 'chapter-5-field-2',
                type: 'text',
                visible: (data) => true,
                required: () => true,
                validate: () => false
              }
            ]
          }
        ]
      },
      {
        id: 'chapter-6',
        label: 'Sixth chapter',
        type: 'field-group',
        visible: () => true,
        children: [
          {
            id: 'chapter-6-field-1',
            type: 'field-group',
            visible: () => true,
            children: [
              {
                id: 'chapter-6-field-2',
                type: 'number',
                visible: (data) => true,
                required: () => true,
                validate: () => true,
                range: {
                  min: 3
                }
              }
            ]
          }
        ]
      },
      {
        id: 'chapter-7',
        label: 'Seventh chapter',
        type: 'field-group',
        visible: () => true,
        children: [
          {
            id: 'chapter-7-field-1',
            type: 'field-group',
            visible: () => true,
            children: [
              {
                id: 'chapter-7-field-2',
                type: 'number',
                visible: (data) => true,
                required: () => true,
                validate: () => true,
                range: {
                  max: 5
                }
              }
            ]
          }
        ]
      },
      {
        id: 'chapter-8',
        label: 'Eight chapter',
        type: 'field-group',
        visible: () => true,
        children: [
          {
            id: 'chapter-8-field-1',
            type: 'field-group',
            visible: () => true,
            children: [
              {
                id: 'chapter-8-field-2',
                type: 'checkbox',
                visible: (data) => true,
                required: () => true,
                validate: () => true
              }
            ]
          }
        ]
      }
    ],
    formData: {
      'chapter-1-field-1': 'value',
      'chapter-1-field-2': 5, // between min and max
      'chapter-1-field-3': undefined,
      'chapter-2-field-1': undefined,
      'chapter-3-field-2': undefined,
      'chapter-4-field-2': undefined,
      'chapter-4-field-4': undefined,
      'chapter-4-field-5': 'value',
      'chapter-4-field-6': 'value',
      'chapter-6-field-2': 1, // below min
      'chapter-7-field-2': 6, // above max
      'chapter-8-field-2': []
    },
    questionnaire: {
      meta: {
        name: 'test_quest',
        label: 'label',
        description: 'description'
      }
    },
    questionnaireList: [
      {id: 'q1', label: 'questionaire1', description: 'desc1', status: 'OPEN'},
      {id: 'q2', label: 'questionaire2', description: 'desc2', status: 'OPEN'}
    ]
  }

  describe('getChapterByIndex', () => {
    it('should return a chapter based on index [1]', () => {
      const getChapterByIndex = getters.getChapterByIndex(state)
      const actual = getChapterByIndex(1)
      const expected = state.chapters[0]

      expect(actual).to.deep.equal(expected)
    })

    it('should return a chapter based on index [2]', () => {
      const getChapterByIndex = getters.getChapterByIndex(state)
      const actual = getChapterByIndex(2)
      const expected = state.chapters[1]

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getChapterCompletion', () => {
    it('should map whether a chapter is completed for all chapters', () => {
      const actual = getters.getChapterCompletion(state)
      const expected = {
        'chapter-1': true,
        'chapter-2': false,
        'chapter-3': true,
        'chapter-4': false,
        'chapter-5': false,
        'chapter-6': false,
        'chapter-7': false,
        'chapter-8': false
      }

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getChapterNavigationList', () => {
    it('should map a list of chapters to a navigation format', () => {
      const actual = getters.getChapterNavigationList(state)
      const expected = [
        {
          id: 'chapter-1',
          label: 'First chapter',
          index: 1
        },
        {
          id: 'chapter-2',
          label: 'Second chapter',
          index: 2
        },
        {
          id: 'chapter-3',
          label: 'Third chapter',
          index: 3
        },
        {
          id: 'chapter-4',
          label: 'Fourth chapter',
          index: 4
        },
        {
          id: 'chapter-5',
          label: 'Fifth chapter',
          index: 5
        },
        {
          id: 'chapter-6',
          label: 'Sixth chapter',
          index: 6
        },
        {
          id: 'chapter-7',
          label: 'Seventh chapter',
          index: 7
        },
        {
          id: 'chapter-8',
          label: 'Eight chapter',
          index: 8
        }
      ]

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getChapterProgress', () => {
    it('should calculate the percentage of completion for every chapter', () => {
      const actual = getters.getChapterProgress(state)
      const expected = {
        'chapter-1': 100,
        'chapter-2': 0,
        'chapter-3': 0,
        'chapter-4': 67,
        'chapter-5': 0,
        'chapter-6': 100,
        'chapter-7': 100,
        'chapter-8': 0
      }

      expect(actual).to.deep.equal(expected)
    })

    it('should catch and log a warning in case the visible expression evaluation throws an error', () => {
      let stub = sinon.stub(console, 'warn')

      const stateWithError = {
        chapters: [
          {
            id: 'chapter-1',
            label: 'First chapter',
            type: 'field-group',
            children: [
              {
                id: 'chapter-1-field-1',
                type: 'text',
                visible: (data) => {
                  throw new Error('This can\'t be happening!')
                }
              }
            ]
          }
        ],
        formData: {'chapter-1-field-1': 'value'},
        questionnaire: {}
      }

      getters.getChapterProgress(stateWithError)

      expect(console.warn.called).to.equal(true)
      expect(console.warn.calledWith(sinon.match('Setting chapter-1-field-1.visible to false because expression evaluation threw an error.'))).to.equal(true)
      stub.reset()
      stub.resetBehavior()
    })
  })

  describe('getTotalNumberOfChapters', () => {
    it('should return the total number of chapters', () => {
      const actual = getters.getTotalNumberOfChapters(state)
      const expected = 8

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getQuestionnaireId', () => {
    it('should return the questionnaire ID', () => {
      const actual = getters.getQuestionnaireId(state)
      const expected = 'test_quest'

      expect(actual).to.equal(expected)
    })
  })

  describe('getQuestionnaireLabel', () => {
    it('should return the questionnaire label', () => {
      const actual = getters.getQuestionnaireLabel(state)(state.questionnaireList[0].id)
      const expected = state.questionnaireList[0].label

      expect(actual).to.equal(expected)
    })
  })

  describe('getQuestionnaireDescription', () => {
    it('should return the questionnaire description', () => {
      const actual = getters.getQuestionnaireDescription(state)(state.questionnaireList[0].id)
      const expected = state.questionnaireList[0].description

      expect(actual).to.equal(expected)
    })
  })

  describe('isSaving', () => {
    it('should return false if the numberOfOutstandingCalls is zero', () => {
      state.numberOfOutstandingCalls = 0
      expect(getters.isSaving(state)).to.equal(false)
    })

    it('should return true if the numberOfOutstandingCalls is bigger then zero', () => {
      state.numberOfOutstandingCalls = 2
      expect(getters.isSaving(state)).to.equal(true)
    })
  })

  describe('getChapterLabel', () => {
    it('should return the chapter label for a given chapter id', () => {
      const chapterId = 'chapter-2'
      expect(getters.getChapterLabel(state)(chapterId)).to.deep.equal('Second chapter')
    })

    it('should thrown an exception if the given id is not a chapter id', () => {
      const chapterId = 'non-existent-id'
      expect(() => getters.getChapterLabel(state)(chapterId)).to.throw()
    })
  })

  describe('getQuestionLabel', () => {
    it('should return the question label for a given question id', () => {
      const questionId = 'chapter-3-field-2'
      expect(getters.getQuestionLabel(state)(questionId)).to.deep.equal('chapter-3-field-2-label')
    })

    it('should thrown an exception if the given id is not a question id', () => {
      const questionId = 'non-existent-id'
      expect(() => getters.getQuestionLabel(state)(questionId)).to.throw()
    })
  })
})
