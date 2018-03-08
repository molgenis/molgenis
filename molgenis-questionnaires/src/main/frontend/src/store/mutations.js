const mutations = {
  'SET_QUESTIONNAIRE_OVERVIEW' (state, questionnaireOverview) {
    state.questionnaireOverview = questionnaireOverview
  },

  'SET_QUESTIONNAIRE_LIST' (state, questionnaireList) {
    state.questionnaireList = questionnaireList
  },

  'SET_CHAPTER_FIELDS' (state, chapterFields) {
    state.chapterFields = chapterFields
  },

  'SET_FORM_DATA' (state, formData) {
    state.formData = formData
  },

  'UPDATE_FORM_STATUS' (state, status) {
    state.formData.status = status
  },

  'SET_QUESTIONNAIRE_ID' (state, questionnaireId) {
    state.questionnaireId = questionnaireId
  },

  'SET_QUESTIONNAIRE_LABEL' (state, questionnaireLabel) {
    state.questionnaireLabel = questionnaireLabel
  },

  'SET_QUESTIONNAIRE_DESCRIPTION' (state, questionnaireDescription) {
    state.questionnaireDescription = questionnaireDescription
  },

  'SET_QUESTIONNAIRE_ROW_ID' (state, questionnaireRowId) {
    state.questionnaireRowId = questionnaireRowId
  },

  'SET_SUBMISSION_TEXT' (state, submissionText) {
    state.submissionText = submissionText
  },

  'SET_MAPPER_OPTIONS' (state, mapperOptions) {
    state.mapperOptions = mapperOptions
  },

  'CLEAR_STATE' (state) {
    state.questionnaireId = ''
    state.chapterFields = []
    state.formData = {}
    state.questionnaireLabel = ''
    state.questionnaireDescription = ''
    state.questionnaireRowId = ''
    state.submissionText = ''
    state.loading = true
  },

  'SET_ERROR' (state, error) {
    state.error = error
  },

  'SET_LOADING' (state, loading) {
    state.loading = loading
  }
}

export default mutations
