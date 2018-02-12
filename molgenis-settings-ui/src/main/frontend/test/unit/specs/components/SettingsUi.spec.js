// import { shallow } from 'vue-test-utils'
import SettingsUi from '@/components/SettingsUi'
// import td from 'testdouble'

describe('SettingsUi component', () => {
  it('should have "SettingsUi" as name', () => {
    expect(SettingsUi.name).to.equal('SettingsUi')
  })

  it('should have a "data" function', () => {
    expect(typeof SettingsUi.data).to.equal('function')
  })

  describe('data should initialize', () => {
    const data = SettingsUi.data()

    it('selectedSetting to null', () => {
      expect(data.selectedSetting).to.equal(null)
    })

    it('state as a empty object', () => {
      expect(data.state).to.deep.equal({})
    })

    it('formFields as a empty array', () => {
      expect(data.formFields).to.deep.equal([])
    })

    it('formData as a empty object', () => {
      expect(data.formData).to.deep.equal({})
    })

    it('settingsOptions as a empty array', () => {
      expect(data.settingsOptions).to.deep.equal([])
    })

    it('alert to null', () => {
      expect(data.alert).to.equal(null)
    })

    it('showForm to false', () => {
      expect(data.showForm).to.equal(false)
    })

    it('settingLabel as empty string', () => {
      expect(data.settingLabel).to.equal('')
    })
  })

  // it('should contain created-method content', () => {
  //   const mockDispatch = td.function('dispatch')
  //   td.when(mockDispatch(GET_SETTINGS)).thenResolve()
  //   Settings.$store =
  //     {
  //       dispatch: mockDispatch
  //     }
  //   Settings.created()
  //   td.verify(mockDispatch(GET_SETTINGS))
  // })
  // it('should contain computed-method content', () => {
  //   const actions = {
  //     '__GET_SETTINGS__': function () {}
  //   }
  //   const state = {
  //     settings: ['settings_entity_1']
  //   }
  //   const store = new Vuex.Store({
  //     state,
  //     actions
  //   })
  //   const wrapper = shallow(Settings, {store, localVue})
  //   expect(wrapper.vm.settings).to.deep.equal(['settings_entity_1'])
  // })
})
