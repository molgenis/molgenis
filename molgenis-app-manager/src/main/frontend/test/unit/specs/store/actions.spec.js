import api from '@molgenis/molgenis-api-client'
import td from 'testdouble'
import actions from 'src/store/actions'
import utils from '@molgenis/molgenis-vue-test-utils'

describe('actions', () => {
  describe('ACTIVATE_APP', () => {
    it('should dispatch FETCH_APPS on successful get request', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/appmanager/activate/test_id')).thenResolve('OK')
      td.replace(api, 'get', get)

      const options = {
        payload: 'test_id',
        expectedActions: [
          {type: 'FETCH_APPS'}
        ]
      }

      utils.testAction(actions.ACTIVATE_APP, options, done)
    })

    it('should commit SET_ERROR on failed get request', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/appmanager/activate/test_id')).thenReject('failed get request')
      td.replace(api, 'get', get)

      const options = {
        payload: 'test_id',
        expectedMutations: [
          {type: 'SET_ERROR', payload: 'failed get request'}
        ]
      }

      utils.testAction(actions.ACTIVATE_APP, options, done)
    })
  })

  describe('DEACTIVATE_APP', () => {
    it('should dispatch FETCH_APPS on successful get request', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/appmanager/deactivate/test_id')).thenResolve('OK')
      td.replace(api, 'get', get)

      const options = {
        payload: 'test_id',
        expectedActions: [
          {type: 'FETCH_APPS'}
        ]
      }

      utils.testAction(actions.DEACTIVATE_APP, options, done)
    })

    it('should commit SET_ERROR on failed get request', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/appmanager/deactivate/test_id')).thenReject('failed get request')
      td.replace(api, 'get', get)

      const options = {
        payload: 'test_id',
        expectedMutations: [
          {type: 'SET_ERROR', payload: 'failed get request'}
        ]
      }

      utils.testAction(actions.DEACTIVATE_APP, options, done)
    })
  })

  describe('DELETE_APP', () => {
    it('should dispatch FETCH_APPS on successful get request', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/appmanager/delete/test_id')).thenResolve('OK')
      td.replace(api, 'get', get)

      const options = {
        payload: 'test_id',
        expectedActions: [
          {type: 'FETCH_APPS'}
        ]
      }

      utils.testAction(actions.DELETE_APP, options, done)
    })

    it('should commit SET_ERROR on failed get request', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/appmanager/delete/test_id')).thenReject('failed get request')
      td.replace(api, 'get', get)

      const options = {
        payload: 'test_id',
        expectedMutations: [
          {type: 'SET_ERROR', payload: 'failed get request'}
        ]
      }

      utils.testAction(actions.DELETE_APP, options, done)
    })
  })

  describe('FETCH_APPS', () => {
    it('should commit UPDATE_APPS and SET_LOADING on successful get request', done => {
      const apps = [{name: 'app1'}, {name: 'app2'}]

      const get = td.function('api.get')
      td.when(get('/plugin/appmanager/apps')).thenResolve(apps)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: 'UPDATE_APPS', payload: apps},
          {type: 'SET_LOADING', payload: false}
        ]
      }

      utils.testAction(actions.FETCH_APPS, options, done)
    })

    it('should commit SET_ERROR on failed get request', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/appmanager/apps')).thenReject('failed get request')
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: 'SET_ERROR', payload: 'failed get request'}
        ]
      }

      utils.testAction(actions.FETCH_APPS, options, done)
    })
  })

  describe('UPLOAD_APP', () => {
    it('should dispatch FETCH_APPS on successful post request', done => {
      const file = 'file'

      const postFile = td.function('api.postFile')
      td.when(postFile('/plugin/appmanager/upload', file)).thenResolve('OK')
      td.replace(api, 'postFile', postFile)

      const options = {
        payload: file,
        expectedActions: [
          {type: 'FETCH_APPS'}
        ]
      }

      utils.testAction(actions.UPLOAD_APP, options, done)
    })

    it('should commit SET_ERROR on failed post request', done => {
      const file = 'file'

      const postFile = td.function('api.postFile')
      td.when(postFile('/plugin/appmanager/upload', file)).thenReject('failed post request')
      td.replace(api, 'postFile', postFile)

      const options = {
        payload: file,
        expectedMutations: [
          {type: 'SET_ERROR', payload: 'failed post request'}
        ]
      }

      utils.testAction(actions.UPLOAD_APP, options, done)
    })
  })
})
