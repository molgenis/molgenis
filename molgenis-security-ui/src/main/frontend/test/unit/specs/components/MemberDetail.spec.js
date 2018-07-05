import MemberDetail from '../../../../src/components/MemberDetail'
import {createLocalVue, shallowMount} from '@vue/test-utils'
import Vuex from 'vuex'

const localVue = createLocalVue()

localVue.use(Vuex)

let pushedRoute = {}
const $router = {
  push: function (pushed) {
    pushedRoute = pushed
  }
}
const $route = {
  path: '/group/my-group'
}

describe('MemberDetail component', () => {
  let getters
  let mutations
  let actions
  let state
  let store

  const stubs = ['router-link', 'router-view']

  const groupRoles = {
    group1: [
      {
        roleName: 'VIEWER',
        roleLabel: 'Viewer'
      },
      {
        roleName: 'MANAGER',
        roleLabel: 'Manager'
      },
      {
        roleName: 'EDITOR',
        roleLabel: 'Editor'
      }
    ]
  }

  const groupMembers = {
    group1: [
      {
        userId: 'user-1',
        username: 'bert',
        roleName: 'VIEWER',
        roleLabel: 'Viewer'
      },
      {
        userId: 'user-2',
        username: 'ans',
        roleName: 'MANAGER',
        roleLabel: 'Manager'
      }
    ]
  }

  state = {}

  actions = {
    fetchGroupMembers: sinon.spy(),
    fetchGroupRoles: sinon.spy(),
    removeMember: sinon.spy(),
    updateMember: sinon.spy(),
  }

  getters = {
    groupRoles: () => groupRoles,
    groupMembers: () => groupMembers
  }

  mutations = {}

  store = new Vuex.Store({state, actions, getters, mutations})

  describe('created', () => {
    let propsData = {
      groupName: 'group1',
      memberName: 'member1'
    }

    beforeEach(() => {
      shallowMount(MemberDetail, {propsData, store, stubs, localVue})
    })

    it('should fetch the group members', () => {
      expect(actions.fetchGroupMembers.called).to.equal(true)
    })

    it('should fetch the group roles', () => {
      expect(actions.fetchGroupRoles.called).to.equal(true)
    })
  })

  describe('getters', () => {
    let wrapper
    let propsData = {
      groupName: 'group1',
      memberName: 'ans'
    }

    beforeEach(() => {
      wrapper = shallowMount(MemberDetail, {propsData, store, stubs, localVue})
    })

    it('should return the groupRoles via a getter', () => {
      expect(wrapper.vm.member).to.deep.equal(groupMembers.group1[1])
    })

    it('should return the sortedRoles via a getter', () => {
      const actual = wrapper.vm.sortedRoles[0].roleLabel
      const expected = 'Editor'
      expect(actual).to.deep.equal(expected)
    })
  })

  describe('methods', () => {
    let wrapper
    let propsData = {
      groupName: 'group1',
      memberName: 'ans'
    }

    beforeEach(() => {
      wrapper = shallowMount(MemberDetail,
        {
          propsData,
          mocks: {$router, $route},
          store,
          stubs,
          localVue
        })
    })

    it('onEditRole should set the selected role and activate the edit mode', () => {
      expect(wrapper.vm.selectedRole).to.equal('')
      expect(wrapper.vm.isEditRoleMode).to.equal(false)
      wrapper.vm.onEditRole()
      expect(wrapper.vm.selectedRole).to.equal('MANAGER')
      expect(wrapper.vm.isEditRoleMode).to.equal(true)
    })

    it('onRemoveMember should call the removeMember action', () => {
      expect(wrapper.vm.isRemoving).to.equal(false)
      wrapper.vm.onRemoveMember()
      expect(wrapper.vm.isRemoving).to.equal(true)
      expect(actions.removeMember.called).to.equal(true)
    })

    it('onUpdateMember should call the updateMember action', () => {
      expect(wrapper.vm.isUpdating).to.equal(false)
      wrapper.vm.onUpdateMember()
      expect(wrapper.vm.isUpdating).to.equal(true)
      expect(actions.updateMember.called).to.equal(true)
    })

  })


})
