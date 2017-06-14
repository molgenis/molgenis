// $FlowFixMe
import { get, post } from 'molgenis-api-client'
import { CREATE_ALERT, SET_EDITOR_ENTITY_TYPE, SET_ENTITY_TYPES, SET_PACKAGES } from './mutations'

export const GET_PACKAGES = '__GET_PACKAGES__'
export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const GET_ENTITY_TYPE_BY_ID = '__GET_ENTITY_TYPE_BY_ID__'
export const CREATE_ENTITY_TYPE = '__CREATE_ENTITY_TYPE__'
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
          type: 'danger',
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
          type: 'danger',
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
        response.enumOptions
      }, error => {
        commit(CREATE_ALERT, {
          type: 'danger',
          message: error.errors[0].message
        })
      })
  },
  /**
   * Retrieve EditorEntityType based on EntityType ID
   *
   * @param entityTypeID The selected EntityType identifier
   */
  [GET_ENTITY_TYPE_BY_ID] ({commit}, entityTypeID) {
    get({apiUrl: '/metadata-manager-service'}, '/entityType/' + entityTypeID)
      .then(response => {
        commit(SET_EDITOR_ENTITY_TYPE, response.entityType)
      }, error => {
        commit(CREATE_ALERT, {
          type: 'danger',
          message: error.errors[0].message
        })
      })
  },
  /**
   * Create a new EntityType
   * Response returns an blank EditorEntityType
   */
  [CREATE_ENTITY_TYPE] ({commit}) {
    get({apiUrl: '/metadata-manager-service'}, '/create/entityType')
      .then(response => {
        commit(SET_EDITOR_ENTITY_TYPE, response.entityType)
      }, error => {
        if (error.errors) {
          commit(CREATE_ALERT, {
            type: 'danger',
            message: error.errors[0].message
          })
        } else {
          commit(CREATE_ALERT, {
            type: 'danger',
            message: 'Something went wrong, make sure you have permissions for creating entities.'
          })
        }
      })
  },
  /**
   * Persist metadata changes to the database
   * @param updatedEditorEntityType the updated EditorEntityType
   */
  [SAVE_EDITOR_ENTITY_TYPE] ({commit, dispatch}, updatedEditorEntityType) {
    post({apiUrl: '/metadata-manager-service'}, '/entityType', updatedEditorEntityType)
      .then(response => {
        commit(CREATE_ALERT, {
          type: 'success',
          message: 'Successfully updated metadata for EntityType: ' + updatedEditorEntityType.label
        })
        dispatch(GET_ENTITY_TYPES)
      }, error => {
        commit(CREATE_ALERT, {
          type: 'danger',
          message: error.errors[0].message
        })
      })
  }
}
