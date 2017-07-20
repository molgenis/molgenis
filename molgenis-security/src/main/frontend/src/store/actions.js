import { get } from '@molgenis/molgenis-api-client'
import { SET_ENTITY_TYPES, SET_FILTER, SET_ROWS, SET_SIDS } from './mutations'
import { debounce } from 'lodash'

export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const GET_SIDS = '__GET_SIDS__'
export const INITIALIZED = '__INITIALIZED__'
export const ENTITY_SELECTED = '__ENTITY_SELECTED__'
export const FILTER_CHANGED = '__FILTER_CHANGED__'
export const GET_ACLS = '__GET_ACLS__'

export default {
  [INITIALIZED] ({dispatch}) {
    dispatch(GET_SIDS)
    dispatch(GET_ENTITY_TYPES)
  },
  [GET_SIDS] ({commit}) {
    get('/plugin/permissionmanager/sid', {}).then(response => {
      commit(SET_SIDS, response)
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
  [ENTITY_SELECTED] ({dispatch}) {
    dispatch(GET_ACLS)
  },
  [GET_ACLS] ({state: {selectedEntityTypeId, filter}, commit}) {
    let url = `/permission/acls?entityTypeId=${selectedEntityTypeId}&pageSize=10`
    if (filter) {
      url += `&filter=${encodeURIComponent(filter)}`
    }
    get(url, {}).then(response => {
      commit(SET_ROWS, response)
    }, error => {
      console.log(error)
    })
  },
  /**
   * Example action for retrieving all EntityTypes from the server
   */
  [GET_ENTITY_TYPES] ({commit}) {
    /**
     * Pass options to the fetch like body, method, x-molgenis-token etc...
     * @type {{}}
     */
    get('/api/v2/sys_md_EntityType?q=isEntityLevelSecurity==true&attrs=id,label,description', {}).then(response => {
      commit(SET_ENTITY_TYPES, response.items)
    }, error => {
      console.log(error)
    })
  }
}
