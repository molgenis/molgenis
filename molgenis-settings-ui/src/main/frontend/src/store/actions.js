// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { SET_ERROR, SET_FORM_FIELDS } from './mutations'
import { EntityToStateMapper } from '@molgenis/molgenis-ui-form'

export const GET_SETTINGS = '__GET_SETTINGS__'

export default {
  [GET_SETTINGS] ({commit}: { commit: Function }) {
    const uri = '/api/v2/sys_set_app?start=0&num=0'
    try {
      api.get(uri).then(response => {
        commit(SET_FORM_FIELDS, EntityToStateMapper.generateFormFields(response.meta))
      }, error => {
        commit(SET_ERROR, error)
      })
    } catch (error) {
      commit(SET_ERROR, error.message)
    }
  }
}
