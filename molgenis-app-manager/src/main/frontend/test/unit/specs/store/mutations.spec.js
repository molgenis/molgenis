import mutations from 'src/store/mutations'

describe('mutations', () => {
  describe('SET_ERROR', () => {
    it('should set the error variable in the state with the payload', () => {
      const state = {error: ''}
      mutations.SET_ERROR(state, 'new error!')
      expect(state.error).to.equal('new error!')
    })
  })

  describe('SET_LOADING', () => {
    it('should set the loading variable in the state with the payload', () => {
      const state = {loading: true}
      mutations.SET_LOADING(state, false)
      expect(state.loading).to.equal(false)
    })
  })

  describe('UPDATE_APPS', () => {
    it('should set the apps variable in the state with the payload', () => {
      const state = {apps: []}
      mutations.UPDATE_APPS(state, [{name: 'app1'}, {name: 'app2'}])
      expect(state.apps).to.deep.equal([{name: 'app1'}, {name: 'app2'}])
    })
  })
})
