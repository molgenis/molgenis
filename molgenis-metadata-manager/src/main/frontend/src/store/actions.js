// @flow
import { get, post, delete_ } from '@molgenis/molgenis-api-client'

import {
  UPDATE_EDITOR_ENTITY_TYPE,
  CREATE_ALERT, SET_EDITOR_ENTITY_TYPE,
  SET_ENTITY_TYPES, SET_PACKAGES,
  SET_SELECTED_ENTITY_TYPE_ID,
  SET_SELECTED_ATTRIBUTE_ID,
  SET_ATTRIBUTE_TYPES
} from './mutations'

import type { EditorEntityType, EditorAttribute, State } from '../flow.types'

export const GET_PACKAGES: string = '__GET_PACKAGES__'
export const GET_ENTITY_TYPES: string = '__GET_ENTITY_TYPES__'
export const GET_EDITOR_ENTITY_TYPE: string = '__GET_EDITOR_ENTITY_TYPE__'
export const CREATE_ENTITY_TYPE: string = '__CREATE_ENTITY_TYPE__'
export const DELETE_ENTITY_TYPE: string = '__DELETE_ENTITY_TYPE__'
export const CREATE_ATTRIBUTE: string = '__CREATE_ATTRIBUTE__'
export const SAVE_EDITOR_ENTITY_TYPE: string = '__SAVE_EDITOR_ENTITY_TYPE__'
export const GET_ATTRIBUTE_TYPES: string = '__GET_ATTRIBUTE_TYPES__'

export const toEntityType = (editorEntityType: Object): EditorEntityType => {
  return {
    'id': editorEntityType.id,
    'label': editorEntityType.label,
    'i18nLabel': editorEntityType.i18nLabel,
    'description': editorEntityType.description,
    'i18nDescription': editorEntityType.i18nDescription,
    'abstract0': editorEntityType.abstract0,
    'backend': editorEntityType.backend,
    'package0': editorEntityType.package0,
    'entityTypeParent': editorEntityType.entityTypeParent,
    'attributes': editorEntityType.attributes.map(attribute => toAttribute(attribute)),
    'tags': editorEntityType.tags,
    'idAttribute': editorEntityType.idAttribute,
    'labelAttribute': editorEntityType.labelAttribute,
    'lookupAttributes': editorEntityType.lookupAttributes,
    'isNew': false
  }
}

export const toAttribute = (attribute: Object): EditorAttribute => {
  return {
    'id': attribute.id,
    'name': attribute.name,
    'type': attribute.type,
    'parent': attribute.parent,
    'refEntityType': attribute.refEntityType,
    'mappedByEntityType': attribute.mappedByEntityType,
    'orderBy': attribute.orderBy,
    'expression': attribute.expression,
    'nullable': attribute.nullable,
    'auto': attribute.auto,
    'visible': attribute.visible,
    'label': attribute.label,
    'i18nLabel': attribute.i18nLabel,
    'description': attribute.description,
    'i18nDescription': attribute.i18nDescription,
    'aggregatable': attribute.aggregatable,
    'enumOptions': attribute.enumOptions,
    'rangeMin': attribute.minRange,
    'rangeMax': attribute.maxRange,
    'readonly': attribute.readonly,
    'unique': attribute.unique,
    'tags': attribute.tags,
    'visibleExpression': attribute.visibleExpression,
    'validationExpression': attribute.validationExpression,
    'defaultValue': attribute.defaultValue,
    'sequenceNumber': attribute.sequenceNumber,
    'isNew': false
  }
}

export default {
  /**
   * Retrieve all Packages and filter on non-system Packages
   */
  [GET_PACKAGES] ({commit}: { commit: Function }) {
    get('/plugin/metadata-manager/editorPackages').then(response => {
      commit(SET_PACKAGES, response)
    }, error => {
      commit(CREATE_ALERT, {type: 'error', message: error.toString() + ' packages'})
    })
  },
  /**
   * Retrieve all EntityTypes and filter on non-system EntityTypes
   */
  [GET_ENTITY_TYPES] ({commit}: { commit: Function }) {
    get('/api/v2/sys_md_EntityType?num=10000').then(response => {
      commit(SET_ENTITY_TYPES, response.items)
    }, error => {
      commit(CREATE_ALERT, {type: 'error', message: error.toString()})
    })
  },
  /**
   * Retrieve all Attribute types
   */
  [GET_ATTRIBUTE_TYPES] ({commit}: { commit: Function }) {
    get('/api/v2/sys_md_Attribute/meta/type').then(response => {
      commit(SET_ATTRIBUTE_TYPES, response.enumOptions.map((type) => type.toUpperCase()))
    }, error => {
      commit(CREATE_ALERT, {type: 'error', message: error.toString() + ' attribute types'})
    })
  },
  /**
   * Retrieve EditorEntityType based on EntityType ID
   *
   * @param entityTypeId The selected EntityType identifier
   */
  [GET_EDITOR_ENTITY_TYPE] ({commit}: { commit: Function }, entityTypeId: string) {
    get('/plugin/metadata-manager/entityType/' + entityTypeId).then(response => {
      commit(SET_EDITOR_ENTITY_TYPE, toEntityType(response.entityType))
    }, error => {
      commit(CREATE_ALERT, {type: 'error', message: error})
    })
  },
  /**
   * Create a new EntityType
   * Response returns a blank EditorEntityType
   * EditorEntityType is added to the list of entityTypes in the state
   */
  [CREATE_ENTITY_TYPE] ({commit}: { commit: Function }) {
    get('/plugin/metadata-manager/create/entityType').then(response => {
      const newEditorEntityType = toEntityType(response.entityType)
      newEditorEntityType.isNew = true
      commit(SET_EDITOR_ENTITY_TYPE, newEditorEntityType)
      commit(SET_SELECTED_ENTITY_TYPE_ID, newEditorEntityType.id)
    }, error => {
      commit(CREATE_ALERT, {type: 'error', message: error})
    })
  },
  /**
   * Deletes an EntityType and reloads the EntityTypes present in the state
   *
   * @param selectedEntityTypeId the ID of the EntityType to be deleted
   */
  [DELETE_ENTITY_TYPE] ({commit, state}: { commit: Function, state: State }, selectedEntityTypeId: string) {
    delete_('/api/v1/' + selectedEntityTypeId + '/meta').then(response => {
      commit(SET_ENTITY_TYPES, state.entityTypes.filter(entityType => entityType.id !== selectedEntityTypeId))
      commit(SET_SELECTED_ENTITY_TYPE_ID, null)
      commit(SET_SELECTED_ATTRIBUTE_ID, null)
      commit(SET_EDITOR_ENTITY_TYPE, null)

      const message = response.statusText + ': Successfully deleted EntityType [' + selectedEntityTypeId + ']'
      commit(CREATE_ALERT, {type: 'success', message: message})
    }, error => {
      commit(CREATE_ALERT, {type: 'error', message: error})
    })
  },
  /**
   * Create a new Attribute
   * Response returns a blank Attribute
   * Attribute is added to the list of attributes in the existing editorEntityType
   */
  [CREATE_ATTRIBUTE] ({commit, state}: { commit: Function, state: State }) {
    get('/plugin/metadata-manager/create/attribute').then(response => {
      const attribute = toAttribute(response.attribute)
      attribute.isNew = true
      commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'attributes', value: [...state.editorEntityType.attributes, attribute]})
      commit(SET_SELECTED_ATTRIBUTE_ID, attribute.id)
    }, error => {
      commit(CREATE_ALERT, {type: 'error', message: error})
    })
  },
  /**
   * Persist metadata changes to the database
   */
  [SAVE_EDITOR_ENTITY_TYPE] ({commit, state}: { commit: Function, state: State }) {
    const options = {
      body: JSON.stringify(state.editorEntityType)
    }

    post('/plugin/metadata-manager/entityType', options).then(response => {
      const message = response.statusText + ': Successfully updated metadata for EntityType: ' + state.editorEntityType.label
      commit(CREATE_ALERT, { type: 'success', message: message })

      if (state.editorEntityType.isNew) {
        const editorEntityType = JSON.parse(JSON.stringify(state.editorEntityType))

        editorEntityType.isNew = false
        editorEntityType.attributes.forEach(attribute => {
          attribute.isNew = false
        })

        commit(SET_SELECTED_ENTITY_TYPE_ID, editorEntityType.id)
        commit(SET_ENTITY_TYPES, [...state.entityTypes, editorEntityType])
      } else {
        commit(SET_EDITOR_ENTITY_TYPE, state.editorEntityType)
      }
    }, error => {
      commit(CREATE_ALERT, {type: 'error', message: error})
    })
  }
}
