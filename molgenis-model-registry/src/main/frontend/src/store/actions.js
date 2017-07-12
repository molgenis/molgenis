import api from '@molgenis/molgenis-api-client/dist/main.bundle'
import { SET_RAWDATA } from './mutations'

export const GET_UMLDATA = '__GET_UMLDATA__'

export default {
  /**
   * Example action for retrieving all EntityTypes from the server
   */
  [GET_UMLDATA] ({commit}) {
    api.get('/api/v2/sys_md_Package?attrs=entityTypes(attributes(id%2Cname%2Ctype%2Clabel%2CrefEntityType)%2Cid%2Clabel%2Cpackage)&q=id%3D%3Dsys&num=10000')
      .then(response => {
        console.log(response)
        commit(SET_RAWDATA, response.items[0])
      }, error => {
        console.log(error)
      })
  }
}
