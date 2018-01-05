import mutations from '@/store/mutations'

describe('mutations', () => {
  describe('SET_FORM_FIELDS', () => {
    it('should set setting the response in the state', () => {
      const state = {
        formFields: [],
        formData: {}
      }

      const formFields = [
        {id: 's1'}
      ]

      const formData = {
        $1: 'test 1'
      }

      mutations.__SET_FORM_FIELDS__(state, formFields)
      mutations.__SET_FORM_DATA__(state, formData)
      expect(state.formFields).to.deep.equal(formFields)
      expect(state.formData).to.deep.equal(formData)
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
