import mutations from '@/store/mutations'

describe('mutations', () => {
  describe('SET_FORM', () => {
    it('should set setting-response in the state', () => {
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

  describe('SET_SETTINGS', () => {
    it('should set settings list in state', () => {
      const state = {
        settings: []
      }

      const settings = [
        {id: 'setting_entity_1'}
      ]

      mutations.__SET_SETTINGS__(state, settings)
      expect(state.settings).to.deep.equal(settings)
    })
  })

  describe('UPDATE_SETTINGS', () => {
    it('should set an occurred error', () => {
      const state = {
        error: []
      }

      const error = []

      mutations.__UPDATE_SETTINGS__(state, error)
      expect(state.error).to.deep.equal(error)
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
  describe('SET_MESSAGE', () => {
    it('should set a message from server or validation library', () => {
      const state = {
        message: ''
      }

      const message = ''

      mutations.__SET_MESSAGE__(state, message)
      expect(state.message).to.deep.equal(message)
    })
  })
})
