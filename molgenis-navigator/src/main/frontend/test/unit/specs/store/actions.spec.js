import api from '@molgenis/molgenis-api-client'
import td from 'testdouble'
import actions, { GET_ENTITIES_IN_PACKAGE, GET_STATE_FOR_PACKAGE, QUERY_ENTITIES, RESET_STATE } from 'src/store/actions'
import { RESET_PATH, SET_ENTITIES, SET_ERROR, SET_PACKAGES, SET_PATH, SET_QUERY } from 'src/store/mutations'
import utils from '@molgenis/molgenis-vue-test-utils'
import { SET_SELECTED_ENTITY_TYPES, SET_SELECTED_PACKAGES } from '../../../../src/store/mutations'

describe('actions', () => {
  afterEach(() => { td.reset() })

  describe('QUERY_PACKAGES', function () {
    it('should fetch the packages, filter out the system packages and call the SET_PACKAGES mutation', done => {
      const package1 = {id: 'pack1', label: 'packLabel1'}
      const sysPackage = {id: 'sys', label: 'sys package'}
      const sysChildPackage = {id: 'sys_child', label: 'sys child package'}
      const package2 = {id: 'pack2', label: 'packLabel2'}
      const response = {
        items: [package1, sysPackage, sysChildPackage, package2]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000&q=id=q=test,label=q=test,description=q=test')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: 'test',
        expectedMutations: [
          {type: SET_PACKAGES, payload: [package1, package2]}
        ]
      }

      utils.testAction(actions.__QUERY_PACKAGES__, options, done)
    })

    it('should fetch packages with a query and call the SET_PACKAGES mutation', done => {
      const package1 = {id: 'pack1', label: 'packLabel1'}
      const sysPackage = {id: 'sys', label: 'sys package'}
      const sysChildPackage = {id: 'sys_child', label: 'sys child package'}
      const response = {
        items: [package1, sysPackage, sysChildPackage]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000&q=id=q=test,label=q=test,description=q=test')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: 'test',
        expectedMutations: [
          {type: SET_PACKAGES, payload: [package1]}
        ]
      }

      utils.testAction(actions.__QUERY_PACKAGES__, options, done)
    })

    it('should fail creating the URI and call the SET_ERROR mutation', done => {
      const error = 'Double quotes not are allowed in queries, please use single quotes.'
      const options = {
        payload: '"wrong query"',
        expectedMutations: [
          {type: SET_ERROR, payload: error}
        ]
      }

      utils.testAction(actions.__QUERY_PACKAGES__, options, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'
      const query = 'foobar'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000&q=id=q=foobar,label=q=foobar,description=q=foobar')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        payload: query,
        expectedMutations: [
          {type: SET_ERROR, payload: error}
        ]
      }

      utils.testAction(actions.__QUERY_PACKAGES__, options, done)
    })
  })

  describe('QUERY_ENTITIES', () => {
    it('should fetch entities, map the result to entity types, filter out system entities and call the SET_ENTITIES mutation', done => {
      const response = {
        items: [
          {
            'id': '1',
            'label': 'test',
            'description': 'test'
          },
          {
            'id': 'sys_entity',
            'label': 'system entity',
            'description': 'test2'
          }
        ]
      }

      const expectedEntities = [
        {
          'id': '1',
          'type': 'entity',
          'label': 'test',
          'description': 'test'
        }
      ]

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&q=(label=q=test,description=q=test);isAbstract==false')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: 'test',
        expectedMutations: [
          {type: SET_ENTITIES, payload: expectedEntities}
        ]
      }
      utils.testAction(actions.__QUERY_ENTITIES__, options, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&q=(label=q=test,description=q=test);isAbstract==false')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        payload: 'test',
        expectedMutations: [
          {type: SET_ERROR, payload: error}
        ]
      }

      utils.testAction(actions.__QUERY_ENTITIES__, options, done)
    })
  })

  describe('GET_ENTITIES_IN_PACKAGE', () => {
    it('should retrieve all entities from a specific package', done => {
      const entity1 = {
        'id': '1',
        'type': 'entity',
        'label': 'test',
        'description': 'test'
      }

      const entity2 = {
        'id': '2',
        'type': 'entity',
        'label': 'test',
        'description': 'test'
      }

      const response = {
        items: [
          entity1,
          entity2
        ]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package==1')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: '1',
        expectedMutations: [
          {type: SET_ENTITIES, payload: response.items}
        ]
      }

      utils.testAction(actions.__GET_ENTITIES_IN_PACKAGE__, options, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package==1')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        payload: '1',
        expectedMutations: [
          {type: SET_ERROR, payload: error}
        ]
      }

      utils.testAction(actions.__GET_ENTITIES_IN_PACKAGE__, options, done)
    })
  })

  describe('RESET_STATE', () => {
    it('should fetch all packages and entities and call RESET_PATH', done => {
      const packageResponse = {
        items: [
          {
            id: '1',
            name: 'package1'
          },
          {
            id: '2',
            name: 'package2'
          }
        ]
      }

      const entityResponse = {
        items: [
          {
            id: '1',
            label: 'entity1'
          }
        ]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000&&q=parent==%22%22')).thenResolve(packageResponse)
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package==%22%22')).thenResolve(entityResponse)
      td.replace(api, 'get', get)

      const packages = [
        {
          id: '1',
          name: 'package1'
        },
        {
          id: '2',
          name: 'package2'
        }
      ]

      const options = {
        expectedMutations: [
          {type: RESET_PATH},
          {type: SET_PACKAGES, payload: packages}
        ]
      }

      utils.testAction(actions.__RESET_STATE__, options, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const entityResponse = {
        items: []
      }

      const entities = []

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000&&q=parent==%22%22')).thenReject(error)
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package==%22%22')).thenResolve(entityResponse)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: RESET_PATH},
          {type: SET_ERROR, payload: error},
          {type: SET_ENTITIES, payload: entities}
        ]
      }

      utils.testAction(actions.__RESET_STATE__, options, done)
    })
  })

  describe('GET_STATE_FOR_PACKAGE', () => {
    it('should reset state if no package is selected', done => {
      const response = {
        items: [
          {
            id: '1',
            name: 'package1'
          },
          {
            id: '2',
            name: 'package2'
          }
        ]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        expectedActions: [
          {type: RESET_STATE}
        ]
      }

      utils.testAction(actions.__GET_STATE_FOR_PACKAGE__, options, done)
    })

    it('should throw an error if the selected package was not found', done => {
      const response = {
        items: [
          {
            id: '1',
            name: 'package1'
          },
          {
            id: '2',
            name: 'package2'
          }
        ]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: '3',
        expectedMutations: [
          {type: SET_ERROR, payload: 'couldn\'t find package.'}
        ],
        expectedActions: [
          {type: RESET_STATE}
        ]
      }

      utils.testAction(actions.__GET_STATE_FOR_PACKAGE__, options, done)
    })

    it('should create a path based on the selected package', done => {
      const response = {
        items: [
          {
            id: '1',
            name: 'package1'
          },
          {
            id: '2',
            name: 'package2',
            parent: {
              id: '1',
              name: 'package1'
            }
          },
          {
            id: '3',
            name: 'package3'
          },
          {
            id: '4',
            name: 'package4',
            parent: {
              id: '2',
              name: 'package2'
            }
          },
          {
            id: '5',
            name: 'package5',
            parent: {
              id: '2',
              name: 'package2'
            }
          }
        ]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenResolve(response)
      td.replace(api, 'get', get)

      const payload = [
        {
          id: '4',
          name: 'package4',
          parent: {
            id: '2',
            name: 'package2'
          }
        },
        {
          id: '5',
          name: 'package5',
          parent: {
            id: '2',
            name: 'package2'
          }
        }
      ]

      const path = [
        {
          id: '1',
          name: 'package1'
        },
        {
          id: '2',
          name: 'package2',
          parent: {
            id: '1',
            name: 'package1'
          }
        }
      ]

      const options = {
        payload: '2',
        expectedMutations: [
          {type: SET_PACKAGES, payload: payload},
          {type: SET_PATH, payload: path}
        ],
        expectedActions: [
          {type: GET_ENTITIES_IN_PACKAGE, payload: '2'}
        ]
      }

      utils.testAction(actions.__GET_STATE_FOR_PACKAGE__, options, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        payload: '1',
        expectedMutations: [
          {type: SET_ERROR, payload: error}
        ]
      }

      utils.testAction(actions.__GET_STATE_FOR_PACKAGE__, options, done)
    })
  })

  describe('GET_ENTITY_PACKAGES', () => {
    it('should find the package id given a entityId', done => {
      const entityId = 'my-entity-id'
      const packageId = 'my-package-id'
      const entity = {
        'id': entityId,
        'label': 'my entity in a package',
        'package': {
          id: packageId
        }
      }

      const response = {
        items: [entity]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?num=1000&&q=isAbstract==false;id==' + entityId)).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: entityId,
        expectedActions: [
          {type: GET_STATE_FOR_PACKAGE, payload: packageId}
        ]
      }

      utils.testAction(actions.__GET_ENTITY_PACKAGES__, options, done)
    })

    it('should reset the state if no package could be found', done => {
      const entityId = 'my-entity-id'
      const response = {items: []}
      const get = td.function('api.get')

      td.when(get('/api/v2/sys_md_EntityType?num=1000&&q=isAbstract==false;id==' + entityId)).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: entityId,
        expectedActions: [
          {type: RESET_STATE}
        ]
      }

      utils.testAction(actions.__GET_ENTITY_PACKAGES__, options, done)
    })

    it('should fallback to searching if the lookup entity is not in a package', done => {
      const entityId = 'my-entity-id'
      const entity = {
        'id': entityId,
        'label': 'my entity in a package'
      }

      const response = {
        items: [entity]
      }

      const get = td.function('api.get')

      td.when(get('/api/v2/sys_md_EntityType?num=1000&&q=isAbstract==false;id==' + entityId)).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        payload: entityId,
        expectedMutations: [
          {type: SET_QUERY, payload: entity.label}
        ],
        expectedActions: [
          {type: QUERY_ENTITIES, payload: entity.label}
        ]
      }

      utils.testAction(actions.__GET_ENTITY_PACKAGES__, options, done)
    })

    it('should call the SET_ERROR mutation in case the rest request fails', done => {
      const entityId = 'my-entity-id'
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?num=1000&&q=isAbstract==false;id==' + entityId)).thenReject(error)
      td.replace(api, 'get', get)

      const options = {
        payload: entityId,
        expectedMutations: [
          {type: SET_ERROR, payload: error}
        ]
      }

      utils.testAction(actions.__GET_ENTITY_PACKAGES__, options, done)
    })
  })

  describe('SELECT_ALL_PACKAGES_AND_ENTITY_TYPES', () => {
    it('should select all packages and entity types', done => {
      const packageId = 'my-package-id'
      const entityTypeId = 'my-entity-type-id'

      const options = {
        state: {
          packages: [{
            id: packageId
          }],
          entities: [{
            id: entityTypeId
          }]
        },
        expectedMutations: [
          {type: SET_SELECTED_PACKAGES, payload: [packageId]},
          {type: SET_SELECTED_ENTITY_TYPES, payload: [entityTypeId]}
        ]
      }

      utils.testAction(actions.__SELECT_ALL_PACKAGES_AND_ENTITY_TYPES__, options, done)
    })
  })

  describe('SELECT_ENTITY_TYPE', () => {
    it('should select the given entity type', done => {
      const entityTypeId0 = 'entity-type-id-0'
      const entityTypeId1 = 'entity-type-id-1'

      const options = {
        state: {
          selectedEntityTypeIds: [entityTypeId0]
        },
        payload: entityTypeId1,
        expectedMutations: [
          {type: SET_SELECTED_ENTITY_TYPES, payload: [entityTypeId0, entityTypeId1]}
        ]
      }

      utils.testAction(actions.__SELECT_ENTITY_TYPE__, options, done)
    })
  })

  describe('SELECT_PACKAGE', () => {
    it('should select the given package', done => {
      const packageId0 = 'package-id-0'
      const packageId1 = 'package-id-1'

      const options = {
        state: {
          selectedPackageIds: [packageId0]
        },
        payload: packageId1,
        expectedMutations: [
          {type: SET_SELECTED_PACKAGES, payload: [packageId0, packageId1]}
        ]
      }

      utils.testAction(actions.__SELECT_PACKAGE__, options, done)
    })
  })

  describe('DESELECT_ALL_PACKAGES_AND_ENTITY_TYPES', () => {
    it('should deselect all packages and entity types', done => {
      const packageId = 'my-package-id'
      const entityTypeId = 'my-entity-type-id'

      const options = {
        state: {
          selectedPackageIds: [packageId],
          selectedEntityTypeIds: [entityTypeId]
        },
        expectedMutations: [
          {type: SET_SELECTED_PACKAGES, payload: []},
          {type: SET_SELECTED_ENTITY_TYPES, payload: []}
        ]
      }

      utils.testAction(actions.__DESELECT_ALL_PACKAGES_AND_ENTITY_TYPES__, options, done)
    })
  })

  describe('DESELECT_ENTITY_TYPE', () => {
    it('should deselect the given entity type', done => {
      const entityTypeId0 = 'entity-type-id-0'
      const entityTypeId1 = 'entity-type-id-1'

      const options = {
        state: {
          selectedEntityTypeIds: [entityTypeId0, entityTypeId1]
        },
        payload: entityTypeId0,
        expectedMutations: [
          {type: SET_SELECTED_ENTITY_TYPES, payload: [entityTypeId1]}
        ]
      }

      utils.testAction(actions.__DESELECT_ENTITY_TYPE__, options, done)
    })
  })

  describe('DESELECT_PACKAGE', () => {
    it('should deselect the given package', done => {
      const packageId0 = 'package-id-0'
      const packageId1 = 'package-id-1'

      const options = {
        state: {
          selectedPackageIds: [packageId0, packageId1]
        },
        payload: packageId0,
        expectedMutations: [
          {type: SET_SELECTED_PACKAGES, payload: [packageId1]}
        ]
      }

      utils.testAction(actions.__DESELECT_PACKAGE__, options, done)
    })
  })

  describe('DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES', () => {
    it('should delete the selected packages and entity types', done => {
      const packageIdRoot = 'package-id-root'
      const packageId0 = 'package-id-0'
      const packageId1 = 'package-id-1'
      const entityTypeId0 = 'entity-type-id-0'
      const entityTypeId1 = 'entity-type-id-1'

      const response = {
        statusText: 'OK!'
      }
      const deleteBody = {packageIds: [packageId0, packageId1], entityTypeIds: [entityTypeId0, entityTypeId1]}
      const delete_ = td.function('api.delete_')
      td.when(delete_('/plugin/navigator/delete', {body: JSON.stringify(deleteBody)})).thenResolve(response)
      td.replace(api, 'delete_', delete_)

      const options = {
        state: {
          selectedPackageIds: [packageId0, packageId1],
          selectedEntityTypeIds: [entityTypeId0, entityTypeId1],
          route: {
            params: {
              package: packageIdRoot
            }
          }
        },
        expectedActions: [
          {type: GET_STATE_FOR_PACKAGE, payload: packageIdRoot}
        ]
      }

      utils.testAction(actions.__DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES__, options, done)
    })

    it('should delete the selected packages', done => {
      const packageIdRoot = 'package-id-root'
      const packageId0 = 'package-id-0'
      const packageId1 = 'package-id-1'

      const response = {
        statusText: 'OK!'
      }
      const deleteBody = {packageIds: [packageId0, packageId1], entityTypeIds: []}
      const delete_ = td.function('api.delete_')
      td.when(delete_('/plugin/navigator/delete', {body: JSON.stringify(deleteBody)})).thenResolve(response)
      td.replace(api, 'delete_', delete_)

      const options = {
        state: {
          selectedPackageIds: [packageId0, packageId1],
          selectedEntityTypeIds: [],
          route: {
            params: {
              package: packageIdRoot
            }
          }
        },
        expectedActions: [
          {type: GET_STATE_FOR_PACKAGE, payload: packageIdRoot}
        ]
      }

      utils.testAction(actions.__DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES__, options, done)
    })

    it('should delete the selected entity types', done => {
      const packageIdRoot = 'package-id-root'
      const entityTypeId0 = 'entity-type-id-0'
      const entityTypeId1 = 'entity-type-id-1'

      const response = {
        statusText: 'OK!'
      }
      const deleteBody = {packageIds: [], entityTypeIds: [entityTypeId0, entityTypeId1]}
      const delete_ = td.function('api.delete_')
      td.when(delete_('/plugin/navigator/delete', {body: JSON.stringify(deleteBody)})).thenResolve(response)
      td.replace(api, 'delete_', delete_)

      const options = {
        state: {
          selectedPackageIds: [],
          selectedEntityTypeIds: [entityTypeId0, entityTypeId1],
          route: {
            params: {
              package: packageIdRoot
            }
          }
        },
        expectedActions: [
          {type: GET_STATE_FOR_PACKAGE, payload: packageIdRoot}
        ]
      }

      utils.testAction(actions.__DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES__, options, done)
    })

    it('should call the SET_ERROR mutation in case the delete request fails', done => {
      const entityTypeId0 = 'entity-type-id-0'

      const error = 'failed to delete'
      const deleteBody = {packageIds: [], entityTypeIds: [entityTypeId0]}
      const delete_ = td.function('api.delete_')
      td.when(delete_('/plugin/navigator/delete', {body: JSON.stringify(deleteBody)})).thenReject(error)
      td.replace(api, 'delete_', delete_)

      const options = {
        state: {
          selectedPackageIds: [],
          selectedEntityTypeIds: [entityTypeId0]
        },
        expectedMutations: [
          {type: SET_ERROR, payload: error}
        ]
      }

      utils.testAction(actions.__DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES__, options, done)
    })
  })
})
