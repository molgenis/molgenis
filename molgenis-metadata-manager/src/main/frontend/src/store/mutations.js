// @flow
import type {
  Alert,
  EditorAttribute,
  EditorEntityType,
  EditorPackageIdentifier,
  State,
  Update,
  UpdateOrder
} from '../flow.types'
import { INITIAL_STATE } from './state'

export const SET_PACKAGES: string = '__SET_PACKAGES__'
export const SET_ENTITY_TYPES: string = '__SET_ENTITY_TYPES__'
export const SET_SELECTED_ENTITY_TYPE_ID: string = '__SET_SELECTED_ENTITY_TYPE_ID__'
export const SET_ATTRIBUTE_TYPES: string = '__SET_ATTRIBUTE_TYPES__'
export const SET_EDITOR_ENTITY_TYPE: string = '__SET_EDITOR_ENTITY_TYPE__'
export const UPDATE_EDITOR_ENTITY_TYPE: string = '__UPDATE_EDITOR_ENTITY_TYPE__'
export const UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE: string = '__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE__'
export const UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER: string = '__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER__'
export const SET_SELECTED_ATTRIBUTE_ID: string = '__SET_SELECTED_ATTRIBUTE_ID__'
export const DELETE_SELECTED_ATTRIBUTE: string = '__DELETE_SELECTED_ATTRIBUTE__'

export const CREATE_ALERT: string = '__CREATE_ALERT__'
export const SET_LOADING = '__SET_LOADING__'

const SYS_PACKAGE_ID = 'sys'

/**
 * Swap the elements in an array at indexes originalIndex and targetIndex.
 *
 * @param (array) The array.
 * @param (originalIndex) The index of the first element to swap.
 * @param (targetIndex) The index of the second element to swap.
 * @return {Array} A new array with the elements swapped.
 */
const swapArrayElements = (array: Array<EditorAttribute>, originalIndex: number, targetIndex: number) => {
  if (array.length === 1) return array
  array.splice(targetIndex, 1, array.splice(originalIndex, 1, array[targetIndex])[0])
  return array
}

/**
 * Filter out all system entities unless user is superUser
 * @param entities
 * @returns {Array.<Object>}
 */
const filterNonVisibleEntities = (entities: Array<Object>) => {
  return INITIAL_STATE.isSuperUser ? entities : entities.filter(entity => !entity.id.startsWith(SYS_PACKAGE_ID + '_'))
}

/**
 * Filter out system package unless user is superUser
 * @param packages
 * @returns {Array.<Package>}
 */
const filterNonVisiblePackages = (packages: Array<EditorPackageIdentifier>) => {
  if (INITIAL_STATE.isSuperUser) {
    return packages
  }

  return packages
    .filter(_package => _package.id !== SYS_PACKAGE_ID)
    .filter(_package => !_package.id.startsWith(SYS_PACKAGE_ID + '_'))
}

const compareByLabel = (a, b) => a.label && b.label ? a.label.localeCompare(b.label) : 0

export default {
  [SET_PACKAGES] (state: State, packages: Array<EditorPackageIdentifier>) {
    const visiblePackages = filterNonVisiblePackages(packages)
    state.packages = visiblePackages.sort(compareByLabel)
  },
  [SET_ENTITY_TYPES] (state: State, entityTypes: Array<Object>) {
    const visibleEntities = filterNonVisibleEntities(entityTypes)
    state.entityTypes = visibleEntities.sort(compareByLabel)
  },
  [SET_SELECTED_ENTITY_TYPE_ID] (state: State, entityTypeId: string) {
    state.selectedEntityTypeId = entityTypeId
  },
  [SET_ATTRIBUTE_TYPES] (state: State, attributeTypes: Array<string>) {
    state.attributeTypes = attributeTypes
  },
  /**
   * Set the editorEntityType in the state
   * Create a deep copy of the editorEntityType and store it in the state
   *
   * The deep copy is used to keep track of changes
   */
  [SET_EDITOR_ENTITY_TYPE] (state: State, editorEntityType: EditorEntityType) {
    state.editorEntityType = editorEntityType
    state.initialEditorEntityType = JSON.parse(JSON.stringify(editorEntityType))
  },
  /**
   * Update currently selected EditorEntityType
   * update contains the parameter to update, together with the value
   *
   * In case of an idAttribute update, extra parameters are set
   */
  [UPDATE_EDITOR_ENTITY_TYPE] (state: State, update: Update) {
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
  [UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE] (state: State, update: Update) {
    const index = state.editorEntityType.attributes.findIndex(attribute => attribute.id === state.selectedAttributeId)
    const key = update.key

    const attr = state.editorEntityType.attributes[index]
    if (key === 'type') {
      if (attr.type === 'onetomany' || update.value === 'onetomany') {
        attr.mappedByAttribute = null
        attr.refEntityType = null
        attr.orderBy = null
      }
      attr[key] = update.value
    } else if (key === 'mappedByAttribute') {
      if (update.value !== null && update.value.entity !== null) {
        attr.refEntityType = update.value.entity
        attr.orderBy = null
      }
    }
    state.editorEntityType.attributes[index][key] = update.value
  },
  /**
   * Set the selected attribute ID in the state
   */
  [SET_SELECTED_ATTRIBUTE_ID] (state: State, selectedAttributeId: string) {
    state.selectedAttributeId = selectedAttributeId
  },
  /**
   * Move the selectedAttribute up or down based on the moveOrder
   */
  [UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER] (state: State, update: UpdateOrder) {
    const moveOrder = update.moveOrder
    const attributes = state.editorEntityType.attributes

    const originalIndex = update.selectedAttributeIndex
    const targetIndex = moveOrder === 'up' ? originalIndex - 1 : originalIndex + 1

    state.editorEntityType.attributes = swapArrayElements(attributes, originalIndex, targetIndex)
  },
  /**
   * Deletes the selected attribute using the ID of the selected attribute found in the state
   */
  [DELETE_SELECTED_ATTRIBUTE] (state: State, selectedAttributeId: string) {
    state.editorEntityType.attributes = state.editorEntityType.attributes.filter(attribute => attribute.id !== selectedAttributeId)
  },
  /**
   * Alert mutations
   * @param alert Object containing 'type' and 'message' Strings
   */
  [CREATE_ALERT] (state: State, alert: Alert) {
    state.alert = alert
  },
  [SET_LOADING] (state: State, loading: boolean) {
    state.loading = loading ? state.loading + 1 : state.loading - 1
  }
}
