import mutations from '@/store/mutations'

describe('mutations', () => {
  describe('SET_RAW_SETTINGS', () => {
    it('should set setting the response in the state', () => {
      const state = {
        rawSettings: []
      }

      const rawSettings = [
        {id: 's1'}
      ]

      mutations.__SET_RAW_SETTINGS__(state, rawSettings)
      expect(state.rawSettings).to.deep.equal(rawSettings)
    })
  })

  describe('SET_SETTINGS', () => {
    it('should set the parsed settings', () => {
      const state = {
        settings: []
      }

      const settings = [
        {id: 's1'}
      ]

      mutations.__SET_SETTINGS__(state, settings)
      expect(state.settings).to.deep.equal(settings)
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
