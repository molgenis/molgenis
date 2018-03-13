// @flow
import type { QuestionnaireState } from 'src/flow.types.js'

const getAllVisibleFieldIds = (chapter: Object, data: Object) => {
  return chapter.children.reduce((accumulator, child) => {
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
  getChapterByIndex: (state: QuestionnaireState): Function => (index: number) => {
    return state.chapterFields[index - 1]
  },

  getChapterNavigationList: (state: QuestionnaireState): Array<*> => {
    return state.chapterFields.map((chapter, index) => ({id: chapter.id, label: chapter.label, index: (index + 1)}))
  },

  getTotalNumberOfChapters: (state: QuestionnaireState): number => {
    return state.chapterFields.length
  },

  getVisibleFieldIdsForAllChapters: (state: QuestionnaireState): Object => {
    return state.chapterFields.reduce((accumulator, chapter) => {
      const visibleFields = getAllVisibleFieldIds(chapter, state.formData)
      accumulator[chapter.id] = visibleFields

      return accumulator
    }, {})
  },

  getQuestionnaireId: (state: QuestionnaireState): string => {
    return state.questionnaire.meta && state.questionnaire.meta.name
  },

  getQuestionnaireLabel: (state: QuestionnaireState): string => {
    return state.questionnaire.meta && state.questionnaire.meta.label
  },

  getQuestionnaireDescription: (state: QuestionnaireState): string => {
    return state.questionnaire.meta && state.questionnaire.meta.description
  }
}

export default getters
