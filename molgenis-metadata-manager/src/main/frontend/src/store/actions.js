import { get, post, callApi } from 'molgenis-api-client'
import { toEntityType, toAttribute } from './utils/utils'

import {
  UPDATE_EDITOR_ENTITY_TYPE,
  CREATE_ALERT, SET_EDITOR_ENTITY_TYPE,
  SET_ENTITY_TYPES, SET_PACKAGES,
  SET_SELECTED_ENTITY_TYPE_ID,
  SET_SELECTED_ATTRIBUTE_ID,
  SET_ATTRIBUTE_TYPES
} from './mutations'

export const GET_PACKAGES = '__GET_PACKAGES__'
export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const GET_EDITOR_ENTITY_TYPE = '__GET_EDITOR_ENTITY_TYPE__'
export const CREATE_ENTITY_TYPE = '__CREATE_ENTITY_TYPE__'
export const DELETE_ENTITY_TYPE = '__DELETE_ENTITY_TYPE__'
export const CREATE_ATTRIBUTE = '__CREATE_ATTRIBUTE__'
export const SAVE_EDITOR_ENTITY_TYPE = '__SAVE_EDITOR_ENTITY_TYPE__'
export const GET_ATTRIBUTE_TYPES = '__GET_ATTRIBUTE_TYPES__'

export default {
  /**
   * Retrieve all Packages and filter on non-system Packages
   */
  [GET_PACKAGES] ({commit}) {
    // TODO filter system packages
    get({apiUrl: '/metadata-manager-service'}, '/editorPackages')
      .then(response => {
        commit(SET_PACKAGES, response)
      }, error => {
        commit(CREATE_ALERT, {
          type: 'error',
          message: error.errors[0].message
        })
      })
  },
  /**
   * Retrieve all EntityTypes and filter on non-system EntityTypes
   */
  [GET_ENTITY_TYPES] ({commit}) {
    // TODO can we filter system entities with REST call??
    get({apiUrl: '/api'}, '/v2/sys_md_EntityType?num=10000')
      .then(response => {
        commit(SET_ENTITY_TYPES, response.items)
      }, error => {
        commit(CREATE_ALERT, {
          type: 'error',
          message: error.errors[0].message
        })
      })
  },
  /**
   * Retrieve all Attribute types
   */
  [GET_ATTRIBUTE_TYPES] ({commit}) {
    get({apiUrl: '/api'}, '/v2/sys_md_Attribute/meta/type')
      .then(response => {
        commit(SET_ATTRIBUTE_TYPES, response.enumOptions.map((type) => type.toUpperCase()))
      }, error => {
        commit(CREATE_ALERT, {
          type: 'error',
          message: error.errors[0].message
        })
      })
  },
  /**
   * Retrieve EditorEntityType based on EntityType ID
   *
   * @param entityTypeId The selected EntityType identifier
   */
  [GET_EDITOR_ENTITY_TYPE] ({commit}, entityTypeId) {
    get({apiUrl: '/metadata-manager-service'}, '/entityType/' + entityTypeId)
      .then(response => {
        commit(SET_EDITOR_ENTITY_TYPE, toEntityType(response.entityType))
      }, error => {
        commit(CREATE_ALERT, {
          type: 'error',
          message: error.errors[0].message
        })
      })
  },
  /**
   * Create a new EntityType
   * Response returns a blank EditorEntityType
   * EditorEntityType is added to the list of entityTypes in the state
   */
  [CREATE_ENTITY_TYPE] ({commit}) {
    get({apiUrl: '/metadata-manager-service'}, '/create/entityType')
      .then(response => {
        commit(SET_EDITOR_ENTITY_TYPE, toEntityType(response.entityType))
      }, error => {
        if (error.errors) {
          commit(CREATE_ALERT, {
            type: 'error',
            message: error.errors[0].message
          })
        } else {
          commit(CREATE_ALERT, {
            type: 'error',
            message: 'Something went wrong, make sure you have permissions for creating entities.'
          })
        }
      })
  },
  /**
   * Deletes an EntityType and reloads the EntityTypes present in the state
   *
   * @param selectedEntityTypeId the ID of the EntityType to be deleted
   */
  [DELETE_ENTITY_TYPE] ({commit, state}, selectedEntityTypeId) {
    callApi({apiUrl: '/api'}, '/v1/' + selectedEntityTypeId + '/meta', 'delete')
      .then(response => {
        // Never reached due to https://github.com/molgenis/molgenis-api-client/issues/1
      }, error => {
        if (error.errors) {
          commit(CREATE_ALERT, {
            type: 'error',
            message: error.errors[0].message
          })
        } else {
          // Clear selected editorEntityType
          commit(SET_EDITOR_ENTITY_TYPE, null)

          // Remove EntityType that was just deleted from list of EntityTypes
          commit(SET_ENTITY_TYPES, state.entityTypes.filter(entityType => entityType.id !== selectedEntityTypeId))

          // Clear selected entity type in dropdown
          commit(SET_SELECTED_ENTITY_TYPE_ID, null)

          // Clear selected attribute
          commit(SET_SELECTED_ATTRIBUTE_ID, null)
        }
      })
  },
  /**
   * Create a new Attribute
   * Response returns a blank Attribute
   * Attribute is added to the list of attributes in the existing editorEntityType
   */
  [CREATE_ATTRIBUTE] ({commit, state}) {
    get({apiUrl: '/metadata-manager-service'}, '/create/attribute')
      .then(response => {
        const attribute = toAttribute(response.attribute)

        // Call an update on the attribute key with the existing attribute list + the new empty attribute
        commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'attributes', value: [...state.editorEntityType.attributes, attribute]})
        commit(SET_SELECTED_ATTRIBUTE_ID, attribute.id)
      }, error => {
        if (error.errors) {
          commit(CREATE_ALERT, {
            type: 'error',
            message: error.errors[0].message
          })
        } else {
          commit(CREATE_ALERT, {
            type: 'error',
            message: 'Something went wrong, make sure you have permissions for creating entities.'
          })
        }
      })
  },
  /**
   * Persist metadata changes to the database
   * @param updatedEditorEntityType the updated EditorEntityType
   */
  [SAVE_EDITOR_ENTITY_TYPE] ({commit, state}) {
    post({apiUrl: '/metadata-manager-service'}, '/entityType', state.editorEntityType)
      .then(response => {
        commit(CREATE_ALERT, {
          type: 'success',
          message: 'Successfully updated metadata for EntityType: ' + state.editorEntityType.label
        })
      }, error => {
        commit(CREATE_ALERT, {
          type: 'error',
          message: error.errors[0].message
        })
      })
  }
}
