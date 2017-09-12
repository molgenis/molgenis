// @flow
import type {State} from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  me: {
    username: 'admin'
  },
  sidType: 'role',
  roles: [],
  selectedSid: null,
  users: null,
  groups: null,
  doCreateRole: false,
  doUpdateRole: false,
  selectedEntityTypeId: null,
  permissions: ['DELETE', 'WRITE', 'READ'],
  filter: '',
  rows: [],
  entityTypes: []
}

export default state
