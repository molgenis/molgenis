// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'

import {
  CREATE_ALERT,
  SET_ATTRIBUTE_TYPES,
  SET_EDITOR_ENTITY_TYPE,
  SET_ENTITY_TYPES,
  SET_LANGUAGE_CODES,
  SET_LOADING,
  SET_PACKAGES,
  SET_SELECTED_ATTRIBUTE_ID,
  SET_SELECTED_ENTITY_TYPE_ID,
  UPDATE_EDITOR_ENTITY_TYPE
} from './mutations'

import type { EditorAttribute, EditorEntityType, State } from '../flow.types'

export const GET_PACKAGES: string = '__GET_PACKAGES__'
export const GET_ENTITY_TYPES: string = '__GET_ENTITY_TYPES__'
export const RESET_EDITOR_ENTITY_TYPE: string = '__RESET_EDITOR_ENTITY_TYPE__'
export const GET_EDITOR_ENTITY_TYPE: string = '__GET_EDITOR_ENTITY_TYPE__'
export const CREATE_ENTITY_TYPE: string = '__CREATE_ENTITY_TYPE__'
export const DELETE_ENTITY_TYPE: string = '__DELETE_ENTITY_TYPE__'
export const CREATE_ATTRIBUTE: string = '__CREATE_ATTRIBUTE__'
export const SAVE_EDITOR_ENTITY_TYPE: string = '__SAVE_EDITOR_ENTITY_TYPE__'
export const GET_ATTRIBUTE_TYPES: string = '__GET_ATTRIBUTE_TYPES__'

export const toLanguageCodes = (languageCodes: Array<string>): Array<string> => {
  return languageCodes
}

export const toEntityType = (editorEntityType: Object): EditorEntityType => {
  return {
    'id': editorEntityType.id,
    'label': editorEntityType.label,
    'labelI18n': editorEntityType.labelI18n,
    'description': editorEntityType.description,
    'descriptionI18n': editorEntityType.descriptionI18n,
    'abstract0': editorEntityType.abstract0,
    'backend': editorEntityType.backend,
    'package0': editorEntityType.package0,
    'entityTypeParent': editorEntityType.entityTypeParent,
    'attributes': editorEntityType.attributes.map(
      attribute => toAttribute(attribute)),
    'referringAttributes': editorEntityType.referringAttributes,
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
    'cascadeDelete': attribute.cascadeDelete,
    'mappedByAttribute': attribute.mappedByAttribute,
    'orderBy': attribute.orderBy,
    'expression': attribute.expression,
    'nullable': attribute.nullable,
    'auto': attribute.auto,
    'visible': attribute.visible,
    'label': attribute.label,
    'labelI18n': attribute.labelI18n,
    'description': attribute.description,
    'descriptionI18n': attribute.descriptionI18n,
    'aggregatable': attribute.aggregatable,
    'enumOptions': attribute.enumOptions,
    'rangeMin': attribute.minRange,
    'rangeMax': attribute.maxRange,
    'readonly': attribute.readonly,
    'unique': attribute.unique,
    'tags': attribute.tags,
    'nullableExpression': attribute.nullableExpression,
    'visibleExpression': attribute.visibleExpression,
    'validationExpression': attribute.validationExpression,
    'defaultValue': attribute.defaultValue,
    'sequenceNumber': attribute.sequenceNumber,
    'isNew': false
  }
}

const withSpinner = (commit, promise) => {
  commit(SET_LOADING, true)
  promise.catch(error => {
    commit(CREATE_ALERT, {type: 'error', message: error})
  }).then(() => commit(SET_LOADING, false), 1000)
}

function getEditorEntityType (commit: Function, entityTypeId: string) {
  withSpinner(commit,
    api.get('/plugin/metadata-manager/entityType/' + entityTypeId).then(
      response => {
        commit(SET_LANGUAGE_CODES, toLanguageCodes(response.languageCodes))
        commit(SET_EDITOR_ENTITY_TYPE, toEntityType(response.entityType))
      }))
}

export default {
  /**
   * Retrieve all Packages and filter on non-system Packages
   */
  [GET_PACKAGES] ({commit}: { commit: Function }) {
    withSpinner(commit,
      api.get('/plugin/metadata-manager/editorPackages').then(response => {
        commit(SET_PACKAGES, response)
      }))
  },
  /**
   * Retrieve all EntityTypes and filter on non-system EntityTypes
   */
  [GET_ENTITY_TYPES] ({commit}: { commit: Function }) {
    withSpinner(commit,
      api.get('/api/v2/sys_md_EntityType?num=10000').then(response => {
        commit(SET_ENTITY_TYPES, response.items)
      }))
  },
  /**
   * Retrieve all Attribute types
   */
  [GET_ATTRIBUTE_TYPES] ({commit}: { commit: Function }) {
    withSpinner(commit,
      api.get('/api/v2/sys_md_Attribute/meta/type').then(response => {
        commit(SET_ATTRIBUTE_TYPES, response.enumOptions)
      }))
  },
  /**
   * Resets the current EditorEntityType
   */
  [RESET_EDITOR_ENTITY_TYPE] ({commit, state}: { commit: Function, state: State }) {
    if (state.selectedEntityTypeId != null && state.selectedEntityTypeId !== undefined) {
      getEditorEntityType(commit, state.selectedEntityTypeId)
    }
  },
  /**
   * Retrieve EditorEntityType based on EntityType ID
   *
   * @param entityTypeId The selected EntityType identifier
   */
  [GET_EDITOR_ENTITY_TYPE] ({commit}: { commit: Function },
    entityTypeId: string) {
    getEditorEntityType(commit, entityTypeId)
  },
  /**
   * Create a new EntityType
   * Response returns a blank EditorEntityType
   * EditorEntityType is added to the list of entityTypes in the state
   */
  [CREATE_ENTITY_TYPE] ({commit}: { commit: Function }) {
    withSpinner(commit,
      api.get('/plugin/metadata-manager/create/entityType').then(response => {
        const newEditorEntityType = toEntityType(response.entityType)
        newEditorEntityType.isNew = true
        commit(SET_EDITOR_ENTITY_TYPE, newEditorEntityType)
        commit(SET_SELECTED_ENTITY_TYPE_ID, newEditorEntityType.id)
      }))
  },
  /**
   * Deletes an EntityType and reloads the EntityTypes present in the state
   *
   * @param selectedEntityTypeId the ID of the EntityType to be deleted
   */
  [DELETE_ENTITY_TYPE] ({commit, state}: { commit: Function, state: State },
    selectedEntityTypeId: string) {
    withSpinner(commit,
      api.delete_('/api/v1/' + selectedEntityTypeId + '/meta').then(
        response => {
          commit(SET_ENTITY_TYPES, state.entityTypes.filter(
            entityType => entityType.id !== selectedEntityTypeId))
          commit(SET_SELECTED_ENTITY_TYPE_ID, null)
          commit(SET_SELECTED_ATTRIBUTE_ID, null)
          commit(SET_EDITOR_ENTITY_TYPE, null)
          commit(CREATE_ALERT, {
            type: 'info',
            message: 'Delete was successful: ' + response.statusText
          })
        }))
  },
  /**
   * Create a new Attribute
   * Response returns a blank Attribute
   * Attribute is added to the list of attributes in the existing editorEntityType
   */
  [CREATE_ATTRIBUTE] ({commit, state}: { commit: Function, state: State }) {
    withSpinner(commit,
      api.get('/plugin/metadata-manager/create/attribute').then(response => {
        const attribute = toAttribute(response.attribute)
        attribute.isNew = true
        commit(UPDATE_EDITOR_ENTITY_TYPE, {
          key: 'attributes',
          value: [...state.editorEntityType.attributes, attribute]
        })
        commit(SET_SELECTED_ATTRIBUTE_ID, attribute.id)
      }))
  },
  /**
   * Persist metadata changes to the database
   */
  [SAVE_EDITOR_ENTITY_TYPE] ({commit, state}: { commit: Function, state: State },
    t: Function) {
    const options = {
      body: JSON.stringify(state.editorEntityType)
    }
    withSpinner(commit,
      api.post('/plugin/metadata-manager/entityType', options).then(
        response => {
          commit(CREATE_ALERT, {
            type: 'success',
            message: response.statusText + ': ' + t('save-succes-message') +
            ': ' + state.editorEntityType.label
          })

          if (state.editorEntityType.isNew) {
            const editorEntityType = JSON.parse(
              JSON.stringify(state.editorEntityType))

            editorEntityType.isNew = false
            editorEntityType.attributes.forEach(attribute => {
              attribute.isNew = false
            })

            commit(SET_SELECTED_ENTITY_TYPE_ID, editorEntityType.id)
            commit(SET_ENTITY_TYPES, [...state.entityTypes, editorEntityType])
          } else {
            commit(SET_EDITOR_ENTITY_TYPE, state.editorEntityType)
          }
        }))
  }
}
