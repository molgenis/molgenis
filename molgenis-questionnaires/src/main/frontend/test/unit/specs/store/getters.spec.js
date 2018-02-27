import getters from '../../../../src/store/getters'

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
            type: 'text'
          },
          {
            id: 'chapter-1-field-2',
            type: 'number'
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
            type: 'text'
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
                type: 'text'
              }
            ]
          }
        ]
      }
    ],
    formData: {
      'chapter-1-field-1': 'value',
      'chapter-1-field-2': 'value',
      'chapter-2-field-1': undefined,
      'chapter-3-field-2': undefined
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
        }
      ]

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getTotalNumberOfChapters', () => {
    it('should return the total number of chapters', () => {
      const actual = getters.getTotalNumberOfChapters(state)
      const expected = 3

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getChapterProgress', () => {
    it('should return the chapter progress based on data and chapters', () => {
      const actual = getters.getChapterProgress(state)
      const expected = {
        'chapter-1': 'complete',
        'chapter-2': 'incomplete',
        'chapter-3': 'incomplete'
      }

      expect(actual).to.deep.equal(expected)
    })
  })
})
