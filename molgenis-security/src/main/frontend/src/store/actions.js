import { get } from '@molgenis/molgenis-api-client'
import { debounce } from 'lodash'

import { SET_ACLS, SET_FILTER } from './mutations'

export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const ENTITY_SELECTED = '__ENTITY_SELECTED__'
export const FILTER_CHANGED = '__FILTER_CHANGED__'
export const GET_ACLS = '__GET_ACLS__'

export default {
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
      commit(SET_ACLS, response)
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
    const options = {}
    get('/api/v2/sys_md_EntityTypes?num=1000', options).then(response => {
      console.log(response)
    }, error => {
      console.log(error)
    })
  }
}
