// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import type {
  Alert,
  Folder,
  FolderState,
  Item,
  Job
} from '../flow.types'
import {
  createJobFromApiJobCopy,
  createJobFromApiJobDownload
} from './JobUtils'
import { AlertError } from './AlertError'

const NAVIGATOR_URI = '/plugin/navigator'
const REST_API_V2 = '/api/v2'

export function fetchJob (job: Job) {
  // TODO fix
  console.log(job)
  if (job.type === 'copy') {
    return api.get(
      REST_API_V2 + '/sys_job_OneClickImportJobExecution/' + job.id).then(
      response => createJobFromApiJobCopy(response))
  } else {
    return api.get(REST_API_V2 + '/blaat/' + job.id).then(
      response => createJobFromApiJobDownload(response))
  }
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
  }).catch(throwAlertError).then(createJobFromApiJobDownload)
}

export function deleteItems (items: Array<Item>): Promise<string> {
  return api.delete_(NAVIGATOR_URI + '/delete', {
    body: JSON.stringify({
      resources: items.map(item => toApiItemIdentifier(item))
    })
  }).catch(throwAlertError)
}

export function copyItems (items: Array<Item>, folder: Folder): Promise<Job> {
  return api.post(NAVIGATOR_URI + '/copy', {
    body: JSON.stringify({
      resources: items.map(item => toApiItemIdentifier(item)),
      targetFolderId: folder.id
    })
  }).catch(throwAlertError).then(createJobFromApiJobCopy)
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

function throwAlertError (response: Object): Alert {
  const alerts = response.errors.map(
    error => ({type: 'ERROR', message: error.message, code: error.code}))
  throw new AlertError(alerts)
}

// map navigator types to API types
function toApiItem (item: Item) {
  return item
}

function toApiItemIdentifier (item: Item) {
  return {
    id: item.id,
    type: item.type
  }
}

export function toApiPackage (item: Item) {
  return {
    id: item.id,
    label: item.label,
    description: item.description,
    parent: undefined
  }
}