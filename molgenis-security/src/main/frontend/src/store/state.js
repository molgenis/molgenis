// @flow
import type { State } from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  me: {
    username: 'admin'
  },
  selectedSid: null,
  sids: [{authority: 'user'}, {authority: 'SU'}],
  selectedEntityTypeId: null,
  permissions: ['WRITEMETA', 'WRITE', 'READ'],
  filter: null,
  rows: [],
  entityTypes: [
    {
      '_href': '/api/v2/sys_md_EntityType/sys_Questionnaire',
      'id': 'sys_Questionnaire',
      'label': 'Questionnaire'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_Plugin',
      'id': 'sys_Plugin',
      'label': 'Plugin'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_FileMeta',
      'id': 'sys_FileMeta',
      'label': 'File metadata'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_ImportRun',
      'id': 'sys_ImportRun',
      'label': 'Import',
      'description': 'Data import reports'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_sec_AclTest2',
      'id': 'sys_sec_AclTest2',
      'label': 'ACL test'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_md_Package',
      'id': 'sys_md_Package',
      'label': 'Package',
      'description': 'Grouping of related entities'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_md_EntityType',
      'id': 'sys_md_EntityType',
      'label': 'Entity type',
      'description': 'Meta data for entity classes'
    },
    {
      '_href': '/api/v2/sys_md_EntityType/sys_md_Attribute',
      'id': 'sys_md_Attribute',
      'label': 'Attribute',
      'description': 'Meta data for attributes'
    }
  ]
}

export default state
