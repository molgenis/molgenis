const getAllFields = (chapter, data) => {
  return chapter.children.reduce((accumulator, child) => {
    if (child.type === 'field-group') {
      return [...accumulator, ...getAllFields(child, data)]
    }

    if (child.visible(data)) {
      accumulator.push(child.id)
    }
    return accumulator
  }, [])
}

const doesChapterHaveUndefinedValues = (chapter, data) => {
  const fields = getAllFields(chapter, data)
  return fields.some(id => data[id] === undefined || data[id].length === 0)
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
