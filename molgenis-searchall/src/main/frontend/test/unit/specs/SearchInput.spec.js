import { shallow } from 'vue-test-utils'
import SearchInput from 'components/SearchInput.vue'

const translations = {
  'search-placeholder': 'please search',
  'search-button-label': 'Search',
  'clear-button-label': 'Clear'
}

describe('SearchInput', () => {
  describe('smoke tests', () => {
    const $t = (key) => translations[key]
    const wrapper = shallow(SearchInput, {
      mocks: {
        $t
      }
    })

    it('should use "search-input" as name', () => {
      expect(wrapper.name()).to.equal('search-input')
    })

    it('should use the correct default props', () => {
      expect(wrapper.vm.query).to.equal('')
    })

    it('should have a disabled search and a clear button when query is empty', () => {
      const buttons = wrapper.findAll('button')
      expect(buttons.at(0).html()).to.equal('<button disabled="disabled" type="button" class="btn btn-primary">search-button-label</button>')
      expect(buttons.at(1).html()).to.equal('<button disabled="disabled" type="button" class="btn btn-light">clear-button-label</button>')
    })

    it('should have enabled search and a clear button when query is not empty', () => {
      wrapper.setData({
        query: 'a query'
      })

      const buttons = wrapper.findAll('button')
      expect(buttons.at(0).html()).to.equal('<button type="button" class="btn btn-primary">search-button-label</button>')
      expect(buttons.at(1).html()).to.equal('<button type="button" class="btn btn-light">clear-button-label</button>')
    })
  })
})
