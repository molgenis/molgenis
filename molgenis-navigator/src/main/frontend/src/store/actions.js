// @flow
import type { ApiEntityType, ApiPackage, Item, State } from '../flow.types'
import { INITIAL_STATE } from './state'
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
// $FlowFixMe
import { encodeRsqlValue, transformToRSQL } from '@molgenis/rsql'
import { createAlertsFromApiError } from '../utils/AlertUtils'
import {
  createItemFromApiEntityType,
  createItemFromApiPackage
} from '../utils/ItemUtils'
import { createJobFromApiJobDownload } from '../utils/JobUtils'
import {
  ADD_ALERTS,
  SET_JOBS,
  SET_PACKAGE,
  SET_ITEMS,
  SET_SELECTED_ITEMS,
  RESET_CLIPBOARD
} from './mutations'
import { createAlertError } from '../models/Alert'
import { createPackageFromApiPackage } from '../utils/PackageUtils'

export const FETCH_ITEMS = '__FETCH_ITEMS__'
export const FETCH_ITEMS_BY_QUERY = '__FETCH_ITEMS_BY_QUERY__'
export const FETCH_ITEMS_BY_PACKAGE = '__FETCH_ITEMS_BY_PACKAGE__'
export const SELECT_ITEM = '__SELECT_ITEM__'
export const DESELECT_ITEM = '__DESELECT_ITEM__'
export const SELECT_ALL_ITEMS = '__SELECT_ALL_ITEMS__'
export const DESELECT_ALL_ITEMS = '__DESELECT_ALL_ITEMS__'
export const DELETE_SELECTED_ITEMS = '__DELETE_SELECTED_ITEMS__'
export const CREATE_PACKAGE = '__CREATE_PACKAGE__'
export const UPDATE_ITEM = '__UPDATE_ITEM__'
export const MOVE_CLIPBOARD_ITEMS = '__MOVE_CLIPBOARD_ITEMS__'
export const SCHEDULE_DOWNLOAD_SELECTED_ITEMS = '__SCHEDULE_DOWNLOAD_SELECTED_ITEMS__'

const SYS_PACKAGE_ID = 'sys'
const REST_API_V2 = '/api/v2'
const PACKAGE_ENDPOINT = REST_API_V2 + '/sys_md_Package'
const ENTITY_TYPE_ENDPOINT = REST_API_V2 + '/sys_md_EntityType'

function getEntityTypeUriByParentPackage (packageId: ?string) {
  let rsql
  if (packageId) {
    rsql = encodeRsqlValue(packageId)
  } else {
    rsql = '%22%22'
  }

  return ENTITY_TYPE_ENDPOINT + '?sort=label&num=1000&q=isAbstract==false;package==' + rsql
}

/**
 * Get a MOLGENIS rest api encoded query for the EntityType table
 * The query retrieves the first 1000 EntityTypes
 */
function getEntityTypeUriByQuery (query: ?string) {
  const baseEntityTypeUri = ENTITY_TYPE_ENDPOINT + '?sort=label&num=1000'

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
 *
 * Validating specific input for searchbox
 *
 * @param query query to send to api/v2
 */
function validateQuery (query: string) {
  if (query.indexOf('"') > -1) {
    throw new Error(
      'Double quotes not are allowed in queries, please use single quotes.')
  }
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

  return PACKAGE_ENDPOINT + '?sort=label&num=1000&q=parent==' + rsql
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
    packageUri = PACKAGE_ENDPOINT + '?sort=label&num=1000' + '&q=' + encodeRsqlValue(rsql)
  } else {
    packageUri = getPackageUriByParentPackage(null)
  }

  return packageUri
}

function fetchPackages (uri: string) {
  return api.get(uri).then(response => filterPackages(response.items).map(createItemFromApiPackage))
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
  return api.get(uri).then(response => filterEntityTypes(response.items).map(createItemFromApiEntityType))
}

function fetchPackageWithAncestors (packageId: ?string) {
  // TODO deal with deep packages (show ... in breadcrumb?)
  if (packageId) {
    const uri = PACKAGE_ENDPOINT + '/' + packageId + '?attrs=id,label,description,parent(id,label,parent(id,label,parent(id,label,parent(id,label,parent(id,label,parent(id,label)))))'
    return api.get(uri).then(response => createPackageFromApiPackage(response))
  } else {
    return new Promise((resolve, reject) => { resolve([]) })
  }
}

function fetchJob (job) {
  const entityType = job.type === 'download' ? 'sys_job_ScriptJobExecution' : 'blaat'
  return api.get(REST_API_V2 + '/' + entityType + '/' + job.id)
}

const pollJob = (commit, state, job) => {
  fetchJob(job).then(response => {
    let updatedJob = createJobFromApiJobDownload(response)
    const index = state.jobs.findIndex(job => job.type === updatedJob.type && job.id === updatedJob.id)
    let updatedJobs = state.jobs.slice()
    updatedJobs[index] = updatedJob
    commit(SET_JOBS, updatedJobs)

    switch (updatedJob.status) {
      case 'pending':
      case 'running':
        setTimeout(() => pollJob(commit, state, job), 500)
        break
      case 'success':
      case 'failed':
      case 'canceled':
        console.log('job finished with status ' + updatedJob.status)
        break
    }
  })
}

export default {
  [FETCH_ITEMS] ({state, dispatch}: { state: State, dispatch: Function }) {
    if (state.query) {
      dispatch(FETCH_ITEMS_BY_QUERY, state.query)
    } else {
      dispatch(FETCH_ITEMS_BY_PACKAGE, state.route.params.package)
    }
  },
  [FETCH_ITEMS_BY_QUERY] ({commit}: { commit: Function }, query: string) {
    try {
      validateQuery(query)
    } catch (error) {
      commit(ADD_ALERTS, [createAlertError(error.message)])
      return
    }

    const packageFetch = fetchPackagesByQuery(query)
    const entityTypeFetch = fetchEntityTypesByQuery(query)
    Promise.all([packageFetch, entityTypeFetch]).then(responses => {
      commit(SET_ITEMS, responses[0].concat(responses[1]))
    }).catch(error => {
      commit(ADD_ALERTS, createAlertsFromApiError(error))
    })
  },
  [FETCH_ITEMS_BY_PACKAGE] ({commit, dispatch}: { commit: Function, dispatch: Function }, packageId: ?string) {
    const packageFetch = fetchPackageWithAncestors(packageId)
    const packagesFetch = fetchPackagesByParentPackage(packageId)
    const entityTypesFetch = fetchEntityTypesByParentPackage(packageId)
    Promise.all([packageFetch, packagesFetch, entityTypesFetch]).then(responses => {
      commit(SET_PACKAGE, responses[0])
      commit(SET_ITEMS, responses[1].concat(responses[2]))
    }).catch(error => {
      commit(ADD_ALERTS, createAlertsFromApiError(error))
    })
  },
  [SELECT_ALL_ITEMS] ({commit, state}: { commit: Function, state: State }) {
    commit(SET_SELECTED_ITEMS, state.items.slice())
  },
  [DESELECT_ALL_ITEMS] ({commit}: { commit: Function }) {
    commit(SET_SELECTED_ITEMS, [])
  },
  [SELECT_ITEM] ({commit, state}: { commit: Function, state: State }, item: Item) {
    commit(SET_SELECTED_ITEMS, state.selectedItems.concat(item))
  },
  [DESELECT_ITEM] ({commit, state}: { commit: Function, state: State }, item: Item) {
    commit(SET_SELECTED_ITEMS, state.selectedItems.filter(selectedItem => !(selectedItem.type === item.type && selectedItem.id === item.id)))
  },
  [DELETE_SELECTED_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function }) {
    api.delete_('/plugin/navigator/delete', {
      body: JSON.stringify({
        packageIds: state.selectedItems.filter(item => item.type === 'package').map(item => item.id),
        entityTypeIds: state.selectedItems.filter(item => item.type === 'entityType').map(item => item.id)
      })
    }).then(() => {
      dispatch(FETCH_ITEMS)
    }, error => {
      commit(ADD_ALERTS, createAlertsFromApiError(error))
    })
  },
  [CREATE_PACKAGE] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    aPackage: Object) {
    const uri = REST_API_V2 + '/sys_md_Package'
    const options = {
      body: JSON.stringify({entities: [aPackage]})
    }
    api.post(uri, options).then(() => {
      dispatch(FETCH_ITEMS)
    }, error => {
      commit(ADD_ALERTS, createAlertsFromApiError(error))
    })
  },
  [UPDATE_ITEM] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    updatedItem: Item) {
    if (updatedItem.type === 'package') {
      const item = state.items.find(
        item => item.type === 'package' && item.id === updatedItem.id)
      if (item) {
        let promises = []
        if (item.label !== updatedItem.label) {
          const uri = REST_API_V2 + '/sys_md_Package/label'
          const options = {
            body: JSON.stringify({
              entities: [{
                id: item.id,
                label: updatedItem.label
              }]
            })
          }
          promises.push(api.put(uri, options))
        }
        if (item.description !== updatedItem.description) {
          const uri = REST_API_V2 + '/sys_md_Package/description'
          const options = {
            body: JSON.stringify({
              entities: [{
                id: item.id,
                description: updatedItem.description
              }]
            })
          }
          promises.push(api.put(uri, options))
        }
        Promise.all(promises).then(() => {
          dispatch(FETCH_ITEMS)
        }, error => {
          commit(ADD_ALERTS, createAlertsFromApiError(error))
        })
      }
    }
  },
  [MOVE_CLIPBOARD_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    targetPackageId: string) {
    if (state.clipboard && state.clipboard.items.length > 0) {
      const clipboardItems = state.clipboard.items
      let promises = []

      const packages = clipboardItems.filter(
        item => item.type === 'package')
      if (packages.length > 0) {
        const uri = REST_API_V2 + '/sys_md_Package/parent'
        const options = {
          body: JSON.stringify({
            entities: packages.map(
              item => ({
                id: item.id,
                parent: targetPackageId !== undefined ? targetPackageId : null
              }))
          })
        }
        promises.push(api.put(uri, options))
      }

      const entityTypes = clipboardItems.filter(
        item => item.type === 'entityType')
      if (entityTypes.length > 0) {
        const uri = REST_API_V2 + '/sys_md_EntityType/package'
        const options = {
          body: JSON.stringify({
            entities: entityTypes.map(
              item => ({
                id: item.id,
                package: targetPackageId !== undefined ? targetPackageId : null
              }))
          })
        }
        promises.push(api.put(uri, options))
      }

      Promise.all(promises).then(responses => {
        commit(RESET_CLIPBOARD)
        dispatch(FETCH_ITEMS)
      }).catch(error => {
        commit(ADD_ALERTS, createAlertsFromApiError(error))
      })
    }
  },
  [SCHEDULE_DOWNLOAD_SELECTED_ITEMS] ({commit, state}: { commit: Function, state: State }) {
    const dummyJob = {type: 'download', id: 'aaaacztxsh4nqabegilmlgaaae', status: 'pending'}
    pollJob(commit, state, dummyJob)
    commit(SET_SELECTED_ITEMS, [])
    // 1. Call download endpoint which will return job location
    // 2. Poll job location until end state is reached, show progress alert ...
    // 3. If success: download file
  }
}
