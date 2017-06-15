export const SET_PACKAGES = '__SET_PACKAGES__'
export const SET_ENTITY_TYPES = '__SET_ENTITY_TYPES__'
export const SET_SELECTED_ENTITY_TYPE = '__SET_SELECTED_ENTITY_TYPE__'
export const SET_ATTRIBUTE_TYPES = '__SET_ATTRIBUTE_TYPES__'
export const SET_EDITOR_ENTITY_TYPE = '__SET_EDITOR_ENTITY_TYPE__'
export const CLEAR_EDITOR_ENTITY_TYPE = '__CLEAR_EDITOR_ENTITY_TYPE__'
export const UPDATE_EDITOR_ENTITY_TYPE = '__UPDATE_EDITOR_ENTITY_TYPE__'
export const SET_SELECTED_ATTRIBUTE_ID = '__SET_SELECTED_ATTRIBUTE_ID__'

export const CREATE_ALERT = '__CREATE_ALERT__'
export const REMOVE_ALERT = '__REMOVE_ALERT__'

export default {
  [SET_PACKAGES] (state, packages) {
    state.packages = packages
  },
  [SET_ENTITY_TYPES] (state, entityTypes) {
    state.entityTypes = entityTypes
  },
  [SET_SELECTED_ENTITY_TYPE] (state, selectedEntityType) {
    state.selectedEntityType = selectedEntityType
  },
  [SET_ATTRIBUTE_TYPES] (state, attributeTypes) {
    state.attributeTypes = attributeTypes
  },
  [SET_EDITOR_ENTITY_TYPE] (state, editorEntityType) {
    state.editorEntityType = editorEntityType
  },
  [CLEAR_EDITOR_ENTITY_TYPE] (state) {
    state.editorEntityType = {}
  },
  [UPDATE_EDITOR_ENTITY_TYPE] (state, update) {
    state.editorEntityType[update.key] = update.value
  },
  [SET_SELECTED_ATTRIBUTE_ID] (state, selectedAttributeID) {
    state.selectedAttributeID = selectedAttributeID
  },
  /**
   * Alert mutations
   * @param alert Object containing 'type' and 'message' Strings
   */
  [CREATE_ALERT] (state, alert) {
    state.alert = alert
  },
  [REMOVE_ALERT] (state) {
    state.alert.message = null
    state.alert.type = null
  }
}
