const getters = {
  getChapterByIndex: (state) => (index) => {
    return [state.chapterFields[(index - 1)]]
  },

  getChapterNavigationList: state => {
    return state.chapterFields.map((chapter, index) => ({label: chapter.label, index: (index + 1)}))
  },

  getTotalNumberOfChapters: state => {
    return state.chapterFields.length
  }
}

export default getters
