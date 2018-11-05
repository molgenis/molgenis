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
  const uri = folderId ? '/plugin/navigator/get?folderId=' + encodeURIComponent(
    folderId) : '/plugin/navigator/get'
  return api.get(uri).catch(throwAlertError).then(toFolderState)
}

export function getItemsByQuery (query: string): Promise<FolderState> {
  const uri = '/plugin/navigator/search?query=' + encodeURIComponent(query)
  return api.get(uri).catch(throwAlertError).then(toFolderState)
}

// TODO cleanup
export function createItem (item: Item, folder: ?Folder) {
  let promise
  if (item.type === 'package') {
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
  return api.put('/plugin/navigator/update', {
    body: JSON.stringify({
      resource: toApiItem(updatedItem, true)
    })
  }).catch(throwAlertError)
}

export function downloadItems (items: Array<Item>): Promise<Job> {
  return api.post('/plugin/navigator/download', {
    body: JSON.stringify({
      resources: items.map(item => toApiItem(item))
    })
  }).catch(throwAlertError).then(createJobFromApiJobDownload)
}

export function deleteItems (items: Array<Item>): Promise<string> {
  return api.delete_('/plugin/navigator/delete', {
    body: JSON.stringify({
      resources: items.map(item => toApiItem(item))
    })
  }).catch(throwAlertError)
}

export function copyItems (items: Array<Item>, folder: Folder): Promise<Job> {
  return api.post('/plugin/navigator/copy', {
    body: JSON.stringify({
      resources: items.map(item => toApiItem(item)),
      targetFolderId: folder.id
    })
  }).catch(throwAlertError).then(createJobFromApiJobCopy)
}

export function moveItems (items: Array<Item>,
  folder: Folder): Promise<string> {
  return api.post('/plugin/navigator/move', {
    body: JSON.stringify({
      resources: items.map(item => toApiItem(item)),
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
function toApiItem (item: Item, allProperties: boolean = false) {
  let apiItemType
  switch (item.type) {
    case 'package':
      apiItemType = 'PACKAGE'
      break
    case 'entityType':
      apiItemType = 'ENTITY_TYPE'
      break
    default:
      throw new Error('unexpected item type ' + item.type)
  }

  return {
    id: item.id,
    type: apiItemType,
    label: allProperties ? item.label : undefined,
    description: allProperties ? item.description : undefined
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
