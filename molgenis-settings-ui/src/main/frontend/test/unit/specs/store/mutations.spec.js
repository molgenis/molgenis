import mutations from '@/store/mutations'

describe('mutations', () => {
  describe('SET_FORM_FIELDS', () => {
    it('should set setting the response in the state', () => {
      const state = {
        formFields: []
      }

      const formFields = [
        {id: 's1'}
      ]

      mutations.__SET_FORM_FIELDS__(state, formFields)
      expect(state.formFields).to.deep.equal(formFields)
    })
  })

  describe('SET_ERROR', () => {
    it('should set an occurred error', () => {
      const state = {
        error: []
      }

      const error = []

      mutations.__SET_ERROR__(state, error)
      expect(state.error).to.deep.equal(error)
    })
  })
})
