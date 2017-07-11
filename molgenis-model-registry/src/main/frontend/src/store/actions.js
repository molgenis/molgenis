import api from '@molgenis/molgenis-api-client/dist/main.bundle'

export const GET_ENTITY_TYPES = '__GET_ENTITY_TYPES__'

export default {
  /**
   * Example action for retrieving all EntityTypes from the server
   */
  [GET_ENTITY_TYPES] ({commit}) {
    api.get('/api/v2/sys_md_EntityTypes?num=1000')
      .then(response => {
        console.log(response)
      }, error => {
        console.log(error)
      })
  }
}
