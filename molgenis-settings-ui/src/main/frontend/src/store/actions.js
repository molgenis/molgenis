// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { SET_ERROR, SET_RAW_SETTINGS } from './mutations'

export const GET_SETTINGS = '__GET_SETTINGS__'
export const UPDATE_SETTINGS = '__UPDATE_SETTINGS__'

export default {
  [GET_SETTINGS] ({commit}: { commit: Function }) {
    let uri = '/api/v2/sys_set_app?start=0&num=0'
    try {
      api.get(uri).then(response => {
        commit(SET_RAW_SETTINGS, response)
      }, error => {
        commit(SET_ERROR, error)
      })
    } catch (error) {
      commit(SET_ERROR, error.message)
    }
  }
}
