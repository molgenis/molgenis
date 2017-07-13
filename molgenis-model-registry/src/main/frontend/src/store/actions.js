import api from '@molgenis/molgenis-api-client/dist/main.bundle'
import { SET_UMLDATA } from './mutations'

export const GET_UMLDATA = '__GET_UMLDATA__'

export default {
  /**
   *
   * Query parameters
   *
   * route = GET /api/v2/{entity_name}/{id}
   * attrs = entityTypes(attributes(id,type,name,label,refEntityType,isNullable,isIdAttribute,isCascadeDelete),id,label,package)
   * id = sys_job
   *
   */
  [GET_UMLDATA] ({commit}) {
    api.get('/api/v2/sys_md_Package/eu_bbmri_eric?attrs=entityTypes(attributes(id%2Ctype%2Cname%2Clabel%2CrefEntityType%2CisNullable%2CisIdAttribute)%2Cid%2CisAbstract%2Cextends%2Clabel%2Cpackage)')
      .then(response => {
        console.log(response)
        commit(SET_UMLDATA, response)
      }, error => {
        console.log(error)
      })
  }
}
