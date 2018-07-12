import GroupDetail from '../../../../src/components/GroupDetail'
import { createLocalVue, shallowMount } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {}
  return translations[key]
}

describe('GroupDetail component', () => {
  let getters
  let mutations
  let actions
  let localVue
  let state
  let store

  let pushedRoute = {}
  const $router = {
    push: function (pushed) {
      pushedRoute = pushed
    }
  }
  const $route = {
    path: '/group'
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

  const groupPermissions = {
    group1: ['ADD_MEMBERSHIP']
  }

  const loginUser = {
    name: 'admin',
    isSuperUser: true
  }

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    state = {
      loginUser: loginUser,
      groups: [],
      groupMembers: groupMembers,
      groupRoles: {},
      groupPermissions: {group1: ['ADD_MEMBERSHIP']},
      users: [],
      toast: null
    }

    actions = {
      fetchGroupMembers: () => td.function(),
      fetchGroupPermissions: () => td.function()
    }

    getters = {
      groupMembers: () => groupMembers,
      groupPermissions: () => groupPermissions
    }

    mutations = {
      clearToast: td.function()
    }

    store = new Vuex.Store({state, actions, getters, mutations})
  })

  const stubs = ['router-link', 'router-view']

  describe('When created', () => {
    it('should return the groupMembers via a getter', () => {
      const wrapper = shallowMount(GroupDetail, {
        propsData: {
          name: 'group1'
        },
        mocks: { $router, $route },
        store,
        stubs,
        localVue
      })
      expect(wrapper.vm.groupMembers).to.deep.equal(groupMembers)
    })

    it('should return determine if the user can add a member', () => {
      const wrapper = shallowMount(GroupDetail, {
        propsData: {
          name: 'group1'
        },
        mocks: { $router, $route },
        store,
        stubs,
        localVue
      })
      expect(wrapper.vm.canAddMember).to.deep.equal(true)
    })
  })

  describe('when the add member button is clicked', () => {
    it('should navigate to the add member page', () => {
      const wrapper = shallowMount(GroupDetail, {
        propsData: {
          name: 'group1'
        },
        mocks: { $router, $route },
        store,
        stubs,
        localVue
      })
      wrapper.find('#add-member-btn').trigger('click')
      expect(pushedRoute).to.deep.equal({name: 'addMember', params: {groupName: 'group1'}})
      td.verify(mutations.clearToast(state, undefined))
    })
  })
})
