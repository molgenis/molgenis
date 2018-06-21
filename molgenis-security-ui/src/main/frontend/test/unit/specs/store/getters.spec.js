import getters from '../../../../src/store/getters'

describe('getters', () => {
  describe('groups', () => {
    it('should return groups from the store', () => {
      const state = {
        groups: [
          {name: 'group1', label: 'group 1'},
          {name: 'group2', label: 'group 2'}
        ]
      }
      expect(getters.groups(state)).to.deep.equal(state.groups)
    })
  })
  describe('toast', () => {
    it('should return the toast object', () => {
      const state = {
        toast: {type: 'danger', message: 'how do you do'}
      }
      expect(getters.toast(state)).to.deep.equal(state.toast)
    })
  })
  describe('getUser', () => {
    it('should return a user', () => {
      const state = {
        user: {
          name: 'admin',
          isSuperUser: true
        }
      }
      expect(getters.getUser(state)).to.deep.equal(state.user)
    })
  })
})
