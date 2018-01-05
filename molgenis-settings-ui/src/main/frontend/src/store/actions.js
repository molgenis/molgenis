// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { SET_ERROR, SET_FORM_DATA, SET_FORM_FIELDS } from './mutations'
import { EntityToStateMapper } from '@molgenis/molgenis-ui-form'

export const GET_SETTINGS = '__GET_SETTINGS__'

export default {
  [GET_SETTINGS] ({commit}: { commit: Function }) {
    const uri = '/api/v2/sys_set_app'
    try {
      api.get(uri).then(response => {
        // TODO: We don't have compounds yet, so we have this workaround for now
        response.meta.attributes = response.meta.attributes.filter(attribute => attribute.fieldType !== 'COMPOUND')
        const formFields = EntityToStateMapper.generateFormFields(response.meta)
        commit(SET_FORM_FIELDS, formFields)
        commit(SET_FORM_DATA, EntityToStateMapper.generateFormData(formFields, response.items[0]))
      }, error => {
        commit(SET_ERROR, error)
      })
    } catch (error) {
      commit(SET_ERROR, error.message)
    }
  }
}
