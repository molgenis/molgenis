import mutations from '../../../../src/store/mutations'

describe('mutations', () => {
  describe('setGroups', () => {
    it('should set an list of groups in the store', () => {
      const state = {
        groups: []
      }

      const payload = [
        {name: 'group1', label: 'group 1'},
        {name: 'group2', label: 'group 2'}
      ]

      mutations.setGroups(state, payload)

      expect(state.groups).to.deep.equal(payload)
    })
  })
  describe('clearToast', () => {
    it('should clears the toast message', () => {
      const state = {
        toast: {
          type: 'danger',
          message: 'How do you do'
        }
      }

      mutations.clearToast(state)

      expect(state.toast).to.deep.equal(null)
    })
  })
  describe('setToast', () => {
    it('should set the toast in the store', () => {
      const state = {
        toast: {}
      }

      const payload = [
        {type: 'danger', message: 'How do you do'}
      ]

      mutations.setToast(state, payload)

      expect(state.toast).to.deep.equal(payload)
    })
  })
})
