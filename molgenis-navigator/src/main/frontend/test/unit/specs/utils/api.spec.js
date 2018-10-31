import molgenisApiClient from '@molgenis/molgenis-api-client'
import * as api from '@/utils/api'
import AlertError from '@/utils/AlertUtils'
import td from 'testdouble'

describe('api', () => {
  const expect = require('chai').use(require('chai-as-promised')).expect
  afterEach(() => td.reset())

  const items = [{
    type: 'package',
    id: 'package0',
    label: 'package #0',
    readonly: false
  }]
  const folderId = 'folderId'
  const folder = {id: folderId, label: 'label', readonly: false}

  describe('moveItems', (done) => {
    const body = {
      body: JSON.stringify({
        resources: [{id: 'package0', type: 'PACKAGE'}],
        targetFolderId: folderId
      })
    }

    it('should call move endpoint and do nothing on success', () => {
      const response = 'OK'

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/move', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.moveItems(items, folder)).to.eventually.equal(response).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/move', body)).thenReject(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.moveItems(items, folder)).to.eventually.be.rejectedWith(AlertError).then(() => done())
    })
  })

  describe('copyItems', (done) => {
    const body = {
      body: JSON.stringify({
        resources: [{id: 'package0', type: 'PACKAGE'}],
        targetFolderId: folderId
      })
    }

    it('should call copy endpoint and return job on success', () => {
      const response = {
        jobId: 'jobId',
        jobStatus: 'SUCCESS'
      }

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/copy', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'post', post)

      const expectedjob = {type: 'copy', id: 'jobId', status: 'success'}
      expect(api.copyItems(items, folder)).to.eventually.eql(expectedjob).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/copy', body)).thenReject(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.copyItems(items, folder)).to.eventually.be.rejectedWith(AlertError).then(() => done())
    })
  })

  describe('deleteItems', (done) => {
    const body = {
      body: JSON.stringify({
        resources: [{id: 'package0', type: 'PACKAGE'}]
      })
    }

    it('should call delete endpoint and do nothing on success', () => {
      const response = 'OK'

      const delete_ = td.function('molgenisApiClient.delete_')
      td.when(delete_('/plugin/navigator/delete', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'delete_', delete_)

      expect(api.deleteItems(items)).to.eventually.equal(response).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const delete_ = td.function('molgenisApiClient.delete_')
      td.when(delete_('/plugin/navigator/delete', body)).thenReject(response)
      td.replace(molgenisApiClient, 'delete_', delete_)

      expect(api.deleteItems(items)).to.eventually.be.rejectedWith(AlertError).then(() => done())
    })
  })

  describe('downloadItems', (done) => {
    const body = {
      body: JSON.stringify({
        resources: [{id: 'package0', type: 'PACKAGE'}]
      })
    }

    it('should call download endpoint and return job on success', () => {
      const response = {
        jobId: 'jobId',
        jobStatus: 'SUCCESS'
      }

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/download', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'post', post)

      const expectedjob = {type: 'download', id: 'jobId', status: 'success'}
      expect(api.downloadItems(items)).to.eventually.eql(expectedjob).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/download', body)).thenReject(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.downloadItems(items)).to.eventually.be.rejectedWith(AlertError).then(() => done())
    })
  })

  describe('createItem', (done) => {
    const body = {
      body: JSON.stringify({
        entities: [{id: 'package0', label: 'package #0', parent: folderId}]
      })
    }
    const post = td.function('molgenisApiClient.post')

    it('should create a new item and do nothing on success', () => {
      const response = 'OK'

      td.when(post('/api/v2/sys_md_Package', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.createItem(items[0], folder)).to.eventually.equal(response).then(done, done)
    })

    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      td.when(post('/api/v2/sys_md_Package', body)).thenReject(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.createItem(items[0], folder)).to.eventually.be.rejectedWith(AlertError).then(() => done())
    })
  })

  describe('updateItem', (done) => {
    const updatedItem = {
      type: 'package',
      id: 'package0',
      label: 'updated package #0',app
      readonly: false
    }
    const body = {
      body: JSON.stringify({
        resource: {id: 'package0', type: 'PACKAGE', label: 'updated package #0', description: undefined}
      })
    }
    const put = td.function('molgenisApiClient.put')

    it('should call update endpoint and do nothing on success', () => {
      const response = 'OK'
      td.when(put('/plugin/navigator/update', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'put', put)

      expect(api.updateItem(items[0], updatedItem)).to.eventually.equal(response).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      td.when(put('/plugin/navigator/update', body)).thenReject(response)
      td.replace(molgenisApiClient, 'put', put)

      expect(api.deleteItems(items, updatedItem)).to.eventually.be.rejectedWith(AlertError).then(() => done())
    })
  })
})
