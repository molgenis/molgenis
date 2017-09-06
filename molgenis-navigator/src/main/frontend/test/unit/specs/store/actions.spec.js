import api from '@molgenis/molgenis-api-client'
import td from 'testdouble'
import actions, { GET_ENTITIES_IN_PACKAGE } from 'src/store/actions'
import { RESET_PATH, SET_ENTITIES, SET_ERROR, SET_PACKAGES, SET_PATH } from 'src/store/mutations'
import testAction from '../../utils/action.utils'

describe('actions', () => {
  let testSetup = {
    'actionToTest': null,
    'actionPayload': null,
    'state': {},
    'expectedMutationsToBeCommited': [],
    'expectedActionsToBeDispatched': []
  }

  afterEach(() => {
    td.reset()
    testSetup = {
      'actionToTest': null,
      'actionPayload': null,
      'state': {},
      'expectedMutationsToBeCommited': [],
      'expectedActionsToBeDispatched': []
    }
  })

  describe('QUERY_PACKAGES', function () {
    it('should fetch the packages and call the SET_PACKAGES mutation', done => {
      const package1 = {id: 'pack1', label: 'packLabel1'}
      const response = {
        items: [package1]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenResolve(response)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__QUERY_PACKAGES__
      testSetup.expectedMutationsToBeCommited.push({type: SET_PACKAGES, payload: response.items})

      testAction(testSetup, done)
    })

    it('should fetch packages with a query and call the SET_PACKAGES mutation', done => {
      const package1 = {id: 'pack1', label: 'packLabel1'}
      const response = {
        items: [package1]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000&q=id=q="test",description=q="test",label=q="test"')).thenResolve(response)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__QUERY_PACKAGES__
      testSetup.actionPayload = 'test'
      testSetup.expectedMutationsToBeCommited.push({type: SET_PACKAGES, payload: response.items})

      testAction(testSetup, done)
    })

    it('should fail creating the URI and call the SET_ERROR mutation', done => {
      const error = 'Double quotes not are allowed in queries, please use single quotes.'

      const get = td.function('api.get')
      td.when(get('/undefined')).thenResolve('ignore')
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__QUERY_PACKAGES__
      testSetup.actionPayload = '"wrong query"'
      testSetup.expectedMutationsToBeCommited.push({type: SET_ERROR, payload: error})

      testAction(testSetup, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenReject(error)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__QUERY_PACKAGES__
      testSetup.expectedMutationsToBeCommited.push({type: SET_ERROR, payload: error})

      testAction(testSetup, done)
    })
  })

  describe('QUERY_ENTITIES', () => {
    it('should fetch entities and call the SET_ENTITIES mutation', done => {
      const response = {
        items: [
          {
            'id': '1',
            'type': 'entity',
            'label': 'test',
            'description': 'test'
          }
        ]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&q=(label=q="test",description=q="test");isAbstract==false')).thenResolve(response)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__QUERY_ENTITIES__
      testSetup.actionPayload = 'test'
      testSetup.expectedMutationsToBeCommited.push({type: SET_ENTITIES, payload: response.items})

      testAction(testSetup, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&q=(label=q="test",description=q="test");isAbstract==false')).thenReject(error)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__QUERY_ENTITIES__
      testSetup.actionPayload = 'test'
      testSetup.expectedMutationsToBeCommited.push({type: SET_ERROR, payload: error})

      testAction(testSetup, done)
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
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package.id==1')).thenResolve(response)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__GET_ENTITIES_IN_PACKAGE__
      testSetup.actionPayload = '1'
      testSetup.expectedMutationsToBeCommited.push({type: SET_ENTITIES, payload: response.items})

      testAction(testSetup, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package.id==1')).thenReject(error)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__GET_ENTITIES_IN_PACKAGE__
      testSetup.actionPayload = '1'
      testSetup.expectedMutationsToBeCommited.push({type: SET_ERROR, payload: error})

      testAction(testSetup, done)
    })
  })

  describe('RESET_STATE', () => {
    it('should fetch all packages and call the SET_PACKAGES, RESET_PATH, and SET_ENTITIES mutations', done => {
      const response = {
        items: [
          {
            id: '1',
            name: 'package1'
          },
          {
            id: '2',
            name: 'package2'
          },
          {
            id: '3',
            name: 'package3',
            parent: '1'
          }
        ]
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenResolve(response)
      td.replace(api, 'get', get)

      const payload = [
        {
          id: '1',
          name: 'package1'
        },
        {
          id: '2',
          name: 'package2'
        }
      ]

      testSetup.actionToTest = actions.__RESET_STATE__
      testSetup.expectedMutationsToBeCommited.push({type: SET_PACKAGES, payload: payload})
      testSetup.expectedMutationsToBeCommited.push({type: RESET_PATH, payload: null})
      testSetup.expectedMutationsToBeCommited.push({type: SET_ENTITIES, payload: []})

      testAction(testSetup, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenReject(error)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__RESET_STATE__
      testSetup.expectedMutationsToBeCommited.push({type: SET_ERROR, payload: error})

      testAction(testSetup, done)
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

      testSetup.actionToTest = actions.__GET_STATE_FOR_PACKAGE__
      testSetup.expectedMutationsToBeCommited.push({type: SET_PACKAGES, payload: response.items})
      testSetup.expectedMutationsToBeCommited.push({type: RESET_PATH, payload: null})
      testSetup.expectedMutationsToBeCommited.push({type: SET_ENTITIES, payload: []})

      testAction(testSetup, done)
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

      testSetup.actionToTest = actions.__GET_STATE_FOR_PACKAGE__
      testSetup.actionPayload = '3'
      testSetup.expectedMutationsToBeCommited.push({type: SET_ERROR, payload: 'couldn\'t find package.'})
      testSetup.expectedMutationsToBeCommited.push({type: SET_PACKAGES, payload: response.items})
      testSetup.expectedMutationsToBeCommited.push({type: RESET_PATH, payload: null})
      testSetup.expectedMutationsToBeCommited.push({type: SET_ENTITIES, payload: []})

      testAction(testSetup, done)
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

      testSetup.actionToTest = actions.__GET_STATE_FOR_PACKAGE__
      testSetup.actionPayload = '2'
      testSetup.expectedMutationsToBeCommited.push({type: SET_PACKAGES, payload: payload})
      testSetup.expectedMutationsToBeCommited.push({type: SET_PATH, payload: path})
      testSetup.expectedActionsToBeDispatched.push({type: GET_ENTITIES_IN_PACKAGE, payload: '2'})

      testAction(testSetup, done)
    })

    it('should fail the get and call the SET_ERROR mutation', done => {
      const error = 'failed to get'

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Package?sort=label&num=1000')).thenReject(error)
      td.replace(api, 'get', get)

      testSetup.actionToTest = actions.__GET_STATE_FOR_PACKAGE__
      testSetup.actionPayload = '1'
      testSetup.expectedMutationsToBeCommited.push({type: SET_ERROR, payload: error})

      testAction(testSetup, done)
    })
  })
})
