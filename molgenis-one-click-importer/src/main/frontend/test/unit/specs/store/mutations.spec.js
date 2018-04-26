import mutations from 'store/mutations'

describe('mutations', () => {
  describe('Testing mutation UPDATE_JOB', () => {
    it('Updates a job', () => {
      const state = {
        job: null
      }

      const job = {
        'id': '1',
        'name': 'test job',
        'start date': 'now'
      }

      mutations.__UPDATE_JOB__(state, job)
      expect(state.job).to.deep.equal(job)
    })
  })

  describe('Testing mutation ADD_FINISHED_JOB', () => {
    it('Adds a job to the front of the finished job array', () => {
      const state = {
        finishedJobs: [{
          'id': '1',
          'name': 'test job',
          'start date': 'now'
        }]
      }

      const job = {
        'id': '2',
        'name': 'test job',
        'start date': 'now'
      }

      const finishedJobs = [{
        'id': '2',
        'name': 'test job',
        'start date': 'now'
      }, {
        'id': '1',
        'name': 'test job',
        'start date': 'now'
      }]

      mutations.__ADD_FINISHED_JOB__(state, job)
      expect(state.finishedJobs).to.deep.equal(finishedJobs)
    })
  })
})
