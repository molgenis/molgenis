// @flow
import type {State} from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  me: {
    username: 'admin'
  },
  sidType: 'role',
  roles: [],
  users: [],
  groups: [],
  selectedSid: null,
  editRole: false,
  selectedEntityTypeId: null,
  permissions: ['WRITEMETA', 'DELETE', 'WRITE', 'READ', 'COUNT'],
  filter: '',
  rows: [],
  entityTypes: [],
  acl: null
}

export default state
