// @flow
import type { Package, Entity } from '../flow.types'
import { INITIAL_STATE } from './state'
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { RESET_PATH, SET_ENTITIES, SET_ERROR, SET_PACKAGES, SET_PATH } from './mutations'

export const QUERY_PACKAGES = 'QUERY_PACKAGES'
export const QUERY_ENTITIES = 'QUERY_ENTITIES'
export const RESET_STATE = 'RESET_STATE'
export const GET_STATE_FOR_PACKAGE = 'GET_STATE_FOR_PACKAGE'
export const GET_ENTITIES_IN_PACKAGE = 'GET_ENTITIES_IN_PACKAGE'

const SYS_PACKAGE_ID = 'sys'

/**
 * Resets the entire state using the given packages as the package state.
 * Only top level packages are set
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
 * Get all ( first 1000 ) packages from the server
 * If the user is not a super user 'system packages' will be removed from the result set
 * @returns {Promise}, on success resolves to list of 'all' packages, on failure resolves to error message
 */
function getAllPackages () {
  return new Promise((resolve, reject) => {
    const uri = '/api/v2/sys_md_Package?sort=label&num=1000'
    api.get(uri).then((response) => {
      resolve(filterNonVisiblePackages(response.items))
    }).catch((error) => {
      reject(error)
    })
  })
}

/**
 * Query's the server of ( first 1000 ) packages.
 * If the user is not a super user 'system entities' will be removed from the result set
 * Packages are returned if query matches id, description or label
 * @returns {Promise}, on success resolves to list of matching packages, on failure resolves to error message
 */
function queryPackages (query: string) {
  return new Promise((resolve, reject) => {
    const uri = '/api/v2/sys_md_Package?sort=label&num=1000&q=id=q="' + encodeURIComponent(query) + '",description=q="' + encodeURIComponent(query) + '",label=q="' + encodeURIComponent(query) + '"'
    api.get(uri).then((response) => {
      resolve(filterNonVisiblePackages(response.items))
    }).catch((error) => {
      reject(error)
    })
  })
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
    return new Promise((resolve, reject) => {
      if (!query) {
        getAllPackages().then(packages => {
          commit(SET_PACKAGES, packages)
          resolve()
        }, errorMessage => {
          commit(SET_ERROR, errorMessage)
          reject()
        })
      } else {
        try {
          validateQuery(query)
          queryPackages(query).then(packages => {
            commit(SET_PACKAGES, packages)
            resolve()
          }, errorMessage => {
            commit(SET_ERROR, errorMessage)
            reject()
          })
        } catch (err) {
          commit(SET_ERROR, err.message)
          reject()
        }
      }
    })
  },
  [QUERY_ENTITIES] ({commit}: { commit: Function }, query: string) {
    return new Promise((resolve, reject) => {
      if (!query) {
        resolve()
      } else {
        try {
          validateQuery(query)
          api.get('/api/v2/sys_md_EntityType?sort=label&num=1000&q=(label=q="' + encodeURIComponent(query) + '",description=q="' + encodeURIComponent(query) + '");isAbstract==false').then((response) => {
            const entities = response.items.map(toEntity)
            commit(SET_ENTITIES, filterNonVisibleEntities(entities))
            resolve()
          }).catch((error) => {
            commit(SET_ERROR, error)
            reject()
          })
        } catch (err) {
          commit(SET_ERROR, err.message)
          reject()
        }
      }
    })
  },
  [GET_ENTITIES_IN_PACKAGE] ({commit}: { commit: Function }, packageId: string) {
    return new Promise((resolve, reject) => {
      api.get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package.id==' + packageId).then((response) => {
        const entities = response.items.map(toEntity)
        commit(SET_ENTITIES, entities)
        resolve()
      }).catch((error) => {
        commit(SET_ERROR, error)
        reject()
      })
    })
  },
  [RESET_STATE] ({commit}: { commit: Function }) {
    return new Promise((resolve, reject) => {
      getAllPackages().then(allPackages => {
        resetToHome(commit, allPackages)
        resolve()
      }, errorMessage => {
        commit(SET_ERROR, errorMessage)
        reject()
      })
    })
  },
  [GET_STATE_FOR_PACKAGE] ({commit, dispatch}: {
    commit: Function, dispatch: Function
  }, selectedPackageId: ? string) {
    return new Promise((resolve, reject) => {
      getAllPackages().then(allPackages => {
        if (!selectedPackageId) {
          resetToHome(commit, allPackages)
        } else {
          const selectedPackage = allPackages.find(function (packageItem) {
            return packageItem.id === selectedPackageId
          })

          if (!selectedPackage) {
            commit(SET_ERROR, 'couldn\'t find package.')
            resetToHome(commit, allPackages)
            reject()
          } else {
            // Find child packages.
            const childPackages = allPackages.filter(function (packageItem) {
              return packageItem.parent && packageItem.parent.id === selectedPackage.id
            })
            commit(SET_PACKAGES, childPackages)

            const path = buildPath(allPackages, selectedPackage, [])
            commit(SET_PATH, path)
            dispatch(GET_ENTITIES_IN_PACKAGE, selectedPackageId).then(resolve)
          }
        }
      }, errorMessage => {
        commit(SET_ERROR, errorMessage)
        reject()
      })
    })
  }
}
