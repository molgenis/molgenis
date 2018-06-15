import GroupOverview from '../../../../src/components/GroupOverview'
import { createLocalVue, shallow } from '@vue/test-utils'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {}
  return translations[key]
}

describe('GroupOverview component', () => {
  let getters
  let localVue
  let store

  const groups = [
    {name: 'group1', label: 'My group 1'},
    {name: 'group2', label: 'My group 2'}
  ]

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    getters = {
      groups: () => groups
    }

    store = new Vuex.Store({getters})
  })

  const stubs = ['router-link', 'router-view']

  it('should return the chapterNavigationList via a getter', () => {
    const wrapper = shallow(GroupOverview, {store, stubs, localVue})
    expect(wrapper.vm.groups).to.deep.equal(groups)
  })
})
