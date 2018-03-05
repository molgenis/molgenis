import api from '@molgenis/molgenis-api-client'

import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'

const actions = {
  'GET_QUESTIONNAIRE_LIST' ({commit}) {
    return api.get('/menu/plugins/questionnaires/list').then(response => {
      commit('SET_QUESTIONNAIRE_LIST', response)
    })
  },

  'START_QUESTIONNAIRE' ({commit, state}, questionnaireId) {
    if (state.questionnaireId !== questionnaireId) {
      commit('CLEAR_STATE')
    }

    api.get('/menu/plugins/questionnaires/start/' + questionnaireId)
  },

  'GET_QUESTIONNAIRE' ({commit}, questionnaireId) {
    return api.get('/api/v2/' + questionnaireId).then(response => {
      commit('SET_QUESTIONNAIRE_ID', questionnaireId)
      commit('SET_QUESTIONNAIRE_LABEL', response.meta.label)
      commit('SET_QUESTIONNAIRE_DESCRIPTION', response.meta.description)

      const data = response.items.length > 0 ? response.items[0] : {}
      const form = EntityToFormMapper.generateForm(response.meta, data)
      const chapters = form.formFields.filter(field => field.type === 'field-group')

      commit('SET_CHAPTER_FIELDS', chapters)
      commit('SET_QUESTIONNAIRE_ROW_ID', data[response.meta.idAttribute])
      commit('SET_FORM_DATA', form.formData)
    })
  },

  /* eslint-disable no-empty-pattern */
  'GET_QUESTIONNAIRE_OVERVIEW' ({}, questionnaireId) {
    return api.get('/api/v2/' + questionnaireId)
  },

  'GET_SUBMISSION_TEXT' ({commit}, questionnaireId) {
    return api.get('/menu/plugins/questionnaires/' + questionnaireId + '/thanks').then(response => {
      commit('SET_SUBMISSION_TEXT', response)
    })
  },

  'AUTO_SAVE_QUESTIONNAIRE' ({state}, updatedAttribute) {
    const options = {
      body: JSON.stringify(updatedAttribute.value),
      method: 'PUT'
    }

    const questionnaireId = state.route.params.questionnaireId
    const uri = '/api/v1/' + questionnaireId + '/' + state.questionnaireRowId + '/' + updatedAttribute.attribute

    return api.post(uri, options)
  },

  'SUBMIT_QUESTIONNAIRE' ({state}, submitDate) {
    const submitData = Object.assign({}, state.formData)
    submitData.submitDate = submitDate

    const options = {
      body: JSON.stringify({
        entities: [
          submitData
        ]
      }),
      method: 'PUT'
    }

    const questionnaireId = state.route.params.questionnaireId
    return api.post('/api/v2/' + questionnaireId, options)
  }
}

export default actions
