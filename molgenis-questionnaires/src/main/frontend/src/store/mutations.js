const mutations = {
  'SET_QUESTIONNAIRE' (state, questionnaire) {
    state.questionnaire = questionnaire
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
    state.error = ''
    state.questionnaire = {}
  },

  'SET_ERROR' (state, error) {
    state.error = error
  },

  'SET_LOADING' (state, loading) {
    state.loading = loading
  }
}

export default mutations
