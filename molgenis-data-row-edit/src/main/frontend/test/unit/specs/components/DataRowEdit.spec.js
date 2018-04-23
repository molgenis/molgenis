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
      formFields: [],
      formData: {}
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
      td.when(post('/api/v1/' + tableId + '/' + rowId + '?_method=PUT', {body: '{"id":"test_id","a":"a"}'})).thenResolve(okeStatus)
      // add post
      // td.when(post('/api/v1/' + tableId + '?_method=PUT', mockBody)).thenResolve(okeStatus)
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
      wrapper.setData({formData: {id: 'test_id', a: 'a'}})
      wrapper.vm.onSubmit()
      done()
    })

    it('goBackToPluginCaller should call the history.go method', (done) => {
      td.replace(window.history, 'go', function () {done()})
      wrapper.vm.goBackToPluginCaller()
    })
  })
})

// describe('On created with row id', () => {
//   const getRowDataResponse = {
//     _href: 'some-href',
//     _meta: {
//       label: 'my-row-data'
//     },
//     foo: 'bar',
//     flub: 'blaf'
//   }
//
//   let wrapper
//
//   before(() => {
//     td.reset()
//     const get = td.function('api.get')
//     td.when(get('/api/v2/' + tableId + '/' + rowId)).thenResolve(getRowDataResponse)
//     td.replace(api, 'get', get)
//     td.replace(EntityToFormMapper, 'generateForm', rowForm)
//
//     wrapper = shallow(DataRowEdit, {
//       propsData: {
//         dataTableId: tableId,
//         dataRowId: rowId
//       }
//     })
//   })
//
//   it('Should make the route tableId the dataTableId.', () => {
//     expect(wrapper.vm.dataTableId).to.equal(tableId)
//   })
//
//   it('Should make the route rowId the dataRowId.', () => {
//     expect(wrapper.vm.dataRowId).to.equal(rowId)
//   })
// })

// describe('On created without row id', () => {
//   const getRowMetaResponse = {
//     href: 'some-href',
//     meta: {
//       label: 'my-row-data'
//     }
//   }
//
//   let wrapper
//
//   before(() => {
//     td.reset()
//     const get = td.function('api.get')
//     td.when(get('/api/v2/' + tableId + '?num=0')).thenResolve(getRowMetaResponse)
//     td.replace(api, 'get', get)
//     td.replace(EntityToFormMapper, 'generateForm', rowForm)
//
//     wrapper = shallow(DataRowEdit, {
//       propsData: {
//         dataTableId: tableId
//       }
//     })
//   })
//
//   it('Should make the route tableId the dataTableId.', () => {
//     expect(wrapper.vm.dataTableId).to.equal(tableId)
//   })
//
// })

// describe('After creating', () => {
//
//   const mockErrorMessage = 'An error has occurred.'
//   let wrapper
//   const getRowDataResponse = {
//     _href: 'some-href',
//     _meta: {
//       label: 'my-row-data'
//     },
//     foo: 'bar',
//     flub: 'blaf'
//   }
//
//   before(() => {
//     wrapper = shallow(DataRowEdit, {
//       propsData: {
//         dataTableId: tableId,
//         dataRowId: rowId
//       }
//     })
//
//     wrapper.vm.$t = function () {
//       return mockErrorMessage
//     }
//
//     td.reset()
//     const post = td.function('api.post')
//     td.when(post('/api/v1/' + tableId + '/' + rowId + '?_method=PUT', {body: '{"id":"test_id","a":"a"}'}))
//       .thenResolve({status: 'OKE'})
//     td.replace(api, 'post', post)
//     const get = td.function('api.get')
//     td.when(get('/api/v2/' + tableId + '/' + rowId)).thenResolve(getRowDataResponse)
//     td.replace(api, 'get', get)
//     td.replace(EntityToFormMapper, 'generateForm', rowForm)
//   })
//
//   it('Calling clear alert, clear the alert', () => {
//     wrapper.vm.clearAlert()
//     expect(wrapper.vm.alert).to.equal(null)
//   })
//
//   it('Calling onValueChanged should pass state of the form to formData', () => {
//     wrapper.vm.onValueChanged({foo: 'bar'})
//     expect(wrapper.vm.formData).to.deep.equal({foo: 'bar'})
//   })
//
//   it('Calling handle error, sets the alert', () => {
//     wrapper.vm.handleError('test-error')
//     expect(wrapper.vm.alert).to.deep.equal({
//       message: 'test-error',
//       type: 'danger'
//     })
//   })
//
//   it('Calling handle error with not passing a string sets the alert the default alert', () => {
//     wrapper.vm.handleError({foo: 'bar'})
//     expect(wrapper.vm.alert).to.deep.equal({
//       message: mockErrorMessage,
//       type: 'danger'
//     })
//   })
//
//   it('Submitting the form triggers post', () => {
//     wrapper.setData({formData: {id: 'test_id', a: 'a'}})
//     wrapper.vm.onSubmit()
//   })
// })
