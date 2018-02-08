import { createLocalVue, shallow } from 'vue-test-utils'
import Settings from '@/components/Settings'
import td from 'testdouble'

const localVue = createLocalVue()

describe('Settings.vue', () => {
  it('should display settings name', () => {
    expect(Settings.name).to.equal('Settings')
  })
  it('should contain created-method content', () => {
    const mockDispatch = td.function('dispatch')
    td.when(mockDispatch(GET_SETTINGS)).thenResolve()
    Settings.$store =
      {
        dispatch: mockDispatch
      }
    Settings.created()
    td.verify(mockDispatch(GET_SETTINGS))
  })
  it('should contain computed-method content', () => {
    const actions = {
      '__GET_SETTINGS__': function () {}
    }
    const state = {
      settings: ['settings_entity_1']
    }
    const store = new Vuex.Store({
      state,
      actions
    })
    const wrapper = shallow(Settings, {store, localVue})
    expect(wrapper.vm.settings).to.deep.equal(['settings_entity_1'])
  })
})
