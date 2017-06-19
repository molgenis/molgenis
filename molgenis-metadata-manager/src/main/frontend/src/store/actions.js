// $FlowFixMe
import { get, post } from 'molgenis-api-client'
import {
  UPDATE_EDITOR_ENTITY_TYPE,
  CREATE_ALERT, SET_EDITOR_ENTITY_TYPE,
  SET_ENTITY_TYPES, SET_PACKAGES,
  SET_SELECTED_ENTITY_TYPE,
  SET_SELECTED_ATTRIBUTE_ID,
  SET_ATTRIBUTE_TYPES
} from './mutations'

export const GET_PACKAGES = '__GET_PACKAGES__'
export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const GET_EDITOR_ENTITY_TYPE = '__GET_EDITOR_ENTITY_TYPE__'
export const CREATE_ENTITY_TYPE = '__CREATE_ENTITY_TYPE__'
export const CREATE_ATTRIBUTE = '__CREATE_ATTRIBUTE__'
export const SAVE_EDITOR_ENTITY_TYPE = '__SAVE_EDITOR_ENTITY_TYPE__'
export const GET_ATTRIBUTE_TYPES = '__GET_ATTRIBUTE_TYPES__'

export const toEntityType = (editorEntityType) => {
  return {
    'id': editorEntityType.id,
    'label': editorEntityType.label,
    'description': editorEntityType.description,
    'abstract_': editorEntityType.abstract_,
    'backend': editorEntityType.backend,
    'package_': editorEntityType.package_,
    'entityTypeParent': editorEntityType.entityTypeParent,
    'attributes': editorEntityType.attributes.map(attribute => toAttribute(attribute)),
    'tags': editorEntityType.tags,
    'idAttribute': editorEntityType.idAttribute,
    'labelAttribute': editorEntityType.labelAttribute,
    'lookupAttributes': editorEntityType.lookupAttributes
  }
}

export const toAttribute = (attribute) => {
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
    'description': attribute.description,
    'aggregatable': attribute.aggregatable,
    'enumOptions': attribute.enumOptions,
    'rangeMin': attribute.minRange,
    'rangeMax': attribute.maxRange,
    'readonly': attribute.readonly,
    'unique': attribute.unique,
    'tags': attribute.tags,
    'visibleExpression': attribute.visibleExpression,
    'validationExpression': attribute.validationExpression,
    'defaultValue': attribute.defaultValue
  }
}

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
  [GET_ENTITY_TYPES] ({commit, dispatch, state}) {
    // TODO can we filter system entities with REST call??
    get({apiUrl: '/api'}, '/v2/sys_md_EntityType?num=10000')
      .then(response => {
        commit(SET_ENTITY_TYPES, response.items)

        const entityTypeID = state.route.params.entityTypeID
        if (entityTypeID !== undefined) {
          dispatch(GET_EDITOR_ENTITY_TYPE, entityTypeID)

          const selectedEntityType = response.items.find(entityType => entityType.id === entityTypeID)
          commit(SET_SELECTED_ENTITY_TYPE, selectedEntityType)

          const attributeID = state.route.params.attributeID
          if (attributeID !== undefined) {
            commit(SET_SELECTED_ATTRIBUTE_ID, attributeID)
          }
        }
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
        commit(SET_ATTRIBUTE_TYPES, response.enumOptions.map((type) => type.toUpperCase()))
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
  [GET_EDITOR_ENTITY_TYPE] ({commit}, entityTypeID) {
    get({apiUrl: '/metadata-manager-service'}, '/entityType/' + entityTypeID)
      .then(response => {
        commit(SET_EDITOR_ENTITY_TYPE, toEntityType(response.entityType))
      }, error => {
        commit(CREATE_ALERT, {
          type: 'danger',
          message: error.errors[0].message
        })
      })
  },
  /**
   * Create a new EntityType
   * Response returns a blank EditorEntityType
   */
  [CREATE_ENTITY_TYPE] ({commit}) {
    get({apiUrl: '/metadata-manager-service'}, '/create/entityType')
      .then(response => {
        commit(SET_EDITOR_ENTITY_TYPE, toEntityType(response.entityType))
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
   * Create a new Attribute
   * Response returns a blank Attribute
   * Attribute is added to the list of attributes in the existing editorEntityType
   */
  [CREATE_ATTRIBUTE] ({commit, state}) {
    get({apiUrl: '/metadata-manager-service'}, '/create/attribute')
      .then(response => {
        const attribute = toAttribute(response.attribute)
        commit(SET_SELECTED_ATTRIBUTE_ID, attribute.id)

        // Call an update on the attribute key with the existing attribute list + the new empty attribute
        commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'attributes', value: [...state.editorEntityType.attributes, attribute]})
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
  [SAVE_EDITOR_ENTITY_TYPE] ({commit, dispatch, state}) {
    post({apiUrl: '/metadata-manager-service'}, '/entityType', state.editorEntityType)
      .then(response => {
        commit(CREATE_ALERT, {
          type: 'success',
          message: 'Successfully updated metadata for EntityType: ' + state.editorEntityType.label
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
