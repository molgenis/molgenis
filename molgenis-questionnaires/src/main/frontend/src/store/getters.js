const getAllFields = (chapter) => {
  return chapter.children.reduce((accumulator, child) => {
    if (child.type === 'field-group') {
      return getAllFields(child)
    }
    accumulator.push(child.id)
    return accumulator
  }, [])
}

const doesChapterHaveUndefinedValues = (chapter, data) => {
  const fields = getAllFields(chapter)
  return fields.some(id => data[id] === undefined)
}

const getters = {
  getChapterByIndex: (state) => (index) => {
    return [state.chapterFields[(index - 1)]]
  },

  getChapterNavigationList: state => {
    return state.chapterFields.map((chapter, index) => ({id: chapter.id, label: chapter.label, index: (index + 1)}))
  },

  getTotalNumberOfChapters: state => {
    return state.chapterFields.length
  },

  getChapterProgress: state => {
    return state.chapterFields.reduce((accumulator, chapter) => {
      const progress = doesChapterHaveUndefinedValues(chapter, state.formData)
      accumulator[chapter.id] = progress ? 'incomplete' : 'complete'

      return accumulator
    }, {})
  }
}

export default getters
