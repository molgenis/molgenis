// @flow
import type { Item, Entity, Package, State } from '../flow.types'
import { INITIAL_STATE } from './state'
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
// $FlowFixMe
import { encodeRsqlValue, transformToRSQL } from '@molgenis/rsql'
import {
  SET_ERROR,
  SET_PATH,
  SET_ITEMS,
  SET_SELECTED_ITEMS
} from './mutations'

export const FETCH_ITEMS = '__FETCH_ITEMS__'
export const FETCH_ITEMS_BY_QUERY = '__FETCH_ITEMS_BY_QUERY__'
export const FETCH_ITEMS_BY_PACKAGE = '__FETCH_ITEMS_BY_PACKAGE__'
export const SELECT_ITEM = '__SELECT_ITEM__'
export const DESELECT_ITEM = '__DESELECT_ITEM__'
export const SELECT_ALL_ITEMS = '__SELECT_ALL_ITEMS__'
export const DESELECT_ALL_ITEMS = '__DESELECT_ALL_ITEMS__'
export const DELETE_SELECTED_ITEMS = '__DELETE_SELECTED_ITEMS__'
export const CREATE_PACKAGE = '__CREATE_PACKAGE__'
export const UPDATE_PACKAGE = '__UPDATE_PACKAGE__'
export const MOVE_CLIPBOARD_ITEMS = '__MOVE_CLIPBOARD_ITEMS__'

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
 * @returns {Array.<Package>}
 */
function filterPackages (packages: Array<Package>) {
  if (INITIAL_STATE.isSuperUser) {
    return packages
  }

  return packages
  .filter(_package => _package.id !== SYS_PACKAGE_ID)
  .filter(_package => !_package.id.startsWith(SYS_PACKAGE_ID + '_'))
}

/**
 * Filter out all system entities unless user is superUser
 * @param entities
 * @returns {Array.<Entity>}
 */
function filterEntityTypes (entities: Array<Entity>) {
  if (INITIAL_STATE.isSuperUser) {
    return entities
  }

  return entities.filter(entity => !entity.id.startsWith(SYS_PACKAGE_ID + '_'))
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

function createPackageItem (aPackage: Package) {
  return {
    type: 'package',
    id: aPackage.id,
    label: aPackage.label,
    description: aPackage.description
  }
}

function fetchPackages (uri: string) {
  return api.get(uri).then(response => filterPackages(response.items).map(createPackageItem))
}

function fetchEntityTypesByParentPackage (packageId: ?string) {
  const uri = getEntityTypeUriByParentPackage(packageId)
  return fetchEntityTypes(uri)
}

function fetchEntityTypesByQuery (query: ?string) {
  const uri = getEntityTypeUriByQuery(query)
  return fetchEntityTypes(uri)
}

function createEntityTypeItem (aPackage: Package) {
  return {
    type: 'entityType',
    id: aPackage.id,
    label: aPackage.label,
    description: aPackage.description
  }
}

function fetchEntityTypes (uri: string) {
  return api.get(uri).then(response => filterEntityTypes(response.items).map(createEntityTypeItem))
}

function getPackagePath (aPackage: Package) {
  let path = []
  getPackagePathRec(aPackage, path)
  return path.reverse()
}

function getPackagePathRec (aPackage: Package, path: Array<Package>) {
  path.push({id: aPackage.id, label: aPackage.label})
  if (aPackage.parent) {
    getPackagePathRec(aPackage.parent, path)
  }
}

function fetchPackagePath (packageId: ?string) {
  if (packageId) {
    const uri = PACKAGE_ENDPOINT + '/' + packageId + '?attrs=id,label,description,parent(id,label,parent(id,label,parent(id,label,parent(id,label,parent(id,label,parent(id,label)))))'
    return api.get(uri).then(response => getPackagePath(response))
  } else {
    return new Promise((resolve, reject) => { resolve([]) })
  }
}

export default {
  [FETCH_ITEMS] ({state, dispatch}: { state: State, dispatch: Function }) {
    if (state.query) {
      dispatch(FETCH_ITEMS_BY_QUERY, state.query)
    } else {
      dispatch(FETCH_ITEMS_BY_PACKAGE, state.route.params.package)
    }
  },
  [FETCH_ITEMS_BY_QUERY] ({commit}: { commit: Function }, query: ?string) {
    if (query) {
      try {
        validateQuery(query)
      } catch (error) {
        commit(SET_ERROR, error.message)
      }
    }

    const packageFetch = fetchPackagesByQuery(query)
    const entityTypeFetch = fetchEntityTypesByQuery(query)
    Promise.all([packageFetch, entityTypeFetch]).then(responses => {
      commit(SET_ITEMS, responses[0].concat(responses[1]))
    }).catch(error => {
      commit(SET_ERROR, error)
    })
  },
  [FETCH_ITEMS_BY_PACKAGE] ({commit}: { commit: Function }, packageId: ?string) {
    const pathFetch = fetchPackagePath(packageId)
    const packageFetch = fetchPackagesByParentPackage(packageId)
    const entityTypeFetch = fetchEntityTypesByParentPackage(packageId)
    Promise.all([pathFetch, packageFetch, entityTypeFetch]).then(responses => {
      commit(SET_PATH, responses[0])
      commit(SET_ITEMS, responses[1].concat(responses[2]))
    }).catch(error => {
      commit(SET_ERROR, error)
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
      commit(SET_ERROR, error)
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
      commit(SET_ERROR, error)
    })
  },
  [UPDATE_PACKAGE] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    aPackage: Object) {
    const uri = REST_API_V2 + '/sys_md_Package'
    const options = {
      body: JSON.stringify({entities: [aPackage]})
    }
    api.put(uri, options).then(() => {
      dispatch(FETCH_ITEMS)
    }, error => {
      commit(SET_ERROR, error)
    })
  },
  [MOVE_CLIPBOARD_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    targetPackageId: string) {
    if (state.clipboard.packageIds && state.clipboard.packageIds.length > 0) {
      const uri = REST_API_V2 + '/sys_md_Package/parent'
      const options = {
        body: JSON.stringify({
          entities: state.clipboard.packageIds.map(
            packageId => ({id: packageId, parent: targetPackageId !== undefined ? targetPackageId : null}))
        })
      }
      api.put(uri, options).then(() => {
        dispatch(FETCH_ITEMS)
      }, error => {
        commit(SET_ERROR, error)
      })
    }
  }
}
