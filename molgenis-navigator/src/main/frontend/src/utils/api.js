// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
// $FlowFixMe
import { encodeRsqlValue, transformToRSQL } from '@molgenis/rsql'
import type { ApiEntityType, ApiPackage, Folder, Item, Job } from '../flow.types'
import { INITIAL_STATE } from '../store/state'
import {
  createItemFromApiEntityType,
  createItemFromApiPackage,
  createRestApiPackageFromItem,
  toApiItem
} from './ItemUtils'
import { createFolderFromApiPackage } from './FolderUtils'
import { createJobFromApiJobCopy, createJobFromApiJobDownload } from './JobUtils'
import { AlertError, createAlertErrorFromApiError } from './AlertUtils'
import { createAlertError } from '../models/Alert'

const SYS_PACKAGE_ID = 'sys'
const REST_API_V2 = '/api/v2'
const PACKAGE_ENDPOINT = REST_API_V2 + '/sys_md_Package'
const ENTITY_TYPE_ENDPOINT = REST_API_V2 + '/sys_md_EntityType'

export function fetchJob (job: Job) {
  // TODO fix
  console.log(job)
  if (job.type === 'copy') {
    return api.get(REST_API_V2 + '/sys_job_OneClickImportJobExecution/' + job.id).then(response => createJobFromApiJobCopy(response))
  } else {
    return api.get(REST_API_V2 + '/blaat/' + job.id).then(response => createJobFromApiJobDownload(response))
  }
}

export function getFolder (folderId: ?string): Promise<?Folder> {
  return fetchPackageWithAncestors(folderId)
}

export function getItemsByFolderId (folderId: ?string) {
  const packagesFetch = fetchPackagesByParentPackage(folderId)
  const entityTypesFetch = fetchEntityTypesByParentPackage(folderId)
  return Promise.all([packagesFetch, entityTypesFetch]).then(responses => responses[0].concat(responses[1]))
}

export function getItemsByQuery (query: string): Promise<Item> {
  validateQuery(query)
  const packageFetch = fetchPackagesByQuery(query)
  const entityTypeFetch = fetchEntityTypesByQuery(query)
  return Promise.all([packageFetch, entityTypeFetch]).then(responses => responses[0].concat(responses[1]))
}

export function createItem (item: Item, folder: Folder) {
  let promise
  if (item.type === 'package') {
    const packageEntity = createRestApiPackageFromItem(item)
    packageEntity.parent = folder.id

    const uri = REST_API_V2 + '/sys_md_Package'
    const options = {
      body: JSON.stringify({entities: [packageEntity]})
    }
    promise = api.post(uri, options).catch(handleApiErrorResponse)
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
  }).catch(handleApiErrorResponse)
}

export function downloadItems (items: Array<Item>): Promise<Job> {
  return api.post('/plugin/navigator/download', {
    body: JSON.stringify({
      resources: items.map(item => toApiItem(item))
    })
  }).catch(handleApiErrorResponse).then(createJobFromApiJobDownload)
}

export function deleteItems (items: Array<Item>): Promise<string> {
  return api.delete_('/plugin/navigator/delete', {
    body: JSON.stringify({
      resources: items.map(item => toApiItem(item))
    })
  }).catch(handleApiErrorResponse)
}

export function copyItems (items: Array<Item>, folder: Folder): Promise<Job> {
  return api.post('/plugin/navigator/copy', {
    body: JSON.stringify({
      resources: items.map(item => toApiItem(item)),
      targetFolderId: folder.id
    })
  }).catch(handleApiErrorResponse).then(createJobFromApiJobCopy)
}

export function moveItems (items: Array<Item>, folder: Folder): Promise<string> {
  return api.post('/plugin/navigator/move', {
    body: JSON.stringify({
      resources: items.map(item => toApiItem(item)),
      targetFolderId: folder.id
    })
  }).catch(handleApiErrorResponse)
}

function getEntityTypeUriByParentPackage (packageId: ?string) {
  let rsql
  if (packageId) {
    rsql = encodeRsqlValue(packageId)
  } else {
    rsql = '%22%22'
  }

  return ENTITY_TYPE_ENDPOINT + '?attrs=id,label,description&sort=label&num=1000&q=isAbstract==false;package==' + rsql
}

/**
 * Get a MOLGENIS rest api encoded query for the EntityType table
 * The query retrieves the first 1000 EntityTypes
 */
function getEntityTypeUriByQuery (query: ?string) {
  const baseEntityTypeUri = ENTITY_TYPE_ENDPOINT + '?attrs=id,label,description&sort=label&num=1000'

  let entityTypeUri
  if (query) {
    const rsql = transformToRSQL({
      operator: 'AND',
      operands: [
        {
          operator: 'OR',
          operands: [
            {selector: 'label', comparison: '=q=', arguments: query},
            {selector: 'description', comparison: '=q=', arguments: query}
          ]
        },
        {selector: 'isAbstract', comparison: '==', arguments: 'false'}]
    })
    entityTypeUri = baseEntityTypeUri + '&q=' + encodeRsqlValue(rsql)
  } else {
    entityTypeUri = getEntityTypeUriByParentPackage(null)
  }

  return entityTypeUri
}

/**
 * Filter out system package unless user is superUser
 * @param packages
 * @returns {Array.<ApiPackage>}
 */
function filterPackages (packages: Array<ApiPackage>) {
  if (INITIAL_STATE.isSuperUser) {
    return packages
  }

  return packages
    .filter(_package => _package.id !== SYS_PACKAGE_ID)
    .filter(_package => !_package.id.startsWith(SYS_PACKAGE_ID + '_'))
}

/**
 * Filter out all system entity types unless user is superUser
 * @param entityTypes
 * @returns {Array.<ApiEntityType>}
 */
function filterEntityTypes (entityTypes: Array<ApiEntityType>) {
  if (INITIAL_STATE.isSuperUser) {
    return entityTypes
  }

  return entityTypes.filter(entityType => !entityType.id.startsWith(SYS_PACKAGE_ID + '_'))
}

function fetchPackagesByParentPackage (packageId: ?string) {
  const uri = getPackageUriByParentPackage(packageId)
  return fetchPackages(uri)
}

function getPackageUriByParentPackage (packageId: ?string) {
  let rsql
  if (packageId) {
    rsql = encodeRsqlValue(packageId)
  } else {
    rsql = '%22%22'
  }

  return PACKAGE_ENDPOINT + '?attrs=id,label,description&sort=label&num=1000&q=parent==' + rsql
}

function fetchPackagesByQuery (query: ?string) {
  const uri = getPackageUriByQuery(query)
  return fetchPackages(uri)
}

/**
 * Get a MOLGENIS rest api encoded query for the Package table
 * The query retrieves the first 1000 packages
 */
function getPackageUriByQuery (query: ?string) {
  let packageUri

  if (query) {
    const rsql = transformToRSQL({
      operator: 'OR',
      operands: [
        {selector: 'id', comparison: '=q=', arguments: query},
        {selector: 'label', comparison: '=q=', arguments: query},
        {selector: 'description', comparison: '=q=', arguments: query}
      ]
    })
    packageUri = PACKAGE_ENDPOINT + '?attrs=id,label,description&sort=label&num=1000' + '&q=' + encodeRsqlValue(rsql)
  } else {
    packageUri = getPackageUriByParentPackage(null)
  }

  return packageUri
}

function handleApiErrorResponse (response: Object) {
  throw createAlertErrorFromApiError(response)
}

function fetchPackages (uri: string) {
  return api.get(uri).then(response => filterPackages(response.items).map(createItemFromApiPackage)).catch(handleApiErrorResponse)
}

function fetchEntityTypesByParentPackage (packageId: ?string) {
  const uri = getEntityTypeUriByParentPackage(packageId)
  return fetchEntityTypes(uri)
}

function fetchEntityTypesByQuery (query: ?string) {
  const uri = getEntityTypeUriByQuery(query)
  return fetchEntityTypes(uri)
}

function fetchEntityTypes (uri: string) {
  return api.get(uri).then(response => filterEntityTypes(response.items).map(createItemFromApiEntityType)).catch(handleApiErrorResponse)
}

function fetchPackageWithAncestors (packageId: ?string): Promise<?Folder> {
  // TODO deal with deep packages (show ... in breadcrumb?)
  if (packageId) {
    const uri = PACKAGE_ENDPOINT + '/' + packageId + '?attrs=id,label,description,parent(id,label,parent(id,label,parent(id,label,parent(id,label,parent(id,label,parent(id,label)))))'
    return api.get(uri).then(response => createFolderFromApiPackage(response))
  } else {
    return Promise.resolve(null)
  }
}

/**
 *
 * Validating specific input for searchbox
 *
 * @param query query to send to api/v2
 */
function validateQuery (query: string) {
  if (query.indexOf('"') > -1) {
    throw new AlertError([{
      type: 'ERROR',
      code: null,
      message: 'Double quotes not are allowed in queries, please use single quotes.'
    }]) // TODO i18n
  }
}
