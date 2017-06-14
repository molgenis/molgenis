/* eslint-disable no-undef */
import testAction from '../utils/action.utils'
import td from 'testdouble'
// $FlowFixMe
import * as api from 'molgenis-api-client'

import { CREATE_ALERT, SET_EDITOR_ENTITY_TYPE, SET_ENTITY_TYPES, SET_PACKAGES } from 'store/mutations'

import actions from 'store/actions'
describe('actions', () => {
  describe('GET_PACKAGES', () => {
    afterEach(() => td.reset())
    const state = {
      alert: {
        message: null,
        type: null
      },
      packages: [],
      entityTypes: [],
      editorEntityType: {}
    }
    it('Should retrieve all Packages and filter on non-system Packages', done => {
      const mockedResponse = [{id: 'base', label: 'Default'}, {id: 'root', label: 'root'}, {
        id: 'root_hospital',
        label: 'root_hospital'
      }]
      const get = td.function('api.get')
      td.when(get({apiUrl: '/metadata-manager-service'}, '/editorPackages'))
        .thenResolve(mockedResponse)
      td.replace(api, 'get', get)
      const payload = [{id: 'base', label: 'Default'}, {id: 'root', label: 'root'}, {
        id: 'root_hospital',
        label: 'root_hospital'
      }]
      testAction(actions.__GET_PACKAGES__, null, state, [{type: SET_PACKAGES, payload: payload}], [], done)
    })
    it('Should create alert when failing', done => {
      const mockedResponse = {
        errors: [{
          type: 'danger',
          message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
        }]
      }
      const get = td.function('api.get')
      td.when(get({apiUrl: '/metadata-manager-service'}, '/editorPackages'))
        .thenReject(mockedResponse)

      td.replace(api, 'get', get)
      const payload = {
        type: 'danger',
        message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
      }
      testAction(actions.__GET_PACKAGES__, null, state, [{type: CREATE_ALERT, payload: payload}], [], done)
    })
  })
  describe('GET_ENTITY_TYPES', () => {
    afterEach(() => td.reset())
    const state = {}
    it('Should retrieve all EntityTypes and filter on non-system EntityTypes', done => {
      const mockedResponse = {
        href: '/api/v2/sys_md_EntityType',
        items: [{
          attributes: [{
            id: 'aaaacxdco53w3krvac3owhyaae',
            name: 'id',
            _href: '/api/v2/sys_md_Attribute/aaaacxdco53w3krvac3owhyaae'
          }, {
            id: 'aaaacxdco53w3krvac3owhyaai',
            name: 'count',
            _href: '/api/v2/sys_md_Attribute/aaaacxdco53w3krvac3owhyaai'
          }],
          isAbstract: false,
          backend: 'PostgreSQL',
          description: 'This entity is used to group the index actions.',
          id: 'sys_idx_IndexActionGroup',
          label: 'Index action group',
          package: {id: 'sys_idx', label: 'Index', _href: '/api/v2/sys_md_Package/sys_idx'},
          tags: [],
          _href: '/api/v2/sys_md_EntityType/sys_idx_IndexActionGroup'
        }, {
          attributes: [{
            id: 'aaaacxdco53x5krvac3owhyaaq',
            name: '_key',
            _href: '/api/v2/sys_md_Attribute/aaaacxdco53x5krvac3owhyaaq'
          }, {
            id: 'aaaacxdco53x5krvac3owhyaau',
            name: 'content',
            _href: '/api/v2/sys_md_Attribute/aaaacxdco53x5krvac3owhyaau'
          }],
          isAbstract: false,
          backend: 'PostgreSQL',
          label: 'Static content',
          id: 'sys_StaticContent',
          package: {id: 'sys', label: 'System', _href: '/api/v2/sys_md_Package/sys'},
          tags: [],
          _href: '/api/v2/sys_md_EntityType/sys_StaticContent'
        }, {
          attributes: [{
            id: 'aaaacxdco53xzkrvac3owhyaae',
            name: 'status',
            _href: '/api/v2/sys_md_Attribute/aaaacxdco53xzkrvac3owhyaae'
          }],
          isAbstract: true,
          backend: 'PostgreSQL',
          label: 'Questionnaire',
          id: 'sys_Questionnaire',
          package: {id: 'sys', label: 'System', _href: '/api/v2/sys_md_Package/sys'},
          tags: [],
          _href: '/api/v2/sys_Questionnaire'
        }, {
          attributes: [
            {
              id: 'aaacxdc2l72fkrvac3owhyaae',
              name: 'id',
              _href: '/api/v2/sys_md_Attribute/aaaacxdc2l72fkrvac3owhyaae'
            }, {
              id: 'aaaacxdc2l72fkrvac3owhyaai',
              name: 'firstName',
              _href: '/api/v2/sys_md_Attribute/aaaacxdc2l72fkrvac3owhyaai'
            }, {
              id: 'aaaacxdc2l72fkrvac3owhyaam',
              name: 'lastName',
              _href: '/api/v2/sys_md_Attribute/aaaacxdc2l72fkrvac3owhyaam'
            }, {
              id: 'aaaacxdc2l72fkrvac3owhyaaq',
              name: 'present',
              _href: '/api/v2/sys_md_Attribute/aaaacxdc2l72fkrvac3owhyaaq'
            }, {
              id: 'aaaacxdc2l72fkrvac3owhyaau',
              name: 'extra',
              _href: '/api/v2/sys_md_Attribute/aaaacxdc2l72fkrvac3owhyaau'
            }],
          isAbstract: false,
          backend: 'PostgreSQL',
          label: 'borrel',
          id: 'demo_borrel',
          package: {id: 'demo', label: 'demo', _href: '/api/v2/sys_md_Package/demo'},
          tags: [],
          _href: '/api/v2/demo_borrel'
        }]
      }
      const get = td.function('api.get')
      td.when(get({apiUrl: '/api'}, '/v2/sys_md_EntityType?num=10000'))
        .thenResolve(mockedResponse)
      td.replace(api, 'get', get)
      testAction(actions.__GET_ENTITY_TYPES__, null, state, [{type: SET_ENTITY_TYPES, payload: mockedResponse.items}], [], done)
    })
    it('Should create alert when failing', done => {
      const mockedResponse = {
        errors: [{
          type: 'danger',
          message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
        }]
      }
      const get = td.function('api.get')
      td.when(get({apiUrl: '/api'}, '/v2/sys_md_EntityType?num=10000'))
        .thenReject(mockedResponse)

      td.replace(api, 'get', get)
      const payload = {
        type: 'danger',
        message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
      }
      testAction(actions.__GET_ENTITY_TYPES__, null, state, [{type: CREATE_ALERT, payload: payload}], [], done)
    })
  })
  describe('GET_ENTITY_TYPE_BY_ID', () => {
    afterEach(() => td.reset())
    const state = {
      alert: {
        message: null,
        type: null
      },
      packages: [],
      entityTypes: [],
      editorEntityType: {}
    }
    it('Should retrieve EditorEntityType based on EntityType ID', done => {
      const mockedResponse = {
        entityType: {
          id: 'root_gender',
          labelI18n: {},
          description: 'gender is located in the root package because it is not hospital specific',
          abstract0: false,
          attributes: [
            {
              aggregatable: false,
              auto: false,
              descriptionI18n: {},
              enumOptions: [],
              id: 'aaaacxdcqjnofkrvac3owhyabe',
              labelI18n: {},
              name: 'id',
              nullable: false,
              readonly: true,
              tags: [],
              type: 'STRING',
              unique: true,
              visible: true
            },
            {
              aggregatable: false,
              auto: false,
              descriptionI18n: {},
              enumOptions: [],
              id: 'aaaacxdcqjnofkrvac3owhyabi',
              labelI18n: {},
              name: 'label',
              nullable: false,
              readonly: true,
              tags: [],
              type: 'STRING',
              unique: true,
              visible: true
            }
          ],
          backend: 'postgreSQL',
          idAttribute: {id: 'aaaacxdcqjnofkrvac3owhyabe', label: 'id'},
          label: 'Gender',
          labelAttribute: {id: 'aaaacxdcqjnofkrvac3owhyabi', label: 'label'},
          lookupAttributes: [
            {id: 'aaaacxdcqjnofkrvac3owhyabe', label: 'id'},
            {id: 'aaaacxdcqjnofkrvac3owhyabi', label: 'label'}
          ],
          package0: {id: 'root', label: 'root'},
          tags: []
        },
        languageCodes: ['en', 'nl', 'de', 'es', 'it', 'pt', 'fr', 'xx']
      }
      const entityTypeID = 'root_gender'
      const get = td.function('api.get')
      td.when(get({apiUrl: '/metadata-manager-service'}, '/entityType/' + entityTypeID))
        .thenResolve(mockedResponse)
      td.replace(api, 'get', get)
      const payload = {
        id: 'root_gender',
        labelI18n: {},
        description: 'gender is located in the root package because it is not hospital specific',
        abstract0: false,
        attributes: [
          {
            aggregatable: false,
            auto: false,
            descriptionI18n: {},
            enumOptions: [],
            id: 'aaaacxdcqjnofkrvac3owhyabe',
            labelI18n: {},
            name: 'id',
            nullable: false,
            readonly: true,
            tags: [],
            type: 'STRING',
            unique: true,
            visible: true
          },
          {
            aggregatable: false,
            auto: false,
            descriptionI18n: {},
            enumOptions: [],
            id: 'aaaacxdcqjnofkrvac3owhyabi',
            labelI18n: {},
            name: 'label',
            nullable: false,
            readonly: true,
            tags: [],
            type: 'STRING',
            unique: true,
            visible: true
          }
        ],
        backend: 'postgreSQL',
        idAttribute: {id: 'aaaacxdcqjnofkrvac3owhyabe', label: 'id'},
        label: 'Gender',
        labelAttribute: {id: 'aaaacxdcqjnofkrvac3owhyabi', label: 'label'},
        lookupAttributes: [
          {id: 'aaaacxdcqjnofkrvac3owhyabe', label: 'id'},
          {id: 'aaaacxdcqjnofkrvac3owhyabi', label: 'label'}
        ],
        package0: {id: 'root', label: 'root'},
        tags: []
      }
      testAction(actions.__GET_ENTITY_TYPE_BY_ID__, 'root_gender', state, [{
        type: SET_EDITOR_ENTITY_TYPE,
        payload: payload
      }], [], done)
    })
    it('Should create alert when failing', done => {
      const mockedResponse = {
        errors: [{
          type: 'danger',
          message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
        }]
      }
      const entityTypeID = 'root_gender'
      const get = td.function('api.get')
      td.when(get({apiUrl: '/metadata-manager-service'}, '/entityType/' + entityTypeID))
        .thenReject(mockedResponse)
      td.replace(api, 'get', get)
      const payload = {
        type: 'danger',
        message: 'No [COUNT] permission on entity type [EntityType] with id [sys_md_EntityType]'
      }
      testAction(actions.__GET_ENTITY_TYPE_BY_ID__, entityTypeID, state, [{
        type: CREATE_ALERT,
        payload: payload
      }], [], done)
    })
  })
  describe('CREATE_ENTITY_TYPE', () => {
    afterEach(() => td.reset())
    const state = {
      alert: {
        message: null,
        type: null
      },
      packages: [],
      entityTypes: [],
      editorEntityType: {}
    }
    it('Should create an entity type', done => {
      const mockedResponse = {
        entityType: {
          id: 'aaaacxego5gxpkrvac3owhyaae',
          labelI18n: {},
          description: {},
          abstract0: false,
          attributes: [],
          backend: 'postgreSQL',
          lookupAttributes: [],
          tags: []
        },
        languageCodes: ['en', 'nl', 'de', 'es', 'it', 'pt', 'fr', 'xx']
      }
      const get = td.function('api.get')
      td.when(get({apiUrl: '/metadata-manager-service'}, '/create/entityType'))
        .thenResolve(mockedResponse)
      td.replace(api, 'get', get)
      const payload = {
        id: 'aaaacxego5gxpkrvac3owhyaae',
        labelI18n: {},
        description: {},
        abstract0: false,
        attributes: [],
        backend: 'postgreSQL',
        lookupAttributes: [],
        tags: []
      }
      testAction(actions.__CREATE_ENTITY_TYPE__, null, state, [{
        type: SET_EDITOR_ENTITY_TYPE,
        payload: payload
      }], [], done)
    })
    it('Should create alert when failing', done => {
      const mockedResponse = 'SyntaxError: Unexpected token < in JSON at position 0'
      const get = td.function('api.get')
      td.when(td.when(get({apiUrl: '/metadata-manager-service'}, '/create/entityType')))
        .thenReject(mockedResponse)
      td.replace(api, 'get', get)
      const payload = {
        type: 'danger',
        message: 'Something went wrong, make sure you have permissions for creating entities.'
      }
      testAction(actions.__CREATE_ENTITY_TYPE__, null, state, [{type: CREATE_ALERT, payload: payload}], [], done)
    })
  })
  describe('SAVE_EDITOR_ENTITY_TYPE', () => {
    afterEach(() => td.reset())
    it('Should persist metadata changes to the database', done => {
      const state = {
        alert: {
          message: null,
          type: null
        },
        packages: [],
        entityTypes: [],
        editorEntityType: {
          id: 'root_gender',
          labelI18n: {},
          description: 'gender is located in the root package because it is not hospital specific',
          abstract0: false,
          attributes: [
            {
              aggregatable: false,
              auto: false,
              descriptionI18n: {},
              enumOptions: [],
              id: 'aaaacxdcqjnofkrvac3owhyabe',
              labelI18n: {},
              name: 'id',
              nullable: false,
              readonly: true,
              tags: [],
              type: 'STRING',
              unique: true,
              visible: true
            },
            {
              aggregatable: false,
              auto: false,
              descriptionI18n: {},
              enumOptions: [],
              id: 'aaaacxdcqjnofkrvac3owhyabi',
              labelI18n: {},
              name: 'label',
              nullable: false,
              readonly: true,
              tags: [],
              type: 'STRING',
              unique: true,
              visible: true
            }
          ],
          backend: 'postgreSQL',
          idAttribute: {id: 'aaaacxdcqjnofkrvac3owhyabe', label: 'id'},
          label: 'Gender',
          labelAttribute: {id: 'aaaacxdcqjnofkrvac3owhyabi', label: 'label'},
          lookupAttributes: [
            {id: 'aaaacxdcqjnofkrvac3owhyabe', label: 'id'},
            {id: 'aaaacxdcqjnofkrvac3owhyabi', label: 'label'}
          ],
          package0: {id: 'root', label: 'root'},
          tags: []
        }
      }
      const mockedResponse = {
        entityType: {
          id: 'aaaacxego5gxpkrvac3owhyaae',
          labelI18n: {},
          description: {},
          abstract0: false,
          attributes: [],
          backend: 'postgreSQL',
          lookupAttributes: [],
          tags: []
        },
        languageCodes: ['en', 'nl', 'de', 'es', 'it', 'pt', 'fr', 'xx']
      }
      const updatedEditorEntityType = {
        id: 'root_gender',
        labelI18n: {},
        description: 'Gender of the patient',
        abstract0: false,
        attributes: [
          {
            aggregatable: false,
            auto: false,
            descriptionI18n: {},
            enumOptions: [],
            id: 'aaaacxdcqjnofkrvac3owhyabe',
            labelI18n: {},
            name: 'id',
            nullable: false,
            readonly: true,
            tags: [],
            type: 'STRING',
            unique: true,
            visible: true
          },
          {
            aggregatable: false,
            auto: false,
            descriptionI18n: {},
            enumOptions: [],
            id: 'aaaacxdcqjnofkrvac3owhyabi',
            labelI18n: {},
            name: 'label',
            nullable: false,
            readonly: true,
            tags: [],
            type: 'STRING',
            unique: true,
            visible: true
          }
        ],
        backend: 'postgreSQL',
        idAttribute: {id: 'aaaacxdcqjnofkrvac3owhyabe', label: 'id'},
        label: 'Gender',
        labelAttribute: {id: 'aaaacxdcqjnofkrvac3owhyabi', label: 'label'},
        lookupAttributes: [
          {id: 'aaaacxdcqjnofkrvac3owhyabe', label: 'id'},
          {id: 'aaaacxdcqjnofkrvac3owhyabi', label: 'label'}
        ],
        package0: {id: 'root', label: 'root'},
        tags: []
      }
      const post = td.function('api.post')
      td.when(post({apiUrl: '/metadata-manager-service'}, '/entityType', updatedEditorEntityType))
        .thenResolve(mockedResponse)
      td.replace(api, 'post', post)
      const payload = {
        type: 'success',
        message: 'Successfully updated metadata for EntityType: ' + updatedEditorEntityType.label
      }
      testAction(actions.__SAVE_EDITOR_ENTITY_TYPE__, updatedEditorEntityType, state, [{
        type: CREATE_ALERT,
        payload: payload
      }], [{type: 'GET_ENTITY_TYPES'}], done)
    })
    it('Should create alert when failing', done => {
      const state = {
        alert: {
          message: null,
          type: null
        },
        packages: [],
        entityTypes: [],
        editorEntityType: {
          id: 'aaaacxego5gxpkrvac3owhyaae',
          labelI18n: {},
          description: {},
          abstract0: false,
          attributes: [],
          backend: 'postgreSQL',
          lookupAttributes: [],
          tags: []
        }
      }
      const mockedResponse = {
        errors: [{
          type: 'danger',
          message: 'Entity [aaaacxehgxmy3krvac3owhyaae] does not contain any attributes. Did you use the correct package+entity name combination in both the entities as well as the attributes sheet?'
        }]
      }
      const updatedEditorEntityType = {
        id: 'aaaacxego5gxpkrvac3owhyaae',
        labelI18n: {},
        description: 'New description',
        abstract0: false,
        attributes: [],
        backend: 'postgreSQL',
        lookupAttributes: [],
        tags: []
      }
      const post = td.function('api.post')
      td.when(post({apiUrl: '/metadata-manager-service'}, '/entityType', updatedEditorEntityType))
        .thenReject(mockedResponse)
      td.replace(api, 'post', post)
      const payload = {
        type: 'danger',
        message: 'Entity [aaaacxehgxmy3krvac3owhyaae] does not contain any attributes. Did you use the correct package+entity name combination in both the entities as well as the attributes sheet?'
      }
      testAction(actions.__SAVE_EDITOR_ENTITY_TYPE__, updatedEditorEntityType, state, [{
        type: CREATE_ALERT,
        payload: payload
      }], [], done)
    })
  })
})
