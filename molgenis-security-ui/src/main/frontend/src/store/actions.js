import {get, post} from '@molgenis/molgenis-api-client'
import {SET_ENTITY_TYPES, SET_FILTER, SET_ROLES, SET_ROWS} from './mutations'
import {debounce} from 'lodash'

export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const GET_ROLES = '__GET_SIDS__'
export const INITIALIZED = '__INITIALIZED__'
export const ENTITY_SELECTED = '__ENTITY_SELECTED__'
export const FILTER_CHANGED = '__FILTER_CHANGED__'
export const GET_ACLS = '__GET_ACLS__'
export const SAVE_ACL = '__SAVE_ACL__'
export const SAVE_CREATE_ROLE = '__SAVE_CREATE_ROLE__'

export default {
  [INITIALIZED] ({dispatch}) {
    dispatch(GET_ROLES)
    dispatch(GET_ENTITY_TYPES)
  },
  [GET_ROLES] ({commit}) {
    get('/api/v2/sys_sec_Role', {}).then(response => {
      commit(SET_ROLES, response.items)
    }, error => {
      console.log(error)
    })
  },
  [FILTER_CHANGED]: debounce(({state, dispatch, commit}, filter) => {
    commit(SET_FILTER, filter)
    if (state.selectedEntityTypeId) {
      dispatch(GET_ACLS)
    }
  }, 300),
  [SAVE_CREATE_ROLE] ({state, dispatch}, role) {
    post('/api/v2/sys_sec_Role', {
      body: JSON.stringify({entities: [role]}),
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(response => {
      dispatch(GET_ROLES)
    }, error => {
      console.log(error)
    })
  },
  [ENTITY_SELECTED] ({dispatch}) {
    dispatch(GET_ACLS)
  },
  [GET_ACLS] ({state: {selectedEntityTypeId, filter}, commit}) {
    let url = `/permission/acls?entityTypeId=${selectedEntityTypeId}&pageSize=100`
    if (filter) {
      url += `&filter=${encodeURIComponent(filter)}`
    }
    get(url, {}).then(response => {
      commit(SET_ROWS, response)
    }, error => {
      console.log(error)
    })
  },
  [SAVE_ACL] ({state}, aclIndex) {
    console.log(JSON.stringify(state.rows[aclIndex].acl))
    post('/permission/acl', {body: JSON.stringify(state.rows[aclIndex].acl), method: 'put'})
  },
  [GET_ENTITY_TYPES] ({commit}) {
    /**
     * Pass options to the fetch like body, method, x-molgenis-token etc...
     * @type {{}}
     */
    get('/api/v2/sys_md_EntityType?q=isEntityLevelSecurity==true;isAbstract==false&attrs=id,label,description', {}).then(response => {
      commit(SET_ENTITY_TYPES, response.items)
    }, error => {
      console.log(error)
    })
  }
}
