import mutations from '../../../../src/store/mutations'
import Vue from 'vue'

describe('mutations', () => {
  describe('setLoginUser', () => {
    it('should set the loginUser in the store', () => {
      const state = {
        loginUser: {}
      }

      const payload = [
        {name: 'admin', isSuperUSer: true}
      ]

      mutations.setLoginUser(state, payload)

      expect(state.loginUser).to.deep.equal(payload)
    })
  })
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
  describe('setUsers', () => {
    it('should set an list of users in the store', () => {
      const state = {
        users: []
      }

      const payload = [
        {id: 'a', username: 'john'},
        {id: 'b', username: 'paul'}
      ]

      mutations.setUsers(state, payload)

      expect(state.users).to.deep.equal(payload)
    })
  })
  describe('setGroupMembers', () => {
    let state = {
      groupMembers: {}
    }

    const payload = {
      groupName: 'my-group',
      groupMembers: [
        {
          userId: 'abc-123',
          username: 'user1',
          roleName: 'VIEWER',
          roleLabel: 'Viewer'
        }
      ]
    }

    beforeEach(function () {
      mutations.setGroupMembers(state, payload)
    })

    it('should set an list members in the groupsMembers map', (done) => {
      Vue.nextTick(() => {
        expect(state.groupMembers[payload.groupName]).to.deep.equal(payload.groupMembers)
        done()
      })
    })
  })
  describe('setGroupRoles', () => {
    let state = {
      groupRoles: {}
    }

    const payload = {
      groupName: 'my-group',
      groupRoles: [
        {
          roleName: 'role-name',
          roleLabel: 'ROLE'
        }
      ]
    }

    beforeEach(function () {
      mutations.setGroupRoles(state, payload)
    })

    it('should set an list of roles in the groupsRoles map', (done) => {
      Vue.nextTick(() => {
        expect(state.groupRoles[payload.groupName]).to.deep.equal(payload.groupRoles)
        done()
      })
    })
  })
  describe('setGroupPermissions', () => {
    let state = {
      groupPermissions: {}
    }

    const payload = {
      groupName: 'my-group',
      groupPermissions: [
        ['ADD_MEMBERSHIP', 'REMOVE_MEMBERSHIP']
      ]
    }

    beforeEach(function () {
      mutations.setGroupPermissions(state, payload)
    })

    it('should set an list of permissions in the groupPermissions map', (done) => {
      Vue.nextTick(() => {
        expect(state.groupPermissions[payload.groupName]).to.deep.equal(payload.groupPermissions)
        done()
      })
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
