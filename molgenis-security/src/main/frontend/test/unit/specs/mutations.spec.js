import mutations from 'store/mutations'

describe('mutations', () => {
  describe('SET_MESSAGE', () => {
    it('should set the message in the state', () => {
      const state = {
        message: 'initial message'
      }

      mutations.__SET_MESSAGE__(state, 'updated message')
      expect(state.message).to.equal('updated message')
    })
  })
})
