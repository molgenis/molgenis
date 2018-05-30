import AppCard from 'src/components/AppCard'
import { createLocalVue, shallow } from '@vue/test-utils'
import Vuex from 'vuex'
import td from 'testdouble'

describe('components', () => {
  let actions
  let localVue
  let store

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)

    actions = {
      ACTIVATE_APP: td.function(),
      DEACTIVATE_APP: td.function(),
      DELETE_APP: td.function()
    }

    store = new Vuex.Store({actions})
  })

  const app = {id: 'test', isActive: false}
  const propsData = {app: app}

  describe('AppCard', () => {
    it('should dispatch DELETE_APP when deleteApp method is called', () => {
      const wrapper = shallow(AppCard, {propsData, store})
      wrapper.vm.deleteApp(app)
      td.verify(actions.DELETE_APP(td.matchers.anything(), 'test', undefined))
    })

    it('should dispatch ACTIVATE_APP when app is inActive', () => {
      const wrapper = shallow(AppCard, {propsData, store})
      wrapper.vm.toggleAppActiveState(app)
      td.verify(actions.ACTIVATE_APP(td.matchers.anything(), 'test', undefined))
    })

    it('should dispatch DEACTIVATE_APP when app is active', () => {
      app.isActive = true
      propsData.app = app
      const wrapper = shallow(AppCard, {propsData, store})
      wrapper.vm.toggleAppActiveState(app)
      td.verify(actions.DEACTIVATE_APP(td.matchers.anything(), 'test', undefined))
    })
  })
})
