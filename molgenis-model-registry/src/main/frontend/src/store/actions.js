import api from '@molgenis/molgenis-api-client'
import {SET_UMLDATA, SET_ERROR} from './mutations'
import type {Package} from './utils/flow.types.js'

export const GET_UMLDATA = '__GET_UMLDATA__'

export default {
  /**
   *
   * Query parameters
   *
   * route = GET /api/v2/{entity_name}/{id}
   * attrs = {
   *    entityTypes(attributes(id,type,name,label,refEntityType(id,label,extends,isAbstract,package),isNullable,isIdAttribute,isCascadeDelete),id,isAbstract,extends(id,label,isAbstract,package),label,package),
   *    children(entityTypes(attributes(id,type,name,label,refEntityType(id,label,extends,isAbstract,package),isNullable,isIdAttribute,isCascadeDelete),id,isAbstract,extends(id,label,isAbstract,package),label,package))
   * }
   *
   *
   * id = sys_job
   *
   */
  [GET_UMLDATA] ({commit, state}) {
    let pkg: Package = {
      name: 'sys_md'
    }
    if (state.molgenisPackage !== 'undefined') pkg = state.molgenisPackage
    // paste this after the current action string
    // %2Cchildren(entityTypes(attributes(id%2Ctype%2Cname%2Clabel%2CrefEntityType(id%2Clabel%2Cextends%2CisAbstract%2Cpackage)%2CisNullable%2CisIdAttribute%2CisCascadeDelete)%2Cid%2CisAbstract%2Cextends(id%2Clabel%2CisAbstract%2Cpackage)%2Clabel%2Cpackage)Cpackage
    api.get(`/api/v2/sys_md_Package/${pkg.name}?attrs=entityTypes(attributes(id%2Ctype%2Cname%2Clabel%2CrefEntityType(id%2Clabel%2Cextends%2CisAbstract%2Cpackage)%2CisNullable%2CisIdAttribute%2CisCascadeDelete)%2Cid%2CisAbstract%2Cextends(id%2Clabel%2CisAbstract%2Cpackage)%2Clabel%2Cpackage))`)
      .then(umlData => {
        commit(SET_UMLDATA, umlData)
      }, error => {
        commit(SET_ERROR, '[ ' + error.status + ' ] ' + error.statusText)
      })
  }
}
