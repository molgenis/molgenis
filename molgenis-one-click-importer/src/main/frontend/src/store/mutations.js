export const UPDATE_JOB = '__UPDATE_JOB__'
export const ADD_FINISHED_JOB = '__ADD_FINISHED_JOB__'

export default {
  [UPDATE_JOB] (state, job) {
    state.job = job
  },
  [ADD_FINISHED_JOB] (state, job) {
    state.finishedJobs.unshift(job)
  }
}
