import getters from '../../../../src/store/getters'

describe('getters', () => {
  describe('getLoginUser', () => {
    it('should return a user', () => {
      const state = {
        loginUser: {
          name: 'admin',
          isSuperUser: true
        }
      }
      expect(getters.getLoginUser(state)).to.deep.equal(state.loginUser)
    })
  })

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

  describe('groupMembers', () => {
    it('should return groupsMembers map from the store', () => {
      const state = {
        groupMembers: {
          myGroup: [
            {
              userId: 'abc-123',
              username: 'user1',
              roleName: 'VIEWER',
              roleLabel: 'Viewer'
            }
          ]
        }
      }
      expect(getters.groupMembers(state)).to.deep.equal(state.groupMembers)
    })
  })

  describe('groupRoles', () => {
    it('should return groupRoles map from the store', () => {
      const state = {
        groupRoles: {
          myGroup: [
            {
              roleName: 'VIEWER',
              roleLabel: 'Viewer'
            }
          ]
        }
      }
      expect(getters.groupRoles(state)).to.deep.equal(state.groupRoles)
    })
  })

  describe('groupPermissions', () => {
    it('should return groupPermissions map from the store', () => {
      const state = {
        groupPermissions: {
          myGroup: ['ADD_MEMBERSHIP', 'REMOVE_MEMBERSHIP']
        }
      }
      expect(getters.groupPermissions(state)).to.deep.equal(state.groupPermissions)
    })
  })

  describe('users', () => {
    it('should return groups from the store', () => {
      const state = {
        users: [
          {id: 'a', username: 'user1'},
          {id: 'b', username: 'user2'}
        ]
      }
      expect(getters.users(state)).to.deep.equal(state.users)
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
})
