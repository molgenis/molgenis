/**
 * Returns a property object used for creating a sweetalert2 modal when trying to delete an EntityType
 */
export const getConfirmBeforeDeletingProperties = (identifier) => {
  return {
    title: 'You are about to delete ' + identifier,
    text: 'Are you sure you want to continue?',
    type: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Yes, delete',
    cancelButtonText: 'Cancel'
  }
}

/**
 * Returns a property object used for creating a sweetalert2 modal when leaving a view with unsaved changes
 */
export const getConfirmBeforeLeavingProperties = () => {
  return {
    title: 'There are unsaved changes',
    text: 'All unsaved changes will be lost, are you sure you want to continue?',
    type: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Yes',
    cancelButtonText: 'No, stay'
  }
}

export default {
  /**
   * Returns the EntityType object based on the ID of the selected EntityType
   */
  getSelectedEntityType: state => state.entityTypes.find(entityType => entityType.id === state.selectedEntityTypeId),
  /**
   * Returns the Attribute object based on the ID of the selected Attribute
   */
  getSelectedAttribute: state => state.editorEntityType && state.editorEntityType.attributes.find(attribute => attribute.id === state.selectedAttributeId),
  /**
   * Returns the index of the currently selected attribute
   */
  getIndexOfSelectedAttribute: state => state.editorEntityType && state.editorEntityType.attributes.findIndex(attribute => attribute.id === state.selectedAttributeId),
  /**
   * Return the editorEntityType attributes from the state
   */
  getEditorEntityTypeAttributes: state => state.editorEntityType && state.editorEntityType.attributes,
  /**
   * Return the entityTypes from the state that are abstract
   */
  getAbstractEntities: state => state.entityTypes && state.entityTypes.filter(function (entityType) {
    return entityType.isAbstract
  }),
  /**
   * Return a tree which is constructed using the attributes found in the editorEntityType
   */
  getAttributeTree: (state, getters) => {
    const allAttributes = state.editorEntityType ? state.editorEntityType.attributes : []
    const rootAttributes = allAttributes.filter(attribute => !attribute.parent)
    const addChildren = attr => {
      const children = allAttributes.filter(attribute => attribute.parent && attribute.parent.id === attr.id)
      const offspring = children.map(addChildren)
      const selected = getters.getSelectedAttribute && attr.id === getters.getSelectedAttribute.id
      return { ...attr, children: offspring, selected }
    }
    return rootAttributes.map(addChildren)
  },
  /**
   * Return a list of referring attributes for mapped attributes property
   */
  getMappedByAttributes: state => {
    return state.initialEditorEntityType.referringAttributes
  },

  /**
   * Return a list of compound attributes present in the currently selected editorEntityType
   */
  getCompoundAttributes: state => state.editorEntityType && state.editorEntityType.attributes.filter(attribute => attribute.type === 'COMPOUND'),
  /**
   * Returns true if the editorEntityType has been updated through interaction with the forms
   */
  getEditorEntityTypeHasBeenEdited: state => state.editorEntityType && JSON.stringify(state.editorEntityType) !== JSON.stringify(state.initialEditorEntityType)
}
