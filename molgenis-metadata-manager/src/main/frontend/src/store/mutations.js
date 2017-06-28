export const SET_PACKAGES = '__SET_PACKAGES__'
export const SET_ENTITY_TYPES = '__SET_ENTITY_TYPES__'
export const SET_SELECTED_ENTITY_TYPE_ID = '__SET_SELECTED_ENTITY_TYPE_ID__'
export const SET_ATTRIBUTE_TYPES = '__SET_ATTRIBUTE_TYPES__'
export const SET_EDITOR_ENTITY_TYPE = '__SET_EDITOR_ENTITY_TYPE__'
export const UPDATE_EDITOR_ENTITY_TYPE = '__UPDATE_EDITOR_ENTITY_TYPE__'
export const UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE = '__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE__'
export const UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER = '__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER__'
export const SET_SELECTED_ATTRIBUTE_ID = '__SET_SELECTED_ATTRIBUTE_ID__'
export const DELETE_SELECTED_ATTRIBUTE = '__DELETE_SELECTED_ATTRIBUTE__'

export const CREATE_ALERT = '__CREATE_ALERT__'

import { swapArrayElements } from './utils/utils'

export default {
  [SET_PACKAGES] (state, packages) {
    state.packages = packages
  },
  [SET_ENTITY_TYPES] (state, entityTypes) {
    state.entityTypes = entityTypes.sort((a, b) => {
      return a.label.localeCompare(b.label)
    })
  },
  [SET_SELECTED_ENTITY_TYPE_ID] (state, entityTypeId) {
    state.selectedEntityTypeId = entityTypeId
  },
  [SET_ATTRIBUTE_TYPES] (state, attributeTypes) {
    state.attributeTypes = attributeTypes
  },
  /**
   * Set the editorEntityType in the state
   * Create a deep copy of the editorEntityType and store it in the state
   *
   * The deep copy is used to keep track of changes
   */
  [SET_EDITOR_ENTITY_TYPE] (state, editorEntityType) {
    state.editorEntityType = editorEntityType
    state.initialEditorEntityType = JSON.parse(JSON.stringify(editorEntityType))
  },
  [UPDATE_EDITOR_ENTITY_TYPE] (state, update) {
    // Add some extra changes to the attribute used as an idAttribute
    if (update.key === 'idAttribute') {
      update.value.readonly = true
      update.value.unique = true
      update.value.nullable = false

      const index = state.editorEntityType.attributes.findIndex(attribute => attribute.id === update.value.id)
      state.editorEntityType.attributes[index] = update.value
    }
    state.editorEntityType[update.key] = update.value
  },
  /**
   * Update the editorEntityType attribute list in place
   * Performs a key value update for the selected attribute
   * Updates an editorEntityType attribute via index
   */
  [UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE] (state, update) {
    // Return the index of the selected attribute in the array of the editorEntityType attributes
    const index = state.editorEntityType.attributes.findIndex(attribute => attribute.id === state.selectedAttributeId)
    const key = update.key

    state.editorEntityType.attributes[index][key] = update.value
  },
  [SET_SELECTED_ATTRIBUTE_ID] (state, selectedAttributeId) {
    state.selectedAttributeId = selectedAttributeId
  },
  /**
   * Move the selectedAttribute up or down based on the moveOrder
   */
  [UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER] (state, update) {
    const moveOrder = update.moveOrder
    const attributes = state.editorEntityType.attributes

    const originalIndex = update.selectedAttributeIndex
    const targetIndex = moveOrder === 'up' ? originalIndex - 1 : originalIndex + 1

    state.editorEntityType.attributes = swapArrayElements(attributes, originalIndex, targetIndex)
  },
  /**
   * Deletes the selected attribute using the ID of the selected attribute found in the state
   */
  [DELETE_SELECTED_ATTRIBUTE] (state, selectedAttributeId) {
    state.editorEntityType.attributes = state.editorEntityType.attributes.filter(attribute => attribute.id !== selectedAttributeId)
  },
  /**
   * Alert mutations
   * @param alert Object containing 'type' and 'message' Strings
   */
  [CREATE_ALERT] (state, alert) {
    state.alert = alert
  }
}
