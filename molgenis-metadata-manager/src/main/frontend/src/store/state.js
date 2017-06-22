export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state = {
  alert: {
    message: null,
    type: null
  },
  packages: [],
  entityTypes: [],
  selectedEntityType: null,
  attributeTypes: [],
  initialEditorEntityType: null,
  editorEntityType: null,
  selectedAttributeID: null
}

export default state
