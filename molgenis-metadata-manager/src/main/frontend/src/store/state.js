// @flow
export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

import type { Alert, Package, EditorEntityType } from './utils/flow.types'

export type State = {
  alert: Alert,
  packages: Array<Package>,
  entityTypes: Array<Object>, // TODO create EntityType and Attribute type in flow.types.js
  attributeTypes: Array<String>,
  selectedEntityTypeId: ?string,
  selectedAttributeId: ?string,
  editorEntityType: ?EditorEntityType,
  initialEditorEntityType: ?EditorEntityType
}

const state: State = {
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
