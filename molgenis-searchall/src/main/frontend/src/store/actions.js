import { get } from '@molgenis/molgenis-api-client'

export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'

export default {
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
