import MemberAdd from '../../../../src/components/MemberAdd'
import {createLocalVue, shallowMount} from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {}
  return translations[key]
}

const localVue = createLocalVue()

localVue.use(Vuex)
localVue.filter('i18n', $t)

describe('MemberAdd component', () => {
  let getters
  let mutations
  let actions
  let state
  let store

  let pushedRoute = {}
  const $router = {
    push: function (pushed) {
      pushedRoute = pushed
    }
  }
  const $route = {
    path: '/group/my-group'
  }

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

  const users = [
    {
      id: 'user-1',
      username: 'bert'
    },
    {
      id: 'user-2',
      username: 'ans'
    },
    {
      id: 'user-3',
      username: 'nick'
    }
  ]

  describe('after created', () => {
    beforeEach(() => {
      td.reset()

      state = {}

      actions = {
        tempFetchUsers: () => td.func(),
        fetchGroupRoles: () => td.func(),
        addMember: sinon.spy()
      }

      getters = {
        groupRoles: () => groupRoles,
        users: () => users,
        groupMembers: () => groupMembers
      }

      mutations = {
        setLoginUser: td.function(),
        clearToast: td.function()
      }

      store = new Vuex.Store({state, actions, getters, mutations})
    })

    const stubs = ['router-link', 'router-view']

    it('should return the groupRoles via a getter', () => {
      const wrapper = shallowMount(MemberAdd, {store, stubs, localVue})
      expect(wrapper.vm.groupRoles).to.deep.equal(groupRoles)
    })

    it('should return the users via a getter', () => {
      const wrapper = shallowMount(MemberAdd, {store, stubs, localVue})
      expect(wrapper.vm.users).to.deep.equal(users)
    })

    it('should return the groupMembers via a getter', () => {
      const wrapper = shallowMount(MemberAdd, {store, stubs, localVue})
      expect(wrapper.vm.groupMembers).to.deep.equal(groupMembers)
    })

    describe('on add member', () => {
      it('should add a member', (done) => {
        const wrapper = shallowMount(MemberAdd, {
          propsData: {
            groupName: 'group1'
          },
          mocks: {$router, $route},
          store,
          stubs,
          localVue
        })

        wrapper.setData({
          username: 'user-3',
          roleName: 'EDITOR',
          isAdding: false
        })

        wrapper.vm.onSubmit()

        localVue.nextTick(() => {
          expect(pushedRoute).to.deep.equal({name: 'groupDetail', params: {name: 'group1'}})
          const expectedPayload = {
            groupName: 'group1',
            addMemberCommand: {
              username: 'user-3',
              roleName: 'EDITOR'
            }
          }
          expect(actions.addMember.called).to.equal(true)
          expect(actions.addMember.getCall(0).args[1]).to.deep.equal(expectedPayload)
          done()
        })
      })
    })
  })
})
