// @flow
import type { QuestionnaireState } from '../flow.types.js'

const mutations = {
  'BLOCK_NAVIGATION' (state: QuestionnaireState, block: boolean) {
    state.navigationBlocked = block
  },

  'CLEAR_STATE' (state: QuestionnaireState) {
    state.chapterFields = []
    state.error = ''
    state.formData = {}
    state.loading = true
    state.navigationBlocked = false
    state.questionnaire = {}
    state.questionnaireRowId = ''
    state.submissionText = ''
  },

  'INCREMENT_SAVING_QUEUE' (state: QuestionnaireState) {
    state.numberOfOutstandingCalls += 1
  },

  'DECREMENT_SAVING_QUEUE' (state: QuestionnaireState) {
    state.numberOfOutstandingCalls -= 1
  },

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

  'SET_QUESTIONNAIRE_ROW_ID' (state: QuestionnaireState, questionnaireRowId: string) {
    state.questionnaireRowId = questionnaireRowId
  },

  'SET_SUBMISSION_TEXT' (state: QuestionnaireState, submissionText: string) {
    state.submissionText = submissionText
  },

  'SET_MAPPER_OPTIONS' (state: QuestionnaireState, mapperOptions: Object) {
    state.mapperOptions = {...state.mapperOptions, ...mapperOptions}
  },

  'SET_ERROR' (state: QuestionnaireState, error: string) {
    state.error = error
  },

  'SET_LOADING' (state: QuestionnaireState, loading: boolean) {
    state.loading = loading
  },

  'UPDATE_FORM_STATUS' (state: QuestionnaireState, status: string) {
    state.formData.status = status
  }
}

export default mutations
