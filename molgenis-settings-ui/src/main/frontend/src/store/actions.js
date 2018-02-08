// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'

import { SET_ALERT, SET_FORM_DATA, SET_FORM_FIELDS } from './mutations'
import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'

export const GET_SETTINGS_BY_ID = '__GET_SETTINGS_BY_ID__'
export const UPDATE_SETTINGS = '__UPDATE_SETTINGS__'

const createAlert = (message, type) => {
  return {
    message: message,
    type: type
  }
}

export default {
  [GET_SETTINGS_BY_ID] ({commit}, selectedEntity: String) {
    if (selectedEntity) {
      const uri = '/api/v2/' + selectedEntity
      return api.get(uri).then(response => {
        commit(SET_FORM_FIELDS, null)
        commit(SET_FORM_DATA, null)

        const form = EntityToFormMapper.generateForm(response.meta, response.items[0])

        commit(SET_FORM_FIELDS, form.formFields)
        commit(SET_FORM_DATA, form.formData)
      }, error => {
        commit(SET_ALERT, createAlert(error, 'danger'))
      })
    }
  },
  [UPDATE_SETTINGS] ({commit, state}, selectedEntity: String) {
    if (selectedEntity) {
      const options = {
        body: JSON.stringify(state.formData)
      }
      const uri = '/api/v1/' + selectedEntity + '/' + state.formData.id + '?_method=PUT'
      api.post(uri, options).then(() => {
        commit(SET_ALERT, createAlert('Settings are successfully saved', 'success'))
      }, error => {
        commit(SET_ALERT, createAlert(error, 'danger'))
      })
    }
  }
}
