import AppManagerContainer from 'src/pages/AppManagerContainer'
import { createLocalVue, shallow } from '@vue/test-utils'
import Vuex from 'vuex'
import td from 'testdouble'

describe('pages', () => {
  let actions
  let localVue
  let state
  let store

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)

    actions = {
      FETCH_APPS: td.function(),
      UPLOAD_APP: td.function()
    }

    state = {
      apps: [],
      error: '',
      loading: false
    }

    store = new Vuex.Store({actions, state})
  })

  describe('AppManagerContainer', () => {
    it('should dispatch FETCH_APPS when created', () => {
      shallow(AppManagerContainer, {store})
      td.verify(actions.FETCH_APPS(td.matchers.anything(), undefined, undefined))
    })

    it('should compute a list of apps based on a search query', () => {
      state.apps = [{label: 'test', description: 'match'}, {label: 'example', description: 'no match'}]
      store = new Vuex.Store({actions, state})

      const wrapper = shallow(AppManagerContainer, {store})
      wrapper.setData({searchQuery: 'test'})
      expect(wrapper.vm.apps).to.deep.equal([{label: 'test', description: 'match'}])
    })

    it('should compute the error message based on the state', () => {
      state.error = 'error'
      store = new Vuex.Store({actions, state})
      const wrapper = shallow(AppManagerContainer, {store})
      expect(wrapper.vm.error).to.equal('error')
    })

    it('should compute the loading state based on the state', () => {
      const wrapper = shallow(AppManagerContainer, {store})
      expect(wrapper.vm.loading).to.equal(false)
    })

    it('should dispatch UPLOAD_APP when handleFileUpload is called', () => {
      const wrapper = shallow(AppManagerContainer, {store})
      const event = {target: {files: ['file']}}
      wrapper.vm.handleFileUpload(event)
      td.verify(actions.UPLOAD_APP(td.matchers.anything(), 'file', undefined))
    })
  })
})
