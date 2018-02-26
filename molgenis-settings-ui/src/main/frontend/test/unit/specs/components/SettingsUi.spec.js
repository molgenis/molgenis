import { shallow } from 'vue-test-utils'
import SettingsUi from '@/components/SettingsUi'
import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'
import api from '@molgenis/molgenis-api-client'
import td from 'testdouble'

describe('SettingsUi component', () => {
  it('Should have "SettingsUi" as name.', () => {
    expect(SettingsUi.name).to.equal('SettingsUi')
  })

  it('Should have a "data" function.', () => {
    expect(typeof SettingsUi.data).to.equal('function')
  })

  describe('Data should initialize', () => {
    const data = SettingsUi.data()

    it('selectedSetting to null.', () => {
      expect(data.selectedSetting).to.equal(null)
    })

    it('formState as a empty object.', () => {
      expect(data.formState).to.deep.equal({})
    })

    it('formFields as a empty array.', () => {
      expect(data.formFields).to.deep.equal([])
    })

    it('formData as a empty object.', () => {
      expect(data.formData).to.deep.equal({})
    })

    it('settingsOptions as a empty array.', () => {
      expect(data.settingsOptions).to.deep.equal([])
    })

    it('alert to null.', () => {
      expect(data.alert).to.equal(null)
    })

    it('showForm to false.', () => {
      expect(data.showForm).to.equal(false)
    })

    it('settingLabel as empty string.', () => {
      expect(data.settingLabel).to.equal('')
    })
  })

  describe('On created', () => {
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

    it('Should fetch the settings data.', () => {
      expect(pushedRoute).to.deep.equal({path: '/test-setting'})
    })

    it('Should make the route setting the selected setting.', () => {
      expect(wrapper.vm.selectedSetting).to.equal('test-setting')
    })

    describe('After creating', () => {
      it('Calling clear alert, clear the alert', () => {
        wrapper.vm.clearAlert()
        expect(wrapper.vm.alert).to.equal(null)
      })

      it('Calling onValueChanged should pass state of the form to settings data', () => {
        wrapper.vm.onValueChanged({foo: 'bar'})
        expect(wrapper.vm.formData).to.deep.equal({foo: 'bar'})
      })

      it('Calling handle error, sets the alert', () => {
        wrapper.vm.handleError('test-error')
        expect(wrapper.vm.alert).to.deep.equal({
          message: 'test-error',
          type: 'danger'
        })
      })

      it('Calling handle error with not passing a string sets the alert the default alert', () => {
        wrapper.vm.handleError({foo: 'bar'})
        expect(wrapper.vm.alert).to.deep.equal({
          message: 'An error has occurred.',
          type: 'danger'
        })
      })

      it('Calling the success handler resets the form and signals succes to the user', () => {
        wrapper.vm.formState._reset = function () {}
        wrapper.vm.handleSuccess()
        expect(wrapper.vm.alert).to.deep.equal({
          message: 'Settings saved',
          type: 'success'
        })
      })

      it('Submitting the form triggers post and triggers success handeler ', () => {
        wrapper.vm.formState._reset = function () {
        }
        wrapper.setData({formData: {id: 'test_id', a: 'a'}})
        const post = td.function('api.post')
        td.when(post('/api/v1/test-setting/test_id?_method=PUT', {body: '{"id":"test_id","a":"a"}'}))
          .thenResolve({status: 'OKE'})
        td.replace(api, 'post', post)
        wrapper.vm.onSubmit()
        expect(wrapper.vm.alert).to.deep.equal({
          message: 'Settings saved',
          type: 'success'
        })
      })
    })
  })
})
