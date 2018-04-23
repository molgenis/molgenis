import {shallow} from 'vue-test-utils'
import DataRowEdit from '@/components/DataRowEdit'
import {EntityToFormMapper} from '@molgenis/molgenis-ui-form'
import api from '@molgenis/molgenis-api-client'
import td from 'testdouble'

describe('DataRowEdit component', () => {
  window.__INITIAL_STATE__ = {
    dataExplorerBaseUrl: 'plugin/data-explorer'
  }

  const tableId = 'testTableId'
  const rowId = 'testRowId'
  const rowForm = function () {
    return {
      formFields: ['a'],
      formData: {b: 'c'}
    }
  }

  it('Should have "DataRowEdit" as name.', () => {
    expect(DataRowEdit.name).to.equal('DataRowEdit')
  })

  it('Should have a "data" function.', () => {
    expect(typeof DataRowEdit.data).to.equal('function')
  })

  describe('Data should initialize', () => {
    const data = DataRowEdit.data()

    it('dataTableLabel as empty string.', () => {
      expect(data.dataTableLabel).to.equal('')
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

    it('alert to null.', () => {
      expect(data.alert).to.equal(null)
    })

    it('showForm to false.', () => {
      expect(data.showForm).to.equal(false)
    })

    it('isSaving to false.', () => {
      expect(data.showForm).to.equal(false)
    })
  })

  describe('methods', () => {
    const getRowDataResponse = {
      _href: 'some-href',
      _meta: {
        label: 'my-row-data'
      },
      foo: 'bar',
      flub: 'blaf'
    }
    // eslint-disable-next-line
    const formUpdate = {"foo": "bar"}

    let wrapper

    beforeEach(function () {
      td.reset()

      const get = td.function('api.get')
      td.when(get('/api/v2/' + tableId + '/' + rowId)).thenResolve(getRowDataResponse)
      td.replace(api, 'get', get)

      const post = td.function('api.post')
      const okeStatus = {status: 'OKE'}
      // update post
      td.when(post('/api/v1/' + tableId + '/' + rowId + '?_method=PUT', {body: '{"id":"update","a":"a"}'})).thenResolve(okeStatus)
      // add post
      td.when(post('/api/v1/' + tableId + '?_method=PUT', {body: '{"id":"create","b":"b"}'})).thenResolve(okeStatus)
      td.replace(api, 'post', post)
      td.replace(EntityToFormMapper, 'generateForm', rowForm)

      wrapper = shallow(DataRowEdit, {
        propsData: {
          dataTableId: tableId,
          dataRowId: rowId
        }
      })
    })

    it('onValueChanged should update the formData', (done) => {
      wrapper.vm.onValueChanged(formUpdate)
      expect(wrapper.vm.formData).to.deep.equal(formUpdate)
      done()
    })

    it('onSubmit should post the form data formData', (done) => {
      wrapper.setData({formData: {id: 'update', a: 'a'}})
      wrapper.vm.onSubmit()
      done()
    })

    it('onSubmit when no rowId is set should trigger the create call', (done) => {
      wrapper.setData({dataRowId: null})
      wrapper.setData({formData: {id: 'create', b: 'b'}})
      wrapper.vm.onSubmit()
      done()
    })

    it('goBackToPluginCaller should call the history.go method', (done) => {
      td.replace(window.history, 'go', () => done())
      wrapper.vm.goBackToPluginCaller()
    })

    it('clearAlter should clear the alert value', () => {
      wrapper.setData({ alert: { message: 'alert', type: 'danger' } })
      wrapper.vm.clearAlert()
      expect(wrapper.vm.alert).to.equal(null)
    })

    it('handle error, sets the alert', () => {
      wrapper.vm.handleError('test-error')
      expect(wrapper.vm.alert).to.deep.equal({
        message: 'test-error',
        type: 'danger'
      })
    })

    it('handle error with not passing a string sets the alert the default alert', () => {
      wrapper.vm.$t = () => 'alter !'

      wrapper.vm.handleError({foo: 'bar'})
      expect(wrapper.vm.alert).to.deep.equal({
        message: 'alter !',
        type: 'danger'
      })
    })

    it('initializeForm should setup the form and set showForm to true', () => {
      wrapper.vm.initializeForm('a', 'b')
      expect(wrapper.vm.formFields).to.deep.equal(['a'])
      expect(wrapper.vm.formData).to.deep.equal({b: 'c'})
      expect(wrapper.vm.showForm).to.equal(true)
    })

    it('parseEditResponse should transform the editResponse to an object usable by the form component', () => {
      const mockEditResponse = {
        _meta: {
          label: 'label',
          foo: 'bar'
        },
        _href: 'http://foo.bar.com',
        a: 'a',
        b: 'b'
      }
      const result = wrapper.vm.parseEditResponse(mockEditResponse)
      const expected = {
        _meta: mockEditResponse._meta,
        rowData: {
          a: 'a',
          b: 'b'
        }
      }
      expect(result).to.deep.equal(expected)
    })

    it('parseAddResponse should transform the addResponse and set readOnly fields to false', () => {
      const mockEditResponse = {
        meta: {
          label: 'label',
          foo: 'bar',
          attributes: [
            { readOnly: true },
            { readOnly: false },
            {}
          ]
        },
        href: 'http://foo.bar.com'
      }
      const result = wrapper.vm.parseAddResponse(mockEditResponse)
      const expected = {
        label: 'label',
        foo: 'bar',
        attributes: [
          { readOnly: false },
          { readOnly: false },
          { readOnly: false }
        ]
      }
      expect(result).to.deep.equal(expected)
    })
  })

  describe('created', () => {
    it('should when rowId is set fetch the data to be edited, parse the response and initialize the form', (done) => {
      const getRowDataResponse = {
        _href: 'some-href',
        _meta: {
          label: 'my-row-data'
        },
        foo: 'bar',
        flub: 'blaf'
      }

      td.reset()
      const get = td.function('api.get')
      td.when(get('/api/v2/' + tableId + '/' + rowId)).thenResolve(getRowDataResponse)
      td.replace(api, 'get', get)
      td.replace(EntityToFormMapper, 'generateForm', rowForm)

      const wrapper = shallow(DataRowEdit, {
        propsData: {
          dataTableId: tableId,
          dataRowId: rowId
        }
      })

      wrapper.vm.$nextTick(() => { // resolve then
        wrapper.vm.$nextTick(() => { // update the model
          expect(wrapper.vm.formFields).to.deep.equal(['a'])
          expect(wrapper.vm.formData).to.deep.equal({b: 'c'})
          expect(wrapper.vm.showForm).to.equal(true)
          done()
        })
      })
    })

    it('when rowId is not set fetch the data structure, parse the response and initialize the form', (done) => {
      const createMock = function () {
        return {
          formFields: ['a'],
          formData: {}
        }
      }
      td.replace(EntityToFormMapper, 'generateForm', createMock)
      td.reset()
      const get = td.function('api.get')
      const response = {
        meta: {
          label: 'label',
          foo: 'bar',
          attributes: [
            { readOnly: true },
            { readOnly: false },
            {}
          ]
        },
        href: 'http://foo.bar.com'
      }
      td.when(get('/api/v2/' + tableId + '?num=0')).thenResolve(response)
      td.replace(api, 'get', get)
      td.replace(EntityToFormMapper, 'generateForm', rowForm)
      const wrapper = shallow(DataRowEdit, {
        propsData: {
          dataTableId: tableId
        }
      })

      wrapper.vm.$nextTick(() => { // resolve then
        wrapper.vm.$nextTick(() => { // update the model
          expect(wrapper.vm.formFields[0]).to.deep.equal('a')
          expect(wrapper.vm.showForm).to.equal(true)
          done()
        })
      })
    })
  })
})
