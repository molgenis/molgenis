export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state = {
  alert: {
    message: null,
    type: null
  },
  packages: [],
  entityTypes: [],
  attributeTypes: [],
  editorEntityType: {},
  selectedAttributeId: null
}

export default state
