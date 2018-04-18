// @flow
import type { QuestionnaireState } from '../flow.types.js'

const isFilledInValue = (value): boolean => {
  if (value === undefined) return false
  if (Array.isArray(value) && value.length === 0) return false
  return value !== ''
}

const isChapterComplete = (chapter: Object, formData: Object): boolean => {
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

const getTotalNumberOfFieldsForChapter = (chapter, formData) => {
  return chapter.children.reduce((accumulator, child) => {
    if (child.type === 'field-group') {
      return accumulator + getTotalNumberOfFieldsForChapter(child, formData)
    }

    if (child.visible(formData)) {
      accumulator++
    }

    return accumulator
  }, 0)
}

const getNumberOfFilledInFieldsForChapter = (chapter, formData) => {
  return chapter.children.reduce((accumulator, child) => {
    if (child.type === 'field-group') {
      return accumulator + getNumberOfFilledInFieldsForChapter(child, formData)
    }

    if (isFilledInValue(formData[child.id])) {
      accumulator++
    }

    return accumulator
  }, 0)
}

const getters = {
  getChapterByIndex: (state: QuestionnaireState): Function => (index: number) => {
    return state.chapterFields[index - 1]
  },

  getChapterCompletion: (state: QuestionnaireState): Object => {
    return state.chapterFields.reduce((accumulator, chapter) => {
      accumulator[chapter.id] = isChapterComplete(chapter, state.formData)
      return accumulator
    }, {})
  },

  getChapterNavigationList: (state: QuestionnaireState): Array<*> => {
    return state.chapterFields.map((chapter, index) => ({id: chapter.id, label: chapter.label, index: (index + 1)}))
  },

  getChapterProgress: (state: QuestionnaireState): Object => {
    return state.chapterFields.reduce((accumulator, chapter) => {
      const totalNumberOfFieldsInChapter = getTotalNumberOfFieldsForChapter(chapter, state.formData)
      const numberOfFilledInFieldsInChapter = getNumberOfFilledInFieldsForChapter(chapter, state.formData)

      accumulator[chapter.id] = (numberOfFilledInFieldsInChapter / totalNumberOfFieldsInChapter) * 100
      return accumulator
    }, {})
  },

  getQuestionnaireDescription: (state: QuestionnaireState): string => {
    return state.questionnaire.meta && state.questionnaire.meta.description
  },

  getQuestionnaireId: (state: QuestionnaireState): string => {
    return state.questionnaire.meta && state.questionnaire.meta.name
  },

  getQuestionnaireLabel: (state: QuestionnaireState): string => {
    return state.questionnaire.meta && state.questionnaire.meta.label
  },

  getTotalNumberOfChapters: (state: QuestionnaireState): number => {
    return state.chapterFields.length
  },

  isSaving: (state: QuestionnaireState): boolean => {
    return state.numberOfOutstandingCalls > 0
  }
}

export default getters
