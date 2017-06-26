export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state = {
  alert: { message: null, type: null },
  packages: [],
  entityTypes: [],
  attributeTypes: [],
  selectedEntityTypeId: null,
  selectedAttributeId: null,
  editorEntityType: null,
  initialEditorEntityType: null
}

export default state
