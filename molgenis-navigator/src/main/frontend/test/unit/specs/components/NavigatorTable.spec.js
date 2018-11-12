import { shallow, createLocalVue } from 'vue-test-utils'
import Vuex from 'vuex'
import NavigatorTable from '@/components/NavigatorTable'

const localVue = createLocalVue()
localVue.use(Vuex)

describe('NavigatorTable.vue', () => {
  window.__INITIAL_STATE__ = {
    pluginUrls: {}
  }

  describe('table', () => {
    it('should exist', () => {
      const state = {
        resources: [{type: 'PACKAGE', id: 'p0', label: 'package #0'}, {type: 'ENTITY_TYPE', id: 'e0', label: 'entity type #0'}],
        selectedResources: []
      }

      let store = new Vuex.Store({
        state: state
      })

      const wrapper = shallow(NavigatorTable, {
        store,
        localVue,
        mocks: {
          $t: () => {}
        }
      })
      const actual = wrapper.find('b-table').exists()
      expect(actual).to.be.true
    })
  })
})
