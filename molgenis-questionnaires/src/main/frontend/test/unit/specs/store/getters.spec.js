import getters from 'src/store/getters'

describe('getters', () => {
  const state = {
    chapterFields: [
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
            required: () => false,
            validate: () => true
          },
          {
            id: 'chapter-1-field-2',
            type: 'number',
            visible: (data) => true,
            required: () => false,
            validate: () => true
          },
          {
            id: 'chapter-1-field-3',
            type: 'number',
            visible: (data) => false,
            required: () => false,
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
      }
    ],
    formData: {
      'chapter-1-field-1': 'value',
      'chapter-1-field-2': 'value',
      'chapter-1-field-3': undefined,
      'chapter-2-field-1': undefined,
      'chapter-3-field-2': undefined,
      'chapter-4-field-2': undefined,
      'chapter-4-field-4': undefined,
      'chapter-4-field-5': 'value',
      'chapter-4-field-6': 'value'
    },
    questionnaire: {
      meta: {
        name: 'test_quest',
        label: 'label',
        description: 'description'
      }
    }
  }

  describe('getChapterByIndex', () => {
    it('should return a chapter based on index [1]', () => {
      const getChapterByIndex = getters.getChapterByIndex(state)
      const actual = getChapterByIndex(1)
      const expected = state.chapterFields[0]

      expect(actual).to.deep.equal(expected)
    })

    it('should return a chapter based on index [2]', () => {
      const getChapterByIndex = getters.getChapterByIndex(state)
      const actual = getChapterByIndex(2)
      const expected = state.chapterFields[1]

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
        'chapter-4': false
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
        'chapter-4': 50
      }

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getTotalNumberOfChapters', () => {
    it('should return the total number of chapters', () => {
      const actual = getters.getTotalNumberOfChapters(state)
      const expected = 4

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
      const actual = getters.getQuestionnaireLabel(state)
      const expected = 'label'

      expect(actual).to.equal(expected)
    })
  })

  describe('getQuestionnaireDescription', () => {
    it('should return the questionnaire description', () => {
      const actual = getters.getQuestionnaireDescription(state)
      const expected = 'description'

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
})
