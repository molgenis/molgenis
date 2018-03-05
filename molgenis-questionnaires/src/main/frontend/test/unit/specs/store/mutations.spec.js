import mutations from 'src/store/mutations'

describe('mutations', () => {
  const state = {
    questionnaireList: [],
    questionnaireId: '',
    chapterFields: [],
    formData: {},
    questionnaireLabel: '',
    questionnaireDescription: '',
    questionnaireRowId: '',
    submissionText: ''
  }

  describe('SET_QUESTIONNAIRE_LIST', () => {
    it('should update the questionnaireList in the state with the payload', () => {
      const payload = ['questionnaire']
      mutations.SET_QUESTIONNAIRE_LIST(state, payload)

      expect(state.questionnaireList).to.deep.equal(payload)
    })
  })

  describe('SET_CHAPTER_FIELDS', () => {
    it('should update the chapterFields in the state with the payload', () => {
      const payload = ['chapter1']
      mutations.SET_CHAPTER_FIELDS(state, payload)

      expect(state.chapterFields).to.deep.equal(payload)
    })
  })

  describe('SET_FORM_DATA', () => {
    it('should update the formData in the state with the payload', () => {
      const payload = {status: 'OPEN'}
      mutations.SET_FORM_DATA(state, payload)

      expect(state.formData).to.deep.equal(payload)
    })
  })

  describe('UPDATE_FORM_STATUS', () => {
    it('should update the form status in the state with the payload', () => {
      const payload = 'SUBMITTED'
      mutations.UPDATE_FORM_STATUS(state, payload)

      expect(state.formData.status).to.deep.equal(payload)
    })
  })

  describe('SET_QUESTIONNAIRE_ID', () => {
    it('should update the questionnaireId in the state with the payload', () => {
      const payload = 'id'
      mutations.SET_QUESTIONNAIRE_ID(state, payload)

      expect(state.questionnaireId).to.deep.equal(payload)
    })
  })

  describe('SET_QUESTIONNAIRE_LABEL', () => {
    it('should update the questionnaireLabel in the state with the payload', () => {
      const payload = 'label'
      mutations.SET_QUESTIONNAIRE_LABEL(state, payload)

      expect(state.questionnaireLabel).to.deep.equal(payload)
    })
  })

  describe('SET_QUESTIONNAIRE_DESCRIPTION', () => {
    it('should update the questionnaireDescription in the state with the payload', () => {
      const payload = 'description'
      mutations.SET_QUESTIONNAIRE_DESCRIPTION(state, payload)

      expect(state.questionnaireDescription).to.deep.equal(payload)
    })
  })

  describe('SET_QUESTIONNAIRE_ROW_ID', () => {
    it('should update the questionnaireRowId in the state with the payload', () => {
      const payload = 'rowId'
      mutations.SET_QUESTIONNAIRE_ROW_ID(state, payload)

      expect(state.questionnaireRowId).to.deep.equal(payload)
    })
  })

  describe('SET_SUBMISSION_TEXT', () => {
    it('should update the submissionText in the state with the payload', () => {
      const payload = 'submissionText'
      mutations.SET_SUBMISSION_TEXT(state, payload)

      expect(state.submissionText).to.deep.equal(payload)
    })
  })

  describe('CLEAR_STATE', () => {
    it('should clear the state', () => {
      mutations.CLEAR_STATE(state)

      expect(state.questionnaireId).to.deep.equal('')
      expect(state.chapterFields).to.deep.equal([])
      expect(state.formData).to.deep.equal({})
      expect(state.questionnaireLabel).to.deep.equal('')
      expect(state.questionnaireDescription).to.deep.equal('')
      expect(state.questionnaireRowId).to.deep.equal('')
      expect(state.submissionText).to.deep.equal('')
    })
  })
})
