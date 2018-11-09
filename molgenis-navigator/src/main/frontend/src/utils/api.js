// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import type {
  Alert,
  Folder,
  FolderState,
  Item,
  Job, JobStatus, JobType
} from '../flow.types'
import { AlertError } from './AlertError'

const NAVIGATOR_URI = '/plugin/navigator'
const REST_API_V2 = '/api/v2'

export function fetchJob (job: Job): Promise<Job> {
  const uri = REST_API_V2 + '/' + toApiJobEntityType(job) + '/' + job.id
  return api.get(uri).catch(throwAlertError).then(toJob)
}

export function getItemsByFolderId (folderId: ?string): Promise<FolderState> {
  const uri = folderId ? NAVIGATOR_URI + '/get?folderId=' + encodeURIComponent(
    folderId) : NAVIGATOR_URI + '/get'
  return api.get(uri).catch(throwAlertError).then(toFolderState)
}

export function getItemsByQuery (query: string): Promise<FolderState> {
  const uri = NAVIGATOR_URI + '/search?query=' + encodeURIComponent(query)
  return api.get(uri).catch(throwAlertError).then(toFolderState)
}

// TODO cleanup
export function createItem (item: Item, folder: ?Folder) {
  let promise
  if (item.type === 'PACKAGE') {
    const packageEntity = toApiPackage(item)
    packageEntity.parent = folder ? folder.id : null

    const uri = REST_API_V2 + '/sys_md_Package'
    const options = {
      body: JSON.stringify({entities: [packageEntity]})
    }
    promise = api.post(uri, options).catch(throwAlertError)
  } else {
    promise = Promise.resolve()
  }
  return promise
}

export function updateItem (item: Item, updatedItem: Item) {
  return api.put(NAVIGATOR_URI + '/update', {
    body: JSON.stringify({
      resource: toApiItem(updatedItem)
    })
  }).catch(throwAlertError)
}

export function downloadItems (items: Array<Item>): Promise<Job> {
  return api.post(NAVIGATOR_URI + '/download', {
    body: JSON.stringify({
      resources: items.map(item => toApiItemIdentifier(item))
    })
  }).catch(throwAlertError).then(toJob)
}

export function deleteItems (items: Array<Item>): Promise<string> {
  return api.delete_(NAVIGATOR_URI + '/delete', {
    body: JSON.stringify({
      resources: items.map(item => toApiItemIdentifier(item))
    })
  }).catch(throwAlertError)
}

export function copyItems (items: Array<Item>, folder: ?Folder): Promise<Job> {
  return api.post(NAVIGATOR_URI + '/copy', {
    body: JSON.stringify({
      resources: items.map(item => toApiItemIdentifier(item)),
      targetFolderId: folder ? folder.id : null
    })
  }).catch(throwAlertError).then(toJob)
}

export function moveItems (items: Array<Item>,
  folder: Folder): Promise<string> {
  return api.post(NAVIGATOR_URI + '/move', {
    body: JSON.stringify({
      resources: items.map(item => toApiItemIdentifier(item)),
      targetFolderId: folder.id
    })
  }).catch(throwAlertError)
}

// map API types to navigator types
function toFolderState (response: Object): FolderState {
  return response
}

function toJob (response: Object): Job {
  return {
    type: toJobType(response),
    id: response.identifier,
    status: toJobStatus(response),
    progress: response.progressInt,
    progressMax: response.progressMax,
    resultUrl: response.resultUrl
  }
}

function toJobType (response: Object): JobType {
  let type
  switch (response.type) {
    case 'Copy':
      type = 'copy'
      break
    case 'DownloadJob':
      type = 'download'
      break
    default:
      throw new Error('Unknown job type \'' + response._meta.name + '\'')
  }
  return type
}

function toJobStatus (response: Object): JobStatus {
  let jobStatus
  switch (response.status) {
    case 'PENDING':
    case 'RUNNING':
      jobStatus = 'running'
      break
    case 'SUCCESS':
      jobStatus = 'success'
      break
    case 'FAILED':
    case 'CANCELED':
      jobStatus = 'failed'
      break
    default:
      throw new Error('unexpected job status \'' + response.status + '\'')
  }
  return jobStatus
}

function throwAlertError (response: Object): Alert {
  const alerts = response.errors.map(
    error => ({type: 'ERROR', message: error.message, code: error.code}))
  throw new AlertError(alerts)
}

// map navigator types to API types
function toApiItem (item: Item): Object {
  return item
}

function toApiItemIdentifier (item: Item): Object {
  return {
    id: item.id,
    type: item.type
  }
}

function toApiPackage (item: Item): Object {
  return {
    id: item.id,
    label: item.label,
    description: item.description,
    parent: undefined
  }
}

function toApiJobEntityType (job: Job): string {
  let apiType
  switch (job.type) {
    case 'copy':
      apiType = 'sys_job_CopyJobExecution'
      break
    case 'download':
      apiType = 'sys_job_DownloadJobExecution'
      break
    default:
      throw new Error('unexpected job type \'' + job.type + '\'')
  }
  return apiType
}
