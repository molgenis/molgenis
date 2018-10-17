// @flow
import type { Job, JobType, JobStatus } from '../flow.types'

export function createJob (type: JobType, id: string, status: JobStatus): Job {
  return {type: type, id: id, status: status}
}

export function createJobDownload (id: string, status: JobStatus): Job {
  return createJob('download', id, status)
}
