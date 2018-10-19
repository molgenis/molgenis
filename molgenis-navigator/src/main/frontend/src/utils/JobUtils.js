// @flow
import type { Job, JobStatus } from '../flow.types'
import { createJobCopy, createJobDownload } from '../models/Job'

function toJobStatus (apiJobStatus: string): JobStatus {
  let jobStatus
  switch (apiJobStatus) {
    case 'PENDING':
      jobStatus = 'running'
      break
    case 'RUNNING':
      jobStatus = 'running'
      break
    case 'SUCCESS':
      jobStatus = 'success'
      break
    case 'FAILED':
      jobStatus = 'failed'
      break
    case 'CANCELED':
      jobStatus = 'failed'
      break
    default:
      throw new Error('unexpected job status ' + apiJobStatus)
  }
  return jobStatus
}

export function createCopyJobFromApiCopy (apiCopy: Object): Job {
  return createJobCopy(apiCopy.jobId, toJobStatus(apiCopy.jobStatus))
}

export function createJobFromApiJobCopy (apiJobCopy: Object): Job {
  return createJobCopy(apiJobCopy.identifier, toJobStatus(apiJobCopy.status))
}

export function createJobFromApiJobDownload (apiJobDownload: Object): Job {
  return createJobDownload(apiJobDownload.identifier, toJobStatus(apiJobDownload.status))
}
