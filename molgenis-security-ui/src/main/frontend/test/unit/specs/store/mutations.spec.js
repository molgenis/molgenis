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
  describe('setGroupMembers', (done) => {
    it('should set an list members in the groupsMembers map', () => {
      const state = {
        groupMembers: {}
      }

      const payload = {
        groupsName: 'my-group',
        groupsMembers: [
          { userId: 'abc-123',
            username: 'user1',
            roleName: 'VIEWER',
            roleLabel: 'Viewer'
          }
        ]
      }

      mutations.setGroupMembers(state, payload)
      Vue.nextTick(() => {
        expect(state.groupMembers[payload.groupsName]).to.deep.equal(payload.groupsMembers)
        done()
      })
    })
  })
  describe('setGroupRoles', () => {
    it('should set an list members in the groupsMembers map', (done) => {
      const state = {
        groupRoles: {}
      }

      const payload = {
        groupsName: 'my-group',
        groupsRoles: [
          { roleName: 'role-name',
            roleLabel: 'ROLE'
          }
        ]
      }

      mutations.setGroupRoles(state, payload)
      Vue.nextTick(() => {
        expect(state.groupRoles[payload.groupsName]).to.deep.equal(payload.groupsMembers)
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
