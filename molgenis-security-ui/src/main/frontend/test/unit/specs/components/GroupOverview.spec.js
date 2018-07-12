import GroupOverview from '../../../../src/components/GroupOverview'
import { createLocalVue, shallowMount } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {}
  return translations[key]
}

describe('GroupOverview component', () => {
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

  const groups = [
    {name: 'group1', label: 'My group 1'},
    {name: 'group2', label: 'My group 2'}
  ]

  const loginUser = {
    name: 'admin',
    isSuperUser: true
  }

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    state = {
      loginUser: loginUser
    }

    actions = {
      fetchGroups: () => td.function()
    }

    getters = {
      groups: () => groups,
      getLoginUser: () => loginUser
    }

    mutations = {
      clearToast: td.function()
    }

    store = new Vuex.Store({state, actions, getters, mutations})
  })

  const stubs = ['router-link', 'router-view']

  it('should return the groups via a getter', () => {
    const wrapper = shallowMount(GroupOverview, { store, stubs, localVue })
    expect(wrapper.vm.groups).to.deep.equal(groups)
  })
  it('should navigate to the group add page', () => {
    const wrapper = shallowMount(GroupOverview, { mocks: { $router, $route }, store, stubs, localVue })
    wrapper.find('#add-group-btn').trigger('click')
    expect(pushedRoute).to.deep.equal({name: 'createGroup'})
    td.verify(mutations.clearToast(state, undefined))
  })
})
