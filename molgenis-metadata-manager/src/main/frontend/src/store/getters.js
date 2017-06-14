export default {
  /**
   * Return the packages from the state
   */
  getPackages: state => state.packages,
  /**
   * Return the entityTypes from the state
   */
  getEntityTypes: state => state.entityTypes,
  /**
   * Return the attributeTypes from the state
   */
  getAttributeTypes: state => state.attributeTypes,
  /**
   * Return the entire entityType object based on the EntityTypeID in the URL
   */
  getSelectedEntityType: state => {
    const entityTypeID = state.route.params.entityTypeID
    return state.entityTypes.filter(entityType => {
      return entityType.id === entityTypeID
    })[0]
  },
  /**
   * Return the editorEntityType form the state
   */
  getEditorEntityType: state => state.editorEntityType,
  /**
   * Return the alert from the state
   */
  getAlert: state => state.alert,
  /**
   * Return the entityTypes from the state that are abstract
   * @param state
   */
  getAbstractEntities: state => state.entityTypes.filter(function (entityType) { return entityType.isAbstract }),
  /**
   * Return a tree which is constructed using the attributes found in the editorEntityType
   * @param state
   */
  getAttributeTree: (state, getters) => {
    const allAttributes = state.editorEntityType.attributes
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
   * @param state
   */
  getSelectedAttribute: state => state.editorEntityType.attributes.find(attribute => attribute.id === state.selectedAttributeId)
}
