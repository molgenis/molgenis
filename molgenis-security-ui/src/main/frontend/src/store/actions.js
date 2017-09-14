import {delete_, get, post} from '@molgenis/molgenis-api-client'
import {
  CANCEL_EDIT_ROLE,
  SET_ACL,
  SET_ENTITY_TYPES,
  SET_FILTER,
  SET_GROUPS,
  SET_GROUPS_IN_ROLE,
  SET_ROLES,
  SET_ROWS,
  SET_SELECTED_SID,
  SET_USERS,
  SET_USERS_IN_ROLE
} from './mutations'
import {debounce} from 'lodash'

export const SELECT_ROLE = '__SELECT_ROLE__'
export const SELECT_USER = '__SELECT_USER__'
export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const GET_ROLES = '__GET_ROLES__'
export const GET_USERS = '__GET_USERS__'
export const GET_GROUPS = '__GET_GROUPS__'
export const INITIALIZED = '__INITIALIZED__'
export const FILTER_CHANGED = '__FILTER_CHANGED__'
export const GET_ACLS = '__GET_ACLS__'
export const SAVE_ACL = '__SAVE_ACL__'
export const SAVE_ROLE = '__SAVE_ROLE__'
export const DELETE_ROLE = '__DELETE_ROLE__'
export const GET_ACL = '__GET_ACL__'

export default {
  [INITIALIZED] ({dispatch}) {
    dispatch(GET_ROLES).then(items => dispatch(SELECT_ROLE, items[0].id))
    dispatch(GET_USERS)
    dispatch(GET_GROUPS)
    dispatch(GET_ENTITY_TYPES)
  },
  [GET_ROLES] ({commit}) {
    return get('/api/v2/sys_sec_Role?sort=label', {}).then(response => {
      commit(SET_ROLES, response.items)
      return response.items
    }, error => {
      console.log(error)
    })
  },
  [GET_GROUPS] ({commit}) {
    get('/api/v2/sys_sec_Group?sort=name', {}).then(response => {
      commit(SET_GROUPS, response.items)
    }, error => {
      console.log(error)
    })
  },
  [GET_USERS] ({commit, dispatch}) {
    return get('/api/v2/sys_sec_User?sort=username', {}).then(response => {
      commit(SET_USERS, response.items)
      return response.items
    }, error => {
      console.log(error)
    })
  },
  [SELECT_USER] ({commit}, sid) {
    commit(SET_SELECTED_SID, sid)
  },
  [SELECT_ROLE] ({commit, getters}, sid) {
    commit(SET_SELECTED_SID, sid)
    if (!getters.role.groups) {
      get(`/api/v2/sys_sec_GroupAuthority?q=role==${sid}&sort=Group`, {}).then(response => {
        commit(SET_GROUPS_IN_ROLE, response.items.map(e => e.Group.id))
      }, error => {
        console.log(error)
      })
    }
    if (!getters.role.users) {
      get(`/api/v2/sys_sec_UserAuthority?q=role==${sid}&sort=User`, {}).then(response => {
        commit(SET_USERS_IN_ROLE, response.items.map(e => e.User.username))
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
  [SAVE_ROLE] ({getters, commit, dispatch}, role) {
    const id = getters.role && getters.role.id
    post('/api/v2/sys_sec_Role', {
      body: JSON.stringify({entities: [{...role, id}]}),
      headers: {
        'Content-Type': 'application/json'
      },
      method: id ? 'put' : 'post'
    }).then(() => {
      commit(CANCEL_EDIT_ROLE)
      dispatch(GET_ROLES)
    }, error => {
      console.log(error)
    })
  },
  [DELETE_ROLE] ({state, dispatch, commit}) {
    delete_('/api/v2/sys_sec_Role/' + state.selectedSid).then(() => {
      commit(SET_SELECTED_SID, undefined)
      dispatch(GET_ROLES)
    }, error => {
      console.log(error)
    })
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
  [SAVE_ACL] (context, acl) {
    console.log('save', JSON.stringify(acl))
    post('/permission/acl', {body: JSON.stringify(acl), method: 'put'})
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
