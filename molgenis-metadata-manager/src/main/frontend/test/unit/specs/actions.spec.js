/* eslint-disable no-undef */
import utils from '@molgenis/molgenis-vue-test-utils'
import td from 'testdouble'
import api from '@molgenis/molgenis-api-client'

import {
  CREATE_ALERT,
  SET_ATTRIBUTE_TYPES,
  SET_EDITOR_ENTITY_TYPE,
  SET_ENTITY_TYPES,
  SET_LOADING,
  SET_PACKAGES,
  SET_SELECTED_ATTRIBUTE_ID,
  SET_SELECTED_ENTITY_TYPE_ID,
  UPDATE_EDITOR_ENTITY_TYPE
} from 'store/mutations'

import actions, { toAttribute, toEntityType } from 'store/actions'

const i18n = {
  'save-succes-message': 'Successfully updated metadata for EntityType'
}

const t = (key) => {
  return i18n[key]
}

describe('actions', () => {
  afterEach(() => td.reset())
  const rejection = 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'

  const alertPayload = {
    type: 'error',
    message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
  }

  describe('GET_PACKAGES', () => {
    const response = [
      {id: 'base', label: 'Default'},
      {id: 'root', label: 'root'},
      {id: 'root_hospital', label: 'root_hospital'}
    ]

    it('should retrieve all Packages and store them in the state via a mutation', done => {
      const get = td.function('api.get')

      td.when(get('/plugin/metadata-manager/editorPackages')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: SET_PACKAGES, payload: response}
        ]
      }

      utils.testAction(actions.__GET_PACKAGES__, options, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/metadata-manager/editorPackages')).thenReject(rejection)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: alertPayload}
        ]
      }

      utils.testAction(actions.__GET_PACKAGES__, options, done)
    })
  })

  describe('GET_ENTITY_TYPES', () => {
    const response = {
      items: [
        {id: '1', name: 'entityType1'},
        {id: '2', name: 'entityType2'},
        {id: '3', name: 'entityType3'}
      ]
    }

    it('should retrieve all EntityTypes and store them in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?num=10000')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        state: {route: {params: {}}},
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: SET_ENTITY_TYPES, payload: response.items}
        ]
      }

      utils.testAction(actions.__GET_ENTITY_TYPES__, options, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_EntityType?num=10000')).thenReject(rejection)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: alertPayload}
        ]
      }

      utils.testAction(actions.__GET_ENTITY_TYPES__, options, done)
    })
  })

  describe('GET_ATTRIBUTE_TYPES', () => {
    it('should retrieve all attribute types and store them in the state via a mutation', done => {
      const response = {
        enumOptions: ['string', 'int', 'xref']
      }

      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Attribute/meta/type')).thenResolve(response)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: SET_ATTRIBUTE_TYPES, payload: ['string', 'int', 'xref']}
        ]
      }

      utils.testAction(actions.__GET_ATTRIBUTE_TYPES__, options, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/api/v2/sys_md_Attribute/meta/type')).thenReject(rejection)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: alertPayload}
        ]
      }

      utils.testAction(actions.__GET_ATTRIBUTE_TYPES__, options, done)
    })
  })

  describe('GET_EDITOR_ENTITY_TYPE', () => {
    const entityTypeId = '1'

    it('should retrieve an EditorEntityType based on EntityType ID and store it in the state via a mutation', done => {
      const response = {
        entityType: {
          id: '1',
          attributes: []
        }
      }

      const get = td.function('api.get')
      td.when(get('/plugin/metadata-manager/entityType/' + entityTypeId)).thenResolve(response)
      td.replace(api, 'get', get)

      const payload = toEntityType(response.entityType)

      const options = {
        payload: entityTypeId,
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: SET_EDITOR_ENTITY_TYPE, payload: payload}
        ]
      }

      utils.testAction(actions.__GET_EDITOR_ENTITY_TYPE__, options, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/metadata-manager/entityType/' + entityTypeId)).thenReject(rejection)
      td.replace(api, 'get', get)

      const options = {
        payload: entityTypeId,
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: alertPayload}
        ]
      }

      utils.testAction(actions.__GET_EDITOR_ENTITY_TYPE__, options, done)
    })
  })

  describe('CREATE_ENTITY_TYPE', () => {
    it('should create an EditorEntityType and use mutations to store it ' +
      'in the state, set it to the selected entity type and add it to the list of entity types', done => {
      const response = {
        entityType: {
          id: '1',
          attributes: []
        }
      }

      const get = td.function('api.get')
      td.when(get('/plugin/metadata-manager/create/entityType')).thenResolve(response)
      td.replace(api, 'get', get)

      const payload = {...toEntityType(response.entityType), isNew: true}

      const options = {
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: SET_EDITOR_ENTITY_TYPE, payload: payload}
        ]
      }

      utils.testAction(actions.__CREATE_ENTITY_TYPE__, options, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/metadata-manager/create/entityType')).thenReject(rejection)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: alertPayload}
        ]
      }

      utils.testAction(actions.__CREATE_ENTITY_TYPE__, options, done)
    })
  })

  describe('DELETE_ENTITY_TYPE', () => {
    const state = {
      entityTypes: [
        {id: '1'},
        {id: '2'}
      ]
    }

    it('should successfully delete an entity type', done => {
      const response = {
        statusText: 'OK!'
      }

      const delete_ = td.function('api.delete_')
      td.when(delete_('/api/v1/1/meta')).thenResolve(response)
      td.replace(api, 'delete_', delete_)

      const payload = {type: 'info', message: 'Delete was successful: OK!'}

      const options = {
        payload: '1',
        state: state,
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: SET_ENTITY_TYPES, payload: [{id: '2'}]},
          {type: SET_SELECTED_ENTITY_TYPE_ID, payload: null},
          {type: SET_SELECTED_ATTRIBUTE_ID, payload: null},
          {type: SET_EDITOR_ENTITY_TYPE, payload: null},
          {type: CREATE_ALERT, payload: payload}
        ]
      }

      utils.testAction(actions.__DELETE_ENTITY_TYPE__, options, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const delete_ = td.function('api.delete_')
      td.when(delete_('/api/v1/1/meta')).thenReject(rejection)
      td.replace(api, 'delete_', delete_)

      const options = {
        payload: '1',
        state: state,
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: alertPayload}
        ]
      }

      utils.testAction(actions.__DELETE_ENTITY_TYPE__, options, done)
    })
  })

  describe('CREATE_ATTRIBUTE', () => {
    it('should create a new attribute and add it to the editorEntityType attributes list', done => {
      const response = {
        attribute: {
          id: '3'
        }
      }

      const state = {
        editorEntityType: {
          attributes: [
            {id: '1'},
            {id: '2'}
          ]
        }
      }

      const get = td.function('api.get')
      td.when(get('/plugin/metadata-manager/create/attribute')).thenResolve(response)
      td.replace(api, 'get', get)

      const attribute = {...toAttribute(response.attribute), isNew: true}

      const options = {
        state: state,
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {
            type: UPDATE_EDITOR_ENTITY_TYPE,
            payload: {key: 'attributes', value: [...state.editorEntityType.attributes, attribute]}
          },
          {type: SET_SELECTED_ATTRIBUTE_ID, payload: attribute.id}
        ]
      }

      utils.testAction(actions.__CREATE_ATTRIBUTE__, options, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get('/plugin/metadata-manager/create/attribute')).thenReject(rejection)
      td.replace(api, 'get', get)

      const options = {
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: alertPayload}
        ]
      }

      utils.testAction(actions.__CREATE_ATTRIBUTE__, options, done)
    })
  })

  describe('SAVE_EDITOR_ENTITY_TYPE', () => {
    it('should persist metadata changes to the database', done => {
      const editorEntityType = toEntityType({id: '1', label: 'test', attributes: []})
      const state = {
        editorEntityType: editorEntityType
      }

      const response = {
        statusText: 'OK'
      }

      const post = td.function('api.post')
      td.when(post('/plugin/metadata-manager/entityType', {body: JSON.stringify(editorEntityType)})).thenResolve(response)
      td.replace(api, 'post', post)

      const payload = {
        type: 'success',
        message: 'OK: Successfully updated metadata for EntityType: test'
      }

      const options = {
        state: state,
        payload: t,
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: payload}
        ]
      }

      utils.testAction(actions.__SAVE_EDITOR_ENTITY_TYPE__, options, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const editorEntityType = toEntityType({id: '1', label: 'test', attributes: []})
      const state = {
        editorEntityType: editorEntityType
      }

      const post = td.function('api.post')
      td.when(post('/plugin/metadata-manager/entityType', {body: JSON.stringify(editorEntityType)})).thenReject(rejection)
      td.replace(api, 'post', post)

      const options = {
        state: state,
        payload: t,
        expectedMutations: [
          {type: SET_LOADING, payload: true},
          {type: CREATE_ALERT, payload: alertPayload}
        ]
      }

      utils.testAction(actions.__SAVE_EDITOR_ENTITY_TYPE__, options, done)
    })
  })
})
