import molgenisApiClient from '@molgenis/molgenis-api-client'
import * as api from '@/utils/api'
import td from 'testdouble'

describe('api', () => {
  const expect = require('chai').use(require('chai-as-promised')).expect
  afterEach(() => td.reset())

  const resources = [{
    type: 'PACKAGE',
    id: 'package0',
    label: 'package #0',
    readonly: false
  }]
  const folderId = 'folderId'
  const folder = {id: folderId, label: 'label', readonly: false}

  describe('fetchJob', (done) => {
    it('should call the job endpoint for a copy job and return current job', () => {
      const job = {
        type: 'COPY',
        id: 'jobId',
        status: 'RUNNING',
        progress: undefined,
        progressMax: undefined,
        progressMessage: undefined,
        resultUrl: undefined
      }

      const response = {
        type: 'Copy',
        identifier: 'jobId',
        status: 'SUCCESS'
      }

      const get = td.function('molgenisApiClient.get')
      td.when(get('/api/v2/sys_job_ResourceCopyJobExecution/jobId')).thenResolve(response)
      td.replace(molgenisApiClient, 'get', get)

      const updatedJob = {
        type: 'COPY',
        id: 'jobId',
        status: 'SUCCESS',
        progress: undefined,
        progressMax: undefined,
        progressMessage: undefined,
        resultUrl: undefined
      }
      expect(api.fetchJob(job)).to.eventually.eql(updatedJob).then(done, done)
    })
    it('should call the job endpoint for a download job and return current job', () => {
      const job = {
        type: 'DOWNLOAD',
        id: 'jobId',
        status: 'RUNNING',
        progress: undefined,
        progressMax: undefined,
        progressMessage: undefined,
        resultUrl: undefined
      }

      const response = {
        type: 'DownloadJob',
        identifier: 'jobId',
        status: 'FAILED'
      }

      const get = td.function('molgenisApiClient.get')
      td.when(get('/api/v2/sys_job_ResourceDownloadJobExecution/jobId')).thenResolve(response)
      td.replace(molgenisApiClient, 'get', get)

      const updatedJob = {
        type: 'DOWNLOAD',
        id: 'jobId',
        status: 'FAILED',
        progress: undefined,
        progressMax: undefined,
        progressMessage: undefined,
        resultUrl: undefined
      }
      expect(api.fetchJob(job)).to.eventually.eql(updatedJob).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const job = {
        type: 'DOWNLOAD',
        id: 'jobId',
        status: 'RUNNING',
        progress: undefined,
        progressMax: undefined,
        resultUrl: undefined
      }

      const response = {errors: [{message: 'error'}]}

      const get = td.function('molgenisApiClient.get')
      td.when(get('/api/v2/sys_job_ResourceDownloadJobExecution/jobId')).thenReject(response)
      td.replace(molgenisApiClient, 'get', get)

      expect(api.fetchJob(job)).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('getResourcesByFolderId', (done) => {
    it('should call the get endpoint with folderId parameter and return folder state', () => {
      const response = {
        folder: {id: 'id', label: 'label', readonly: 'false'},
        resources: [{type: 'PACKAGE', id: 'p0', label: 'package #0', readonly: 'false'}]
      }
      const get = td.function('molgenisApiClient.get')
      td.when(get('/plugin/navigator/get?folderId=f0')).thenResolve(response)
      td.replace(molgenisApiClient, 'get', get)

      expect(api.getResourcesByFolderId('f0')).to.eventually.equal(response).then(done, done)
    })

    it('should call the get endpoint without folderId parameter and return folder state', () => {
      const response = {
        folder: {id: 'id', label: 'label', readonly: 'false'},
        resources: [{type: 'PACKAGE', id: 'p0', label: 'package #0', readonly: 'false'}]
      }
      const get = td.function('molgenisApiClient.get')
      td.when(get('/plugin/navigator/get')).thenResolve(response)
      td.replace(molgenisApiClient, 'get', get)

      expect(api.getResourcesByFolderId()).to.eventually.equal(response).then(done, done)
    })

    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const get = td.function('molgenisApiClient.get')
      td.when(get('/plugin/navigator/get?folderId=unknownFolder')).thenReject(response)
      td.replace(molgenisApiClient, 'get', get)

      expect(api.getResourcesByFolderId('unknownFolder')).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('getResourcesByQuery', (done) => {
    it('should call the search endpoint and return folder state', () => {
      const response = {
        resources: [{type: 'PACKAGE', id: 'p0', label: 'package #0', readonly: 'false'}]
      }
      const get = td.function('molgenisApiClient.get')
      td.when(get('/plugin/navigator/search?query=text')).thenResolve(response)
      td.replace(molgenisApiClient, 'get', get)

      expect(api.getResourcesByQuery('text')).to.eventually.equal(response).then(done, done)
    })

    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const get = td.function('molgenisApiClient.get')
      td.when(get('/plugin/navigator/search?query=text')).thenReject(response)
      td.replace(molgenisApiClient, 'get', get)

      expect(api.getResourcesByQuery('text')).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('moveResources', (done) => {
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

      expect(api.moveResources(resources, folder)).to.eventually.equal(response).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/move', body)).thenReject(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.moveResources(resources, folder)).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('copyResources', (done) => {
    const body = {
      body: JSON.stringify({
        resources: [{id: 'package0', type: 'PACKAGE'}],
        targetFolderId: folderId
      })
    }

    it('should call copy endpoint and return job on success', () => {
      const response = {
        type: 'Copy',
        identifier: 'jobId',
        status: 'SUCCESS'
      }

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/copy', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'post', post)

      const expectedjob = {
        type: 'COPY',
        id: 'jobId',
        status: 'SUCCESS',
        progress: undefined,
        progressMax: undefined,
        progressMessage: undefined,
        resultUrl: undefined
      }
      expect(api.copyResources(resources, folder)).to.eventually.eql(expectedjob).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/copy', body)).thenReject(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.copyResources(resources, folder)).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('deleteResources', (done) => {
    const body = {
      body: JSON.stringify({
        resources: [{id: 'package0', type: 'PACKAGE'}]
      })
    }

    it('should call download endpoint and return job on success', () => {
      const response = {
        type: 'DeleteJob',
        identifier: 'jobId',
        status: 'SUCCESS'
      }

      const delete_ = td.function('molgenisApiClient.delete_')
      td.when(delete_('/plugin/navigator/delete', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'delete_', delete_)

      const expectedjob = {
        type: 'DELETE',
        id: 'jobId',
        status: 'SUCCESS',
        progress: undefined,
        progressMax: undefined,
        progressMessage: undefined,
        resultUrl: undefined
      }
      expect(api.deleteResources(resources)).to.eventually.eql(expectedjob).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const delete_ = td.function('molgenisApiClient.delete_')
      td.when(delete_('/plugin/navigator/delete', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'delete_', delete_)

      expect(api.deleteResources(resources)).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('deleteResources', (done) => {
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

      expect(api.deleteResources(resources)).to.eventually.equal(response).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const delete_ = td.function('molgenisApiClient.delete_')
      td.when(delete_('/plugin/navigator/delete', body)).thenReject(response)
      td.replace(molgenisApiClient, 'delete_', delete_)

      expect(api.deleteResources(resources)).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('downloadResources', (done) => {
    const body = {
      body: JSON.stringify({
        resources: [{id: 'package0', type: 'PACKAGE'}]
      })
    }

    it('should call download endpoint and return job on success', () => {
      const response = {
        type: 'DownloadJob',
        identifier: 'jobId',
        status: 'SUCCESS'
      }

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/download', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'post', post)

      const expectedjob = {
        type: 'DOWNLOAD',
        id: 'jobId',
        status: 'SUCCESS',
        progress: undefined,
        progressMax: undefined,
        progressMessage: undefined,
        resultUrl: undefined
      }
      expect(api.downloadResources(resources)).to.eventually.eql(expectedjob).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      const post = td.function('molgenisApiClient.post')
      td.when(post('/plugin/navigator/download', body)).thenReject(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.downloadResources(resources)).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('createResource', (done) => {
    const body = {
      body: JSON.stringify({
        entities: [{id: 'package0', label: 'package #0', parent: folderId}]
      })
    }
    const post = td.function('molgenisApiClient.post')

    it('should create a new resource and do nothing on success', () => {
      const response = 'OK'

      td.when(post('/api/v2/sys_md_Package', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.createResource(resources[0], folder)).to.eventually.equal(response).then(done, done)
    })

    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      td.when(post('/api/v2/sys_md_Package', body)).thenReject(response)
      td.replace(molgenisApiClient, 'post', post)

      expect(api.createResource(resources[0], folder)).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })

  describe('updateResource', (done) => {
    const updatedResource = {
      type: 'PACKAGE',
      id: 'package0',
      label: 'updated package #0',
      readonly: false
    }
    const body = {
      body: JSON.stringify({
        resource: {type: 'PACKAGE', id: 'package0', label: 'updated package #0', readonly: false}
      })
    }
    const put = td.function('molgenisApiClient.put')

    it('should call update endpoint and do nothing on success', () => {
      const response = 'OK'
      td.when(put('/plugin/navigator/update', body)).thenResolve(response)
      td.replace(molgenisApiClient, 'put', put)

      expect(api.updateResource(resources[0], updatedResource)).to.eventually.equal(response).then(done, done)
    })
    it('should return alerts in case of errors', (done) => {
      const response = {errors: [{message: 'error'}]}

      td.when(put('/plugin/navigator/update', body)).thenReject(response)
      td.replace(molgenisApiClient, 'put', put)

      expect(api.deleteResources(resources, updatedResource)).to.eventually.be.rejectedWith(Error).then(() => done())
    })
  })
})
