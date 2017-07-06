import { get } from '@molgenis/molgenis-api-client'

export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'

export default {
  /**
   * Example action for retrieving all EntityTypes from the server
   */
  [GET_ENTITY_TYPES] ({commit}) {
    get({apiUrl: '/api'}, '/v2/sys_md_EntityTypes?num=1000')
      .then(response => {
        console.log(response)
      }, error => {
        console.log(error)
      })
  }
}
