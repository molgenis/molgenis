// @flow
import type { QuestionnaireState } from '../flow.types.js'

const isFilledInValue = (value) => {
  if (value === undefined) return false
  if (Array.isArray(value) && value.length === 0) return false
  return value !== ''
}

const isChapterComplete = (chapter, formData) => {
  return chapter.children.every(child => {
    if (child.type === 'field-group') {
      return isChapterComplete(child, formData)
    }

    const visible = child.visible(formData)
    if (!visible) return true

    const value = formData[child.id]
    const filledInValue = isFilledInValue(value)
    const required = child.required(formData)
    const valid = child.validate(formData)

    if (filledInValue) {
      const inRange = child.range ? value => child.range.min && value <= child.range.max : true
      return valid && inRange
    } else {
      return valid && !required
    }
  })
}

const getters = {
  getChapterCompletion: (state: QuestionnaireState): Object => {
    return state.chapterFields.reduce((accumulator, chapter) => {
      accumulator[chapter.id] = isChapterComplete(chapter, state.formData)
      return accumulator
    }, {})
  },

  getChapterByIndex: (state: QuestionnaireState): Function => (index: number) => {
    return state.chapterFields[index - 1]
  },

  getChapterNavigationList: (state: QuestionnaireState): Array<*> => {
    return state.chapterFields.map((chapter, index) => ({id: chapter.id, label: chapter.label, index: (index + 1)}))
  },

  getTotalNumberOfChapters: (state: QuestionnaireState): number => {
    return state.chapterFields.length
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
