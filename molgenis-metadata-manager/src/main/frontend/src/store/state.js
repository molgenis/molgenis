// @flow
export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

import type { State } from '../flow.types'

const state: State = {
  alert: { message: null, type: null },
  packages: [],
  entityTypes: [],
  attributeTypes: [],
  selectedEntityTypeId: null,
  selectedAttributeId: null,
  editorEntityType: {
    'id': '',
    'label': '',
    'attributes': [],
    'lookupAttributes': []
  },
  initialEditorEntityType: {
    'id': '',
    'label': '',
    'attributes': [],
    'lookupAttributes': []
  }
}

export default state
