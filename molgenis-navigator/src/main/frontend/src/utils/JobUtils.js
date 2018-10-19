// @flow
import type { Job, JobStatus } from '../flow.types'
import { createJobCopy, createJobDownload } from '../models/Job'

function toJobStatus (apiJobStatus: string): JobStatus {
  let jobStatus
  switch (apiJobStatus) {
    case 'PENDING':
      jobStatus = 'pending'
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
      jobStatus = 'canceled'
      break
    default:
      throw new Error('unexpected job status ' + apiJobStatus)
  }
  return jobStatus
}

export function createCopyJobFromApiCopy (apiJobCopy: Object): Job {
  return createJobCopy(apiJobCopy.jobId, toJobStatus(apiJobCopy.jobStatus))
}

export function createJobFromApiJobDownload (apiJobDownload: Object): Job {
  return createJobDownload(apiJobDownload.identifier, toJobStatus(apiJobDownload.status))
}
