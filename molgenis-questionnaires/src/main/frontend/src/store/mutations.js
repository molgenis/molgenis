// @flow
import type { QuestionnaireState } from 'src/flow.types.js'

const mutations = {
  'SET_QUESTIONNAIRE' (state: QuestionnaireState, questionnaire: Object) {
    state.questionnaire = questionnaire
  },

  'SET_QUESTIONNAIRE_LIST' (state: QuestionnaireState, questionnaireList: Array<*>) {
    state.questionnaireList = questionnaireList
  },

  'SET_CHAPTER_FIELDS' (state: QuestionnaireState, chapterFields: Array<*>) {
    state.chapterFields = chapterFields
  },

  'SET_FORM_DATA' (state: QuestionnaireState, formData: Object) {
    state.formData = formData
  },

  'UPDATE_FORM_STATUS' (state: QuestionnaireState, status: string) {
    state.formData.status = status
  },

  'SET_QUESTIONNAIRE_ROW_ID' (state: QuestionnaireState, questionnaireRowId: string) {
    state.questionnaireRowId = questionnaireRowId
  },

  'SET_SUBMISSION_TEXT' (state: QuestionnaireState, submissionText: string) {
    state.submissionText = submissionText
  },

  'SET_MAPPER_OPTIONS' (state: QuestionnaireState, mapperOptions: Object) {
    state.mapperOptions = mapperOptions
  },

  'CLEAR_STATE' (state: QuestionnaireState) {
    state.questionnaireId = ''
    state.chapterFields = []
    state.formData = {}
    state.questionnaireLabel = ''
    state.questionnaireDescription = ''
    state.questionnaireRowId = ''
    state.submissionText = ''
    state.loading = true
    state.error = ''
    state.questionnaire = {}
  },

  'SET_ERROR' (state: QuestionnaireState, error: string) {
    state.error = error
  },

  'SET_LOADING' (state: QuestionnaireState, loading: boolean) {
    state.loading = loading
  }
}

export default mutations
