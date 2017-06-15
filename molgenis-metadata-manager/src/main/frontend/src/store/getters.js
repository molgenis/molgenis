export default {
  /**
   * Return the editorEntityType attributes from the state
   */
  getEditorEntityTypeAttributes: state => state.editorEntityType && state.editorEntityType.attributes,
  /**
   * Return the entityTypes from the state that are abstract
   */
  getAbstractEntities: state => state.entityTypes && state.entityTypes.filter(function (entityType) { return entityType.isAbstract }),
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
      return {...attr, children: offspring, selected}
    }
    return rootAttributes.map(addChildren)
  },
  /**
   * Returns attribute based on selected attribute in tree
   * Returns null if no attribute is selected
   */
  getSelectedAttribute: state => state.editorEntityType && state.editorEntityType.attributes.find(attribute => state.selectedAttributeID && state.selectedAttributeID === attribute.id),
  /**
   * Return the index of the selected attribute in the array of the editorEntityType attributes
   */
  getIndexOfSelectedAttribute: state => state.selectedAttributeID && state.editorEntityType && state.editorEntityType.attributes.findIndex(attribute => attribute.id === state.selectedAttributeID)
}
