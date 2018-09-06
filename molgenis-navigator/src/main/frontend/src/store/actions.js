// @flow
import type { Entity, Package, State } from '../flow.types'
import { INITIAL_STATE } from './state'
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
// $FlowFixMe
import { transformToRSQL, encodeRsqlValue } from '@molgenis/rsql'
import {
  RESET_PATH,
  SET_ENTITIES,
  SET_ERROR,
  SET_PACKAGES,
  SET_PATH,
  SET_QUERY,
  SET_SELECTED_ENTITY_TYPES,
  SET_SELECTED_PACKAGES
} from './mutations'

export const QUERY_PACKAGES = '__QUERY_PACKAGES__'
export const QUERY_ENTITIES = '__QUERY_ENTITIES__'
export const RESET_STATE = '__RESET_STATE__'
export const GET_STATE_FOR_PACKAGE = '__GET_STATE_FOR_PACKAGE__'
export const GET_ENTITIES_IN_PACKAGE = '__GET_ENTITIES_IN_PACKAGE__'
export const GET_ENTITY_PACKAGES = '__GET_ENTITY_PACKAGES__'
export const SELECT_ALL_PACKAGES_AND_ENTITY_TYPES = '__SELECT_ALL_PACKAGES_AND_ENTITY_TYPES__'
export const SELECT_ENTITY_TYPE = '__SELECT_ENTITY_TYPE__'
export const SELECT_PACKAGE = '__SELECT_PACKAGE__'
export const DESELECT_ALL_PACKAGES_AND_ENTITY_TYPES = '__DESELECT_ALL_PACKAGES_AND_ENTITY_TYPES__'
export const DESELECT_ENTITY_TYPE = '__DESELECT_ENTITY_TYPE__'
export const DESELECT_PACKAGE = '__DESELECT_PACKAGE__'
export const DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES = '__DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES__'

const SYS_PACKAGE_ID = 'sys'
const REST_API_V2 = '/api/v2'
const PACKAGE_ENDPOINT = REST_API_V2 + '/sys_md_Package'
const ENTITY_TYPE_ENDPOINT = REST_API_V2 + '/sys_md_EntityType'

/**
 * Recursively build the path, going backwards starting at the currentPackage
 *
 * @param packages, the complete list of packages
 * @param currentPackage, the tail
 * @param path the path where building
 * @returns path, in order array of packages (grandparent, parent, child, ....)
 */
function buildPath (packages, currentPackage: Package, path: Array<Package>) {
  if (currentPackage.parent) {
    const currentParent = currentPackage.parent
    const parentPackage = packages.find(function (packageItem) {
      return packageItem.id === currentParent.id
    })
    path = buildPath(packages, parentPackage, path)
  }
  path.push(currentPackage)
  return path
}

/**
 * Transform the result to an Entity object
 *
 * @param item result row form query to backend
 * @returns {{id: *, type: string, label: *, description: *}}
 */
function toEntity (item: any) {
  return {
    'id': item.id,
    'type': 'entity',
    'label': item.label,
    'description': item.description
  }
}

/**
 * Get a MOLGENIS rest api encoded query for the Package table
 * The query retrieves the first 1000 packages
 *
 * @param query
 */
function getPackageQuery (query: string) {
  const rsql = transformToRSQL({
    operator: 'OR',
    operands: [
      {selector: 'id', comparison: '=q=', arguments: query},
      {selector: 'label', comparison: '=q=', arguments: query},
      {selector: 'description', comparison: '=q=', arguments: query}
    ]
  })

  return PACKAGE_ENDPOINT + '?sort=label&num=1000&q=' + encodeRsqlValue(rsql)
}

/**
 * Get a MOLGENIS rest api encoded query for the EntityType table
 * The query retrieves the first 1000 EntityTypes
 *
 * @param query
 */
function getEntityTypeQuery (query: string) {
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

  return ENTITY_TYPE_ENDPOINT + '?sort=label&num=1000&q=' + encodeRsqlValue(rsql)
}

/**
 *
 * Validating specific input for searchbox
 *
 * @param query query to send to api/v2
 */
function validateQuery (query: string) {
  if (query.indexOf('"') > -1) {
    throw new Error('Double quotes not are allowed in queries, please use single quotes.')
  }
}

/**
 * Filter out system package unless user is superUser
 * @param packages
 * @returns {Array.<Package>}
 */
function filterNonVisiblePackages (packages: Array<Package>) {
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
function filterNonVisibleEntities (entities: Array<Entity>) {
  if (INITIAL_STATE.isSuperUser) {
    return entities
  }

  return entities.filter(entity => !entity.id.startsWith(SYS_PACKAGE_ID + '_'))
}

export default {
  [QUERY_PACKAGES] ({commit}: { commit: Function }, query: ?string) {
    let uri

    if (!query) {
      uri = PACKAGE_ENDPOINT + '?sort=label&num=1000'
    } else {
      try {
        validateQuery(query)
        uri = getPackageQuery(query)
        api.get(uri).then(response => {
          commit(SET_PACKAGES, filterNonVisiblePackages(response.items))
        }, error => {
          commit(SET_ERROR, error)
        })
      } catch (error) {
        commit(SET_ERROR, error.message)
      }
    }
  },
  [QUERY_ENTITIES] ({commit}: { commit: Function }, query: string) {
    if (query) {
      try {
        validateQuery(query)
        api.get(getEntityTypeQuery(query)).then(response => {
          const entities = response.items.map(toEntity)
          commit(SET_ENTITIES, filterNonVisibleEntities(entities))
        }, error => {
          commit(SET_ERROR, error)
        })
      } catch (error) {
        commit(SET_ERROR, error.message)
      }
    }
  },
  [GET_ENTITIES_IN_PACKAGE] ({commit}: { commit: Function }, packageId: string) {
    api.get(ENTITY_TYPE_ENDPOINT + '?sort=label&num=1000&&q=isAbstract==false;package==' + packageId).then(response => {
      const entities = response.items.map(toEntity)
      commit(SET_ENTITIES, entities)
    }, error => {
      commit(SET_ERROR, error)
    })
  },
  [RESET_STATE] ({commit}: { commit: Function }) {
    api.get(PACKAGE_ENDPOINT + '?sort=label&num=1000&&q=parent==%22%22').then(response => {
      const visibleRootPackages = filterNonVisiblePackages(response.items)
      commit(SET_PACKAGES, visibleRootPackages)
    }, error => {
      commit(SET_ERROR, error)
    })
    api.get(ENTITY_TYPE_ENDPOINT + '?sort=label&num=1000&&q=isAbstract==false;package==%22%22').then(response => {
      const entities = response.items.map(toEntity)
      const visibleRootEntities = filterNonVisibleEntities(entities)
      commit(SET_ENTITIES, visibleRootEntities)
    }, error => {
      commit(SET_ERROR, error)
    })

    commit(RESET_PATH)
  },
  [GET_ENTITY_PACKAGES] ({commit, dispatch}: { commit: Function, dispatch: Function }, lookupId: string) {
    api.get(ENTITY_TYPE_ENDPOINT + '?num=1000&&q=isAbstract==false;id==' + lookupId).then(response => {
      // At the moment each entity is stored in either a single package, or no package at all
      if (response.items.length > 0) {
        const entityType = response.items[0]
        const _package = entityType['package']
        if (_package) {
          dispatch(GET_STATE_FOR_PACKAGE, _package.id)
        } else {
          // In case entity is not in package fallback to searching for entity name.
          const entityLabel = entityType.label
          commit(SET_QUERY, entityLabel)
          dispatch(QUERY_ENTITIES, entityLabel)
        }
      } else {
        dispatch(RESET_STATE)
      }
    }, error => {
      commit(SET_ERROR, error)
    })
  },
  [GET_STATE_FOR_PACKAGE] ({commit, dispatch}: { commit: Function, dispatch: Function }, selectedPackageId: ?string) {
    api.get(PACKAGE_ENDPOINT + '?sort=label&num=1000').then(response => {
      const packages = filterNonVisiblePackages(response.items)

      if (!selectedPackageId) {
        dispatch(RESET_STATE)
      } else {
        const selectedPackage = packages.find(function (packageItem) {
          return packageItem.id === selectedPackageId
        })

        if (!selectedPackage) {
          commit(SET_ERROR, 'couldn\'t find package.')
          dispatch(RESET_STATE)
        } else {
          // Find child packages.
          const childPackages = packages.filter(function (packageItem) {
            return packageItem.parent && packageItem.parent.id === selectedPackage.id
          })

          commit(SET_PACKAGES, childPackages)

          const path = buildPath(packages, selectedPackage, [])
          commit(SET_PATH, path)
          dispatch(GET_ENTITIES_IN_PACKAGE, selectedPackageId)
        }
      }
    }, error => {
      commit(SET_ERROR, error)
    })
  },
  [SELECT_ALL_PACKAGES_AND_ENTITY_TYPES] ({commit, state}: { commit: Function, state: State }) {
    commit(SET_SELECTED_PACKAGES, state.packages.map(aPackage => aPackage.id))
    commit(SET_SELECTED_ENTITY_TYPES, state.entities.map(entityType => entityType.id))
  },
  [DESELECT_ALL_PACKAGES_AND_ENTITY_TYPES] ({commit}: { commit: Function }) {
    commit(SET_SELECTED_PACKAGES, [])
    commit(SET_SELECTED_ENTITY_TYPES, [])
  },
  [SELECT_ENTITY_TYPE] ({commit, state}: { commit: Function, state: State }, entityTypeId: string) {
    commit(SET_SELECTED_ENTITY_TYPES, state.selectedEntityTypeIds.concat([entityTypeId]))
  },
  [DESELECT_ENTITY_TYPE] ({commit, state}: { commit: Function, state: State }, entityTypeId: string) {
    var selectedEntityTypeIds = state.selectedEntityTypeIds.slice()
    selectedEntityTypeIds.splice(selectedEntityTypeIds.indexOf(entityTypeId), 1)
    commit(SET_SELECTED_ENTITY_TYPES, selectedEntityTypeIds)
  },
  [SELECT_PACKAGE] ({commit, state}: { commit: Function, state: State }, packageId: string) {
    commit(SET_SELECTED_PACKAGES, state.selectedPackageIds.concat([packageId]))
  },
  [DESELECT_PACKAGE] ({commit, state}: { commit: Function, state: State }, packageId: string) {
    var selectedPackageIds = state.selectedPackageIds.slice()
    selectedPackageIds.splice(selectedPackageIds.indexOf(packageId), 1)
    commit(SET_SELECTED_PACKAGES, selectedPackageIds)
  },
  [DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function }) {
    api.delete_('/plugin/navigator/delete', {
      body: JSON.stringify({
        packageIds: state.selectedPackageIds,
        entityTypeIds: state.selectedEntityTypeIds
      })
    }).then(() => {
      dispatch(GET_STATE_FOR_PACKAGE, state.route.params.package)
    }, error => {
      commit(SET_ERROR, error)
    })
  }
}
