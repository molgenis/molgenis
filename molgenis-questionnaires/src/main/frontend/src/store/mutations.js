const mutations = {
  'SET_QUESTIONNAIRE_LIST' (state, questionnaireList) {
    state.questionnaireList = questionnaireList
  },

  'SET_CHAPTER_FIELDS' (state, chapterFields) {
    state.chapterFields = chapterFields
  },

  'SET_FORM_DATA' (state, formData) {
    // formData contains either the entire set of data or a single parameter with updated values
    state.formData = Object.assign(state.formData, formData)
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
  }
}

export default mutations
