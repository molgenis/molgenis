// @flow
// $FlowFixMe
import api from '@molgenis/molgenis-api-client'
import { SET_ERROR, SET_FORM_DATA, SET_FORM_FIELDS, SET_MESSAGE, SET_SETTINGS } from './mutations'
import { EntityToStateMapper } from '@molgenis/molgenis-ui-form'

export const GET_SETTINGS = '__GET_SETTINGS__'
export const GET_SETTINGS_BY_ID = '__GET_SETTINGS_BY_ID__'
export const UPDATE_SETTINGS = '__UPDATE_SETTINGS__'

export default {
  [GET_SETTINGS] ({commit}: { commit: Function }) {
    const uri = 'api/v2/sys_md_EntityType?q=extends==sys_set_settings'
    api.get(uri).then(response => {
      commit(SET_SETTINGS, response.items.map(item => { return {id: item.id, label: item.label} }))
    }, error => {
      commit(SET_ERROR, error)
    })
  },
  [GET_SETTINGS_BY_ID] ({commit}: { commit: Function }, selectedEntity: String) {
    if (selectedEntity) {
      const uri = '/api/v2/' + selectedEntity
      api.get(uri).then(response => {
        const formFields = EntityToStateMapper.generateFormFields(response.meta)
        commit(SET_FORM_FIELDS, formFields)
        commit(SET_FORM_DATA, EntityToStateMapper.generateFormData(formFields, response.items[0]))
      }, error => {
        commit(SET_ERROR, error)
      })
    }
  },
  [UPDATE_SETTINGS] ({commit, state}: { commit: Function }, selectedEntity: String) {
    if (selectedEntity) {
      const options = {
        body: JSON.stringify(state.formData)
      }
      const uri = '/api/v1/' + selectedEntity + '/' + state.formData.id + '?_method=PUT'
      api.post(uri, options).then(response => {
        commit(SET_MESSAGE, 'Settings are successfully saved')
      }, error => {
        commit(SET_ERROR, error)
      })
    }
  }
}
