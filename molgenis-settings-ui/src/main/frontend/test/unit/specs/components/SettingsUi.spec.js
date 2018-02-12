import { shallow } from 'vue-test-utils'
import SettingsUi from '@/components/SettingsUi'
import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'
import api from '@molgenis/molgenis-api-client'
import td from 'testdouble'

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

  describe('on created', () => {
    const settingItem = function () {
      return {
        formFields: [],
        formData: {}
      }
    }

    const settingsOptions = {
      items: [{id: '1', label: 'set1'}, {id: '2', label: 'set2'}]
    }

    const settingResponse = {
      items: [settingItem],
      meta: {
        label: 'my-setting'
      }
    }

    const get = td.function('api.get')
    td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package.id==sys_set'))
      .thenResolve(settingsOptions)
    td.when(get('/api/v2/test-setting'))
      .thenResolve(settingResponse)
    td.replace(api, 'get', get)

    td.replace(EntityToFormMapper, 'generateForm', settingItem)

    let pushedRoute = {}
    const $router = {
      push: function (pushed) {
        pushedRoute = pushed
      }
    }
    const $route = {
      params: {
        setting: 'test-setting'
      }
    }
    const wrapper = shallow(SettingsUi, {
      mocks: {
        $router,
        $route
      }
    })

    it('should fetch the settings data', () => {
      expect(pushedRoute).to.deep.equal({path: '/test-setting'})
    })

    it('should make the route setting the selected setting', () => {
      expect(wrapper.vm.selectedSetting).to.equal('test-setting')
    })

    describe('after creating', () => {
      it('calling clear alert, clear the alert', () => {
        wrapper.vm.clearAlert()
        expect(wrapper.vm.alert).to.equal(null)
      })

      it('calling handle error, sets the alert', () => {
        wrapper.vm.handleError('test-error')
        expect(wrapper.vm.alert).to.deep.equal({
          message: 'test-error',
          type: 'danger'
        })
      })
    })
  })
})
