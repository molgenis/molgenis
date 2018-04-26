import utils from '@molgenis/molgenis-vue-test-utils'
import td from 'testdouble'
import api from '@molgenis/molgenis-api-client'
import actions from 'store/actions'
import { ADD_FINISHED_JOB, UPDATE_JOB } from 'store/mutations'

describe('actions', () => {
  describe('Testing action IMPORT_FILE', () => {
    afterEach(() => td.reset())

    it('Should send a file to the server and poll twice due to RUNNING job', done => {
      const file = 'test-file'
      const jobUrl = '/api/v2/job/test_job'
      const job1 = {
        id: '1',
        status: 'RUNNING'
      }

      const job2 = {
        id: '1',
        status: 'FINISHED'
      }

      const postFile = td.function('api.postFile')
      td.when(postFile('/plugin/one-click-importer/upload', file)).thenResolve(jobUrl)
      td.replace(api, 'postFile', postFile)

      const get = td.function('api.get')
      td.when(get(jobUrl)).thenResolve(job1, job2)
      td.replace(api, 'get', get)

      const options = {
        payload: file,
        expectedMutations: [
          {type: UPDATE_JOB, payload: job1},
          {type: UPDATE_JOB, payload: job2},
          {type: ADD_FINISHED_JOB, payload: job2}
        ]
      }

      utils.testAction(actions.__IMPORT_FILE__, options, done)
    })

    it('Should send a file to the server and poll twice due to PENDING job', done => {
      const file = 'test-file'
      const jobUrl = '/api/v2/job/test_job'
      const job1 = {
        id: '1',
        status: 'PENDING'
      }

      const job2 = {
        id: '1',
        status: 'FINISHED'
      }

      const postFile = td.function('api.postFile')
      td.when(postFile('/plugin/one-click-importer/upload', file)).thenResolve(jobUrl)
      td.replace(api, 'postFile', postFile)

      const get = td.function('api.get')
      td.when(get(jobUrl)).thenResolve(job1, job2)
      td.replace(api, 'get', get)

      const options = {
        payload: file,
        expectedMutations: [
          {type: UPDATE_JOB, payload: job1},
          {type: UPDATE_JOB, payload: job2},
          {type: ADD_FINISHED_JOB, payload: job2}
        ]
      }

      utils.testAction(actions.__IMPORT_FILE__, options, done)
    })

    it('Should send a file to the server and fail the job instantly', done => {
      const file = 'test-file'
      const jobUrl = '/api/v2/job/test_job'
      const job = {
        id: '1',
        status: 'FAILED'
      }

      const postFile = td.function('api.postFile')
      td.when(postFile('/plugin/one-click-importer/upload', file)).thenResolve(jobUrl)
      td.replace(api, 'postFile', postFile)

      const get = td.function('api.get')
      td.when(get(jobUrl)).thenResolve(job)
      td.replace(api, 'get', get)

      const options = {
        payload: file,
        expectedMutations: [
          {type: UPDATE_JOB, payload: job},
          {type: ADD_FINISHED_JOB, payload: job}
        ]
      }

      utils.testAction(actions.__IMPORT_FILE__, options, done)
    })
  })
})
