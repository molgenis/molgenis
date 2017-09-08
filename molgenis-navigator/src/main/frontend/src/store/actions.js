// @flow
import type { Package, Entity } from '../flow.types'
import { INITIAL_STATE } from './state'
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { RESET_PATH, SET_ENTITIES, SET_ERROR, SET_PACKAGES, SET_PATH } from './mutations'

export const QUERY_PACKAGES = '__QUERY_PACKAGES__'
export const QUERY_ENTITIES = '__QUERY_ENTITIES__'
export const RESET_STATE = '__RESET_STATE__'
export const GET_STATE_FOR_PACKAGE = '__GET_STATE_FOR_PACKAGE__'
export const GET_ENTITIES_IN_PACKAGE = '__GET_ENTITIES_IN_PACKAGE__'

const SYS_PACKAGE_ID = 'sys'

/**
 * Resets the entire state using the given packages as the package state.
 * Only top level packages are set
 *
 * @param commit, reference to mutation function
 * @param packages, the complete list of packages
 */
function resetToHome (commit: Function, packages: Array<Package>) {
  const homePackages = packages.filter(function (packageItem) {
    return !packageItem.hasOwnProperty('parent')
  })
  commit(SET_PACKAGES, homePackages)
  commit(RESET_PATH)
  commit(SET_ENTITIES, [])
}

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
  return '/api/v2/sys_md_Package?sort=label&num=1000&q=id=q="' + encodeURIComponent(query) + '",description=q="' + encodeURIComponent(query) + '",label=q="' + encodeURIComponent(query) + '"'
}

/**
 * Get a MOLGENIS rest api encoded query for the EntityType table
 * The query retrieves the first 1000 EntityTypes
 *
 * @param query
 */
function getEntityTypeQuery (query: string) {
  return '/api/v2/sys_md_EntityType?sort=label&num=1000&q=(label=q="' + encodeURIComponent(query) + '",description=q="' + encodeURIComponent(query) + '");isAbstract==false'
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
      uri = '/api/v2/sys_md_Package?sort=label&num=1000'
    } else {
      try {
        validateQuery(query)
        uri = getPackageQuery(query)
      } catch (error) {
        commit(SET_ERROR, error.message)
      }
    }

    api.get(uri).then(response => {
      commit(SET_PACKAGES, response.items)
    }, error => {
      commit(SET_ERROR, error)
    })
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
    api.get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package.id==' + packageId).then(response => {
      const entities = response.items.map(toEntity)
      commit(SET_ENTITIES, entities)
    }, error => {
      commit(SET_ERROR, error)
    })
  },
  [RESET_STATE] ({commit}: { commit: Function }) {
    api.get('/api/v2/sys_md_Package?sort=label&num=1000').then(response => {
      resetToHome(commit, response.items)
    }, error => {
      commit(SET_ERROR, error)
    })
  },
  [GET_STATE_FOR_PACKAGE] ({commit, dispatch}: { commit: Function, dispatch: Function }, selectedPackageId: ?string) {
    api.get('/api/v2/sys_md_Package?sort=label&num=1000').then(response => {
      const packages = response.items

      if (!selectedPackageId) {
        resetToHome(commit, packages)
      } else {
        const selectedPackage = packages.find(function (packageItem) {
          return packageItem.id === selectedPackageId
        })

        if (!selectedPackage) {
          commit(SET_ERROR, 'couldn\'t find package.')
          resetToHome(commit, packages)
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
  }
}
