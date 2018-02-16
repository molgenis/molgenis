const getters = {
  getChapterByIndex: (state) => (index) => {
    return [state.chapterFields[(index - 1)]]
  },

  getTotalNumberOfChapters: state => {
    return state.chapterFields.length
  }
}

export default getters
