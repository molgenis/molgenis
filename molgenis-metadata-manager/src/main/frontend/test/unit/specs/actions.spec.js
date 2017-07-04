/* eslint-disable no-undef */
import testAction from '../utils/action.utils'
import td from 'testdouble'

import * as api from '@molgenis/molgenis-api-client'
import {
  CREATE_ALERT,
  SET_ATTRIBUTE_TYPES,
  SET_EDITOR_ENTITY_TYPE,
  SET_ENTITY_TYPES,
  SET_PACKAGES,
  SET_SELECTED_ATTRIBUTE_ID,
  SET_SELECTED_ENTITY_TYPE_ID,
  UPDATE_EDITOR_ENTITY_TYPE
} from 'store/mutations'
import actions, { toAttribute, toEntityType } from 'store/actions'

describe('actions', () => {
  const rejection = {
    errors: [{
      type: 'error',
      message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
    }]
  }

  const alertPayload = {
    type: 'error',
    message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
  }

  describe('GET_PACKAGES', () => {
    afterEach(() => td.reset())

    const response = [
      { id: 'base', label: 'Default' },
      { id: 'root', label: 'root' },
      { id: 'root_hospital', label: 'root_hospital' }
    ]

    it('should retrieve all Packages and store them in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get({ apiUrl: '/plugin/metadata-manager-service' }, '/editorPackages')).thenResolve(response)
      td.replace(api, 'get', get)

      testAction(actions.__GET_PACKAGES__, null, {}, [{ type: SET_PACKAGES, payload: response }], [], done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get({ apiUrl: '/plugin/metadata-manager-service' }, '/editorPackages')).thenReject(rejection)
      td.replace(api, 'get', get)

      testAction(actions.__GET_PACKAGES__, null, {}, [{ type: CREATE_ALERT, payload: alertPayload }], [], done)
    })
  })

  describe('GET_ENTITY_TYPES', () => {
    afterEach(() => td.reset())

    const response = {
      items: [
        { id: '1', name: 'entityType1' },
        { id: '2', name: 'entityType2' },
        { id: '3', name: 'entityType3' }
      ]
    }

    it('should retrieve all EntityTypes and store them in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get({ apiUrl: '/api' }, '/v2/sys_md_EntityType?num=10000')).thenResolve(response)
      td.replace(api, 'get', get)

      testAction(actions.__GET_ENTITY_TYPES__, null, { route: { params: {} } }, [
        { type: SET_ENTITY_TYPES, payload: response.items }
      ], [], done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get({ apiUrl: '/api' }, '/v2/sys_md_EntityType?num=10000')).thenReject(rejection)
      td.replace(api, 'get', get)

      testAction(actions.__GET_ENTITY_TYPES__, null, {}, [{ type: CREATE_ALERT, payload: alertPayload }], [], done)
    })
  })

  describe('GET_ATTRIBUTE_TYPES', () => {
    afterEach(() => td.reset())

    it('should retrieve all attribute types and store them in the state via a mutation', done => {
      const response = {
        enumOptions: ['string', 'int', 'xref']
      }

      const get = td.function('api.get')
      td.when(get({ apiUrl: '/api' }, '/v2/sys_md_Attribute/meta/type')).thenResolve(response)
      td.replace(api, 'get', get)

      const payload = ['STRING', 'INT', 'XREF']

      testAction(actions.__GET_ATTRIBUTE_TYPES__, null, {}, [{ type: SET_ATTRIBUTE_TYPES, payload: payload }], [], done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get({ apiUrl: '/api' }, '/v2/sys_md_Attribute/meta/type')).thenReject(rejection)
      td.replace(api, 'get', get)

      testAction(actions.__GET_ATTRIBUTE_TYPES__, null, {}, [{ type: CREATE_ALERT, payload: alertPayload }], [], done)
    })
  })

  describe('GET_EDITOR_ENTITY_TYPE', () => {
    afterEach(() => td.reset())

    const entityTypeId = '1'

    it('should retrieve an EditorEntityType based on EntityType ID and store it in the state via a mutation', done => {
      const response = {
        entityType: {
          id: '1',
          attributes: []
        }
      }

      const get = td.function('api.get')
      td.when(get({ apiUrl: '/plugin/metadata-manager-service' }, '/entityType/' + entityTypeId)).thenResolve(response)
      td.replace(api, 'get', get)

      const payload = toEntityType(response.entityType)

      testAction(actions.__GET_EDITOR_ENTITY_TYPE__, entityTypeId, {}, [{
        type: SET_EDITOR_ENTITY_TYPE,
        payload: payload
      }], [], done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get({ apiUrl: '/plugin/metadata-manager-service' }, '/entityType/' + entityTypeId)).thenReject(rejection)
      td.replace(api, 'get', get)

      testAction(actions.__GET_EDITOR_ENTITY_TYPE__, entityTypeId, {}, [{
        type: CREATE_ALERT,
        payload: alertPayload
      }], [], done)
    })
  })

  describe('CREATE_ENTITY_TYPE', () => {
    afterEach(() => td.reset())

    it('should create an EditorEntityType and use mutations to store it ' +
      'in the state, set it to the selected entity type and add it to the list of entity types', done => {
      const response = {
        entityType: {
          id: '1',
          attributes: []
        }
      }

      const get = td.function('api.get')
      td.when(get({ apiUrl: '/plugin/metadata-manager-service' }, '/create/entityType')).thenResolve(response)
      td.replace(api, 'get', get)

      const payload = {...toEntityType(response.entityType), isNew: true}

      testAction(actions.__CREATE_ENTITY_TYPE__, null, {}, [
        { type: SET_EDITOR_ENTITY_TYPE, payload: payload }
      ], [], done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get({ apiUrl: '/plugin/metadata-manager-service' }, '/create/entityType')).thenReject(rejection)
      td.replace(api, 'get', get)

      testAction(actions.__CREATE_ENTITY_TYPE__, null, {}, [{ type: CREATE_ALERT, payload: alertPayload }], [], done)
    })
  })

  describe('DELETE_ENTITY_TYPE', () => {
    const state = {
      entityTypes: [
        { id: '1' },
        { id: '2' }
      ]
    }

    it('should successfully delete an entity type', done => {
      const callApi = td.function('api.callApi')
      td.when(callApi({ apiUrl: '/api' }, '/v1/1/meta', 'delete')).thenReject('rejection')
      td.replace(api, 'callApi', callApi)

      testAction(actions.__DELETE_ENTITY_TYPE__, '1', state, [
        { type: SET_ENTITY_TYPES, payload: [{ id: '2' }] },
        { type: SET_SELECTED_ENTITY_TYPE_ID, payload: null },
        { type: SET_SELECTED_ATTRIBUTE_ID, payload: null }
      ], [], done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const callApi = td.function('api.callApi')
      td.when(callApi({ apiUrl: '/api' }, '/v1/1/meta', 'delete')).thenReject(rejection)
      td.replace(api, 'callApi', callApi)

      testAction(actions.__DELETE_ENTITY_TYPE__, '1', state, [{ type: CREATE_ALERT, payload: alertPayload }], [], done)
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
            { id: '1' },
            { id: '2' }
          ]
        }
      }

      const get = td.function('api.get')
      td.when(get({ apiUrl: '/plugin/metadata-manager-service' }, '/create/attribute')).thenResolve(response)
      td.replace(api, 'get', get)

      const attribute = {...toAttribute(response.attribute), isNew: true}

      testAction(actions.__CREATE_ATTRIBUTE__, null, state, [
        {
          type: UPDATE_EDITOR_ENTITY_TYPE,
          payload: { key: 'attributes', value: [...state.editorEntityType.attributes, attribute] }
        },
        { type: SET_SELECTED_ATTRIBUTE_ID, payload: attribute.id }
      ], [], done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const get = td.function('api.get')
      td.when(get({ apiUrl: '/plugin/metadata-manager-service' }, '/create/attribute')).thenReject(rejection)
      td.replace(api, 'get', get)

      testAction(actions.__CREATE_ATTRIBUTE__, null, {}, [{
        type: CREATE_ALERT,
        payload: alertPayload
      }], [], done)
    })
  })

  describe('SAVE_EDITOR_ENTITY_TYPE', () => {
    afterEach(() => td.reset())

    it('should persist metadata changes to the database', done => {
      const editorEntityType = toEntityType({ id: '1', label: 'test', attributes: [] })
      const state = {
        editorEntityType: editorEntityType
      }

      const post = td.function('api.post')
      td.when(post({ apiUrl: '/plugin/metadata-manager-service' }, '/entityType', editorEntityType)).thenResolve({})
      td.replace(api, 'post', post)

      const payload = {
        type: 'success',
        message: 'Successfully updated metadata for EntityType: test'
      }

      testAction(actions.__SAVE_EDITOR_ENTITY_TYPE__, null, state, [
        { type: CREATE_ALERT, payload: payload }
      ], null, done)
    })

    it('should fail and create an alert in the state via a mutation', done => {
      const editorEntityType = toEntityType({ id: '1', label: 'test', attributes: [] })
      const state = {
        editorEntityType: editorEntityType
      }

      const post = td.function('api.post')
      td.when(post({ apiUrl: '/plugin/metadata-manager-service' }, '/entityType', editorEntityType)).thenReject(rejection)
      td.replace(api, 'post', post)

      testAction(actions.__SAVE_EDITOR_ENTITY_TYPE__, null, state, [{
        type: CREATE_ALERT,
        payload: alertPayload
      }], [], done)
    })
  })
})
