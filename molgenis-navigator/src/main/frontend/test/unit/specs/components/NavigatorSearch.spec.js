import { shallow, createLocalVue } from 'vue-test-utils'
import Vuex from 'vuex'
import NavigatorSearch from '@/components/NavigatorSearch'

const localVue = createLocalVue()
localVue.use(Vuex)

describe('NavigatorSearch.vue', () => {
  describe('search input', () => {
    it('should exist', () => {
      const state = {
        route: {
          query: {
            q: 'search text'
          }
        }
      }

      let store = new Vuex.Store({
        state: state
      })

      const wrapper = shallow(NavigatorSearch, {
        store,
        localVue,
        mocks: {
          $t: () => {}
        }
      })
      expect(wrapper.find('b-form-input').exists()).to.be.true
    })
  })
})
