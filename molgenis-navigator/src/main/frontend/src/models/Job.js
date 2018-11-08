// @flow
import type { Job, JobType, JobStatus } from '../flow.types'

export function createJob (type: JobType, id: string, status: JobStatus, resultUrl: ?string): Job {
  return {type: type, id: id, status: status, resultUrl: resultUrl}
}

export function createJobCopy (id: string, status: JobStatus): Job {
  return createJob('copy', id, status, null)
}

export function createJobDownload (id: string, status: JobStatus, resultUrl: ?string): Job {
  return createJob('download', id, status, resultUrl)
}
