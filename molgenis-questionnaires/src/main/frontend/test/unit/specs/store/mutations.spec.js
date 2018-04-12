import mutations from 'src/store/mutations'

describe('mutations', () => {
  const state = {
    chapterFields: [],
    error: '',
    formData: {},
    loading: true,
    navigationBlocked: false,
    questionnaire: {},
    questionnaireDescription: '',
    questionnaireId: '',
    questionnaireLabel: '',
    questionnaireList: [],
    questionnaireRowId: '',
    submissionText: '',
    numberOfOutstandingCalls: 0
  }

  describe('BLOCK_NAVIGATION', () => {
    it('should update the navigationBlocked in the state with the payload', () => {
      const payload = true
      mutations.BLOCK_NAVIGATION(state, payload)

      expect(state.navigationBlocked).to.equal(payload)
    })
  })

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

  describe('SET_MAPPER_OPTIONS', () => {
    it('should update the mapperOptions in the state with the payload', () => {
      const payload = {
        booleanLabels: {
          'true': 'Yes'
        }
      }
      mutations.SET_MAPPER_OPTIONS(state, payload)

      expect(state.mapperOptions).to.deep.equal(payload)
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
      expect(state.loading).to.equal(true)
      expect(state.error).to.equal('')
      expect(state.questionnaire).to.deep.equal({})
    })
  })

  describe('SET_ERROR', () => {
    it('should update the error in the state with the payload', () => {
      const payload = 'error'
      mutations.SET_ERROR(state, payload)

      expect(state.error).to.equal(payload)
    })
  })

  describe('SET_LOADING', () => {
    it('should update loading status in the state with the payload', () => {
      const payload = true
      mutations.SET_LOADING(state, payload)

      expect(state.loading).to.equal(payload)
    })
  })

  describe('SET_QUESTIONNAIRE', () => {
    it('should update the questionnaire in the state with the payload', () => {
      const payload = 'questionnaire'
      mutations.SET_QUESTIONNAIRE(state, payload)

      expect(state.questionnaire).to.equal(payload)
    })
  })

  describe('INCREMENT_SAVING_QUEUE', () => {
    it('should increment the number of outstanding calls queue', () => {
      state.numberOfOutstandingCalls = 0
      mutations.INCREMENT_SAVING_QUEUE(state)

      expect(state.numberOfOutstandingCalls).to.equal(1)
    })
  })

  describe('DECREMENT_SAVING_QUEUE', () => {
    it('should decrement the number of outstanding calls queue', () => {
      state.numberOfOutstandingCalls = 3
      mutations.DECREMENT_SAVING_QUEUE(state)

      expect(state.numberOfOutstandingCalls).to.equal(2)
    })
  })
})
