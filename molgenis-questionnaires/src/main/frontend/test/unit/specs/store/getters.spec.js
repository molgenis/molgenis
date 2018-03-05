import getters from 'src/store/getters'

describe('getters', () => {
  const state = {
    chapterFields: [
      {
        id: 'chapter-1',
        label: 'First chapter',
        type: 'field-group',
        children: [
          {
            id: 'chapter-1-field-1',
            type: 'text',
            visible: (data) => true
          },
          {
            id: 'chapter-1-field-2',
            type: 'number',
            visible: (data) => true
          },
          {
            id: 'chapter-1-field-3',
            type: 'number',
            visible: (data) => false
          }
        ]
      },
      {
        id: 'chapter-2',
        label: 'Second chapter',
        type: 'field-group',
        children: [
          {
            id: 'chapter-2-field-1',
            type: 'text',
            visible: (data) => true
          }
        ]
      },
      {
        id: 'chapter-3',
        label: 'Third chapter',
        type: 'field-group',
        children: [
          {
            id: 'chapter-3-field-1',
            type: 'field-group',
            children: [
              {
                id: 'chapter-3-field-2',
                type: 'text',
                visible: (data) => true
              }
            ]
          }
        ]
      },
      {
        id: 'chapter-4',
        label: 'Fourth chapter',
        type: 'field-group',
        children: [
          {
            id: 'chapter-4-field-1',
            type: 'field-group',
            children: [
              {
                id: 'chapter-4-field-2',
                type: 'text',
                visible: (data) => false
              }
            ]
          },
          {
            id: 'chapter-4-field-3',
            type: 'field-group',
            children: [
              {
                id: 'chapter-4-field-4',
                type: 'text',
                visible: (data) => false
              }
            ]
          },
          {
            id: 'chapter-4-field-5',
            type: 'text',
            visible: (data) => data['chapter-1-field-1'] === 'value'
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
      'chapter-4-field-5': 'value'
    }
  }

  describe('getChapterByIndex', () => {
    it('should return a chapter based on index [1]', () => {
      const getChapterByIndex = getters.getChapterByIndex(state)
      const actual = getChapterByIndex(1)
      const expected = [state.chapterFields[0]]

      expect(actual).to.deep.equal(expected)
    })

    it('should return a chapter based on index [2]', () => {
      const getChapterByIndex = getters.getChapterByIndex(state)
      const actual = getChapterByIndex(2)
      const expected = [state.chapterFields[1]]

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

  describe('getTotalNumberOfChapters', () => {
    it('should return the total number of chapters', () => {
      const actual = getters.getTotalNumberOfChapters(state)
      const expected = 4

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getChapterProgress', () => {
    it('should return the chapter progress based on data and chapters', () => {
      const actual = getters.getChapterProgress(state)
      const expected = {
        'chapter-1': 'complete',
        'chapter-2': 'incomplete',
        'chapter-3': 'incomplete',
        'chapter-4': 'complete'
      }

      expect(actual).to.deep.equal(expected)
    })
  })
})
