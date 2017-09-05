import testAction from '../../utils/action.utils'
import td from 'testdouble'
import api from '@molgenis/molgenis-api-client'
import actions, { POLL_JOB_URL } from 'store/actions'
import { ADD_FINISHED_JOB, UPDATE_JOB } from 'store/mutations'

describe('actions', () => {
  describe('Testing action IMPORT_FILE', () => {
    afterEach(() => td.reset())

    it('Should send a file to the server and receive a job URL', done => {
      const file = 'test-file'
      const jobUrl = '/api/v2/job/test_job'

      const postFile = td.function('api.postFile')
      td.when(postFile('/plugin/one-click-importer/upload', file)).thenResolve(jobUrl)
      td.replace(api, 'postFile', postFile)

      testAction(actions.__IMPORT_FILE__, file, {}, [], [{type: POLL_JOB_URL, payload: jobUrl}], done)
    })
  })

  describe('Testing action POLL_JOB_URL', () => {
    afterEach(() => td.reset())

    it('RUNNING Job', done => {
      const jobUrl = '/api/v2/job/test_job'
      const job = {
        id: '1',
        status: 'RUNNING'
      }

      const state = {
        job: null
      }

      const get = td.function('api.get')
      td.when(get(jobUrl)).thenResolve(job)
      td.replace(api, 'get', get)

      testAction(actions.__POLL_JOB_URL__, jobUrl, state, [{type: UPDATE_JOB, payload: job}], [{
        type: POLL_JOB_URL,
        payload: jobUrl
      }], done)
    })

    it('PENDING Job', done => {
      const jobUrl = '/api/v2/job/test_job'
      const job = {
        id: '1',
        status: 'PENDING'
      }

      const state = {
        job: null
      }

      const get = td.function('api.get')
      td.when(get(jobUrl)).thenResolve(job)
      td.replace(api, 'get', get)

      testAction(actions.__POLL_JOB_URL__, jobUrl, state, [{type: UPDATE_JOB, payload: job}], [{
        type: POLL_JOB_URL,
        payload: jobUrl
      }], done)
    })

    it('FINISHED Job', done => {
      const jobUrl = '/api/v2/job/test_job'
      const job = {
        id: '1',
        status: 'FINISHED'
      }

      const state = {
        job: null
      }

      const get = td.function('api.get')
      td.when(get(jobUrl)).thenResolve(job)
      td.replace(api, 'get', get)

      testAction(actions.__POLL_JOB_URL__, jobUrl, state, [
        {type: UPDATE_JOB, payload: job},
        {type: ADD_FINISHED_JOB, payload: job}
      ], [], done)
    })

    it('FAILED Job', done => {
      const jobUrl = '/api/v2/job/test_job'
      const job = {
        id: '1',
        status: 'FAILED'
      }

      const state = {
        job: null
      }

      const get = td.function('api.get')
      td.when(get(jobUrl)).thenResolve(job)
      td.replace(api, 'get', get)

      testAction(actions.__POLL_JOB_URL__, jobUrl, state, [
        {type: UPDATE_JOB, payload: job},
        {type: ADD_FINISHED_JOB, payload: job}
      ], [], done)
    })
  })
})
