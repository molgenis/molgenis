import { get } from '@molgenis/molgenis-api-client'
import { SET_ENTITY_TYPES } from './mutations'

export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'
export const INITIALIZED = '__INITIALIZED__'

export default {
  [INITIALIZED] ({dispatch}) {
    dispatch(GET_ENTITY_TYPES)
  },
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
