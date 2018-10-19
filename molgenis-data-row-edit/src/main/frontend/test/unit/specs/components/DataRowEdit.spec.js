import {shallow} from 'vue-test-utils'
import DataRowEdit from '@/components/DataRowEdit'
import * as repository from '@/repository/dataRowRepository'
import td from 'testdouble'

describe('DataRowEdit component', () => {
  window.__INITIAL_STATE__ = {
    dataExplorerBaseUrl: 'plugin/data-explorer'
  }

  const tableId = 'testTableId'
  const rowId = 'testRowId'

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
    let wrapper
    let mappedCreateData = {
      formFields: [{id: 'a', type: 'text'}],
      formData: {a: 'b'},
      formLabel: 'form label'
    }
    let mappedUpdateData = {
      formFields: [{id: 'a', type: 'text'}, {id: 'b', type: 'field-group'}],
      formData: {a: 'b'},
      formLabel: 'form label'
    }

    let save // repository.save action mock

    beforeEach(function () {
      td.reset()

      // mock fetch
      const fetch = td.function('repository.fetch')
      td.when(fetch(tableId, rowId)).thenResolve(mappedUpdateData)
      td.when(fetch(tableId, null)).thenResolve(mappedCreateData)
      td.replace(repository, 'fetch', fetch)

      // mock save
      save = td.function('repository.save')
      td.when(save({id: 'update', a: 'a'}, [{id: 'a'}], tableId, rowId)).thenResolve()
      td.when(save({id: 'create', b: 'b'}, [{id: 'a'}], tableId, null)).thenResolve()
      td.replace(repository, 'save', save)

      wrapper = shallow(DataRowEdit, {
        propsData: {
          dataTableId: tableId,
          dataRowId: rowId
        }
      })

      // mock formState for submit testing
      wrapper.setData({
        formState: {
          $valid: true,
          a: {
            $touched: false
          }
        }
      })
    })

    it('onValueChanged should update the formData', (done) => {
      // eslint-disable-next-line
      const formUpdate = {"foo": "bar"}
      wrapper.vm.onValueChanged(formUpdate)
      expect(wrapper.vm.formData).to.deep.equal(formUpdate)
      done()
    })

    it('onSubmit should post the form data formData', (done) => {
      wrapper.setData({formData: {id: 'update', a: 'a'}})
      wrapper.setData({formFields: [{id: 'a'}]})
      wrapper.vm.onSubmit()
      td.verify(save(), {times: 1, ignoreExtraArgs: true})
      done()
    })

    it('onSubmit block the post when the form is invalid', (done) => {
      wrapper.setData({formData: {id: 'update', a: 'a'}})
      wrapper.setData({formFields: [{id: 'a'}]})
      wrapper.setData({
        formState: {
          $valid: false,
          a: {
            $touched: false
          }
        }
      })
      wrapper.vm.onSubmit()
      td.verify(save(), {times: 0, ignoreExtraArgs: true})
      done()
    })

    it('onSubmit when no rowId is set should trigger the create call', (done) => {
      wrapper.setData({dataRowId: null})
      wrapper.setData({formData: {id: 'create', b: 'b'}})
      wrapper.setData({formFields: [{id: 'a'}]})
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
      const mappedData = {
        formFields: ['abc'],
        formData: {foo: 'bar'}
      }
      wrapper.vm.initializeForm(mappedData)
      expect(wrapper.vm.formFields).to.deep.equal(['abc'])
      expect(wrapper.vm.formData).to.deep.equal({foo: 'bar'})
      expect(wrapper.vm.showForm).to.equal(true)
    })
  })

  describe('created', () => {
    let mappedCreateData = {
      formFields: ['a'],
      formData: {a: 'b'},
      formLabel: 'form label'
    }
    let mappedUpdateData = {
      formFields: ['a'],
      formData: {b: 'c'},
      formLabel: 'form label'
    }

    beforeEach(function () {
      td.reset()

      const fetch = td.function('repository.fetch')
      td.when(fetch(tableId, rowId)).thenResolve(mappedUpdateData)
      td.when(fetch(tableId, null)).thenResolve(mappedCreateData)
      td.replace(repository, 'fetch', fetch)
    })

    it('when rowId is not set fetch the data structure and initialize the form', (done) => {
      const wrapper = shallow(DataRowEdit, {
        propsData: {
          dataTableId: tableId
        }
      })

      wrapper.vm.$nextTick(() => { // resolve then
        wrapper.vm.$nextTick(() => { // update the model
          expect(wrapper.vm.formFields).to.deep.equal(['a'])
          expect(wrapper.vm.formData).to.deep.equal({a: 'b'})
          expect(wrapper.vm.showForm).to.equal(true)
          done()
        })
      })
    })

    it('should when rowId is set fetch the data to be edited, and initialize the form', (done) => {
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
  })
})
