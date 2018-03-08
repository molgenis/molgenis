const getAllVisibleFieldIds = (chapter, data) => {
  return chapter.children.reduce((accumulator, child) => {
    // console.log(child.id)
    if (child.type === 'field-group') {
      return [...accumulator, ...getAllVisibleFieldIds(child, data)]
    }

    if (child.visible(data)) {
      accumulator.push(child.id)
    }
    return accumulator
  }, [])
}

const getters = {
  getChapterByIndex: (state) => (index) => {
    return state.chapterFields[index - 1]
  },

  getChapterNavigationList: state => {
    return state.chapterFields.map((chapter, index) => ({id: chapter.id, label: chapter.label, index: (index + 1)}))
  },

  getTotalNumberOfChapters: state => {
    return state.chapterFields.length
  },

  getVisibleFieldIdsForAllChapters: state => {
    return state.chapterFields.reduce((accumulator, chapter) => {
      const visibleFields = getAllVisibleFieldIds(chapter, state.formData)
      accumulator[chapter.id] = visibleFields

      return accumulator
    }, {})
  },

  getQuestionnaireId: state => {
    return state.questionnaire.meta && state.questionnaire.meta.name
  },

  getQuestionnaireLabel: state => {
    return state.questionnaire.meta && state.questionnaire.meta.label
  },

  getQuestionnaireDescription: state => {
    return state.questionnaire.meta && state.questionnaire.meta.description
  }
}

export default getters
