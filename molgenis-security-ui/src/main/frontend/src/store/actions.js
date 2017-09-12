import {delete_, get, post} from '@molgenis/molgenis-api-client'
import {
  CANCEL_CREATE_ROLE,
  CANCEL_UPDATE_ROLE,
  SET_ENTITY_TYPES,
  SET_FILTER,
  SET_GROUPS,
  SET_ROLES,
  SET_ROWS,
  SET_SELECTED_ROLE,
  SET_USERS,
  SET_ACL
} from './mutations'
import {debounce} from 'lodash'

export const SELECT_ROLE = '__SELECT_ROLE__'
export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const GET_ROLES = '__GET_SIDS__'
export const INITIALIZED = '__INITIALIZED__'
export const ENTITY_SELECTED = '__ENTITY_SELECTED__'
export const FILTER_CHANGED = '__FILTER_CHANGED__'
export const GET_ACLS = '__GET_ACLS__'
export const SAVE_ACL = '__SAVE_ACL__'
export const SAVE_CREATE_ROLE = '__SAVE_CREATE_ROLE__'
export const UPDATE_ROLE = '__UPDATE_ROLE__'
export const DELETE_ROLE = '__DELETE_ROLE__'
export const GET_ACL = '__GET_ACL__'

export default {
  [INITIALIZED] ({dispatch}) {
    dispatch(GET_ROLES)
    dispatch(GET_ENTITY_TYPES)
  },
  [GET_ROLES] ({state, commit, dispatch}) {
    if (state.sidType === 'role') {
      get('/api/v2/sys_sec_Role?sort=label', {}).then(response => {
        commit(SET_ROLES, response.items)
        dispatch(SELECT_ROLE, response.items[0].id)
      }, error => {
        console.log(error)
      })
    } else {
      get('/api/v2/sys_sec_User?sort=username', {}).then(response => {
        commit(SET_ROLES, response.items)
        dispatch(SELECT_ROLE, response.items[0].username)
      }, error => {
        console.log(error)
      })
    }
  },
  [SELECT_ROLE] ({state, commit}, role) {
    commit(SET_SELECTED_ROLE, role)
    if (state.sidType === 'role') {
      get(`/api/v2/sys_sec_UserAuthority?q=role==${role}&sort=User`, {}).then(response => {
        commit(SET_USERS, response.items.map(e => e.User.username))
      }, error => {
        console.log(error)
      })
      get(`/api/v2/sys_sec_GroupAuthority?q=role==${role}&sort=Group`, {}).then(response => {
        commit(SET_GROUPS, response.items.map(e => e.Group.name))
      }, error => {
        console.log(error)
      })
    }
  },
  [FILTER_CHANGED]: debounce(({state, dispatch, commit}, filter) => {
    commit(SET_FILTER, filter)
    if (state.selectedEntityTypeId) {
      dispatch(GET_ACLS)
    }
  }, 300),
  [SAVE_CREATE_ROLE] ({state, commit, dispatch}, role) {
    post('/api/v2/sys_sec_Role', {
      body: JSON.stringify({entities: [role]}),
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(response => {
      commit(CANCEL_CREATE_ROLE)
      dispatch(GET_ROLES)
    }, error => {
      console.log(error)
    })
  },
  [UPDATE_ROLE] ({state, commit, dispatch}, role) {
    post('/api/v2/sys_sec_Role/', {
      body: JSON.stringify({
        entities: [{...role, id: state.selectedSid}]
      }),
      headers: {
        'Content-Type': 'application/json'
      },
      method: 'put'
    }).then(response => {
      commit(CANCEL_UPDATE_ROLE)
      dispatch(GET_ROLES)
    }, error => {
      console.log(error)
    })
  },
  [DELETE_ROLE] ({state, dispatch}, roleId) {
    delete_('/api/v2/sys_sec_Role/' + roleId).then(response => {
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
  [GET_ACL] ({state: {route: {params: {entityType, id}}}, commit}) {
    console.log('GET_ACL!!')
    // TODO: urlencode id!
    let url = `/permission/acl?entityTypeId=${entityType}&entityId=${id}`
    get(url, {}).then(response => {
      commit(SET_ACL, response)
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
    get('/api/v2/sys_md_EntityType?q=isAbstract==false&attrs=id,label,description&sort=label', {}).then(response => {
      commit(SET_ENTITY_TYPES, response.items)
    }, error => {
      console.log(error)
    })
  }
}
