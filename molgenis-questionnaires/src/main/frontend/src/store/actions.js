import api from '@molgenis/molgenis-api-client'
import moment from 'moment'

import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'

const actions = {
  'GET_QUESTIONNAIRE_LIST' ({commit}) {
    return api.get('/menu/plugins/questionnaires/meta/list').then(response => {
      commit('SET_QUESTIONNAIRE_LIST', response)
    })
  },

  'GET_QUESTIONNAIRE' ({commit}, questionnaireId) {
    return api.get('/api/v2/' + questionnaireId).then(response => {
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

  'GET_RAW_QUESTIONNAIRE' ({commit}, questionnaireId) {
    return api.get('/api/v2/' + questionnaireId)
  },

  'GET_SUBMISSION_TEXT' ({commit}, questionnaireId) {
    api.get('/menu/plugins/questionnaires/' + questionnaireId + '/thanks').then(response => {
      commit('SET_SUBMISSION_TEXT', response)
    }).catch(error => {
      console.log('Something went wrong fetching the questionnaire submission text', error)
    })
  },

  'AUTO_SAVE_QUESTIONNAIRE' ({commit, state}, formData) {
    commit('SET_FORM_DATA', formData)
    const options = {
      body: JSON.stringify({
        entities: [
          state.formData
        ]
      }),
      method: 'PUT'
    }

    const questionnaireId = state.route.params.questionnaireId
    return api.post('/api/v2/' + questionnaireId, options).catch(error => {
      console.log(error)
    })
  },

  'SUBMIT_QUESTIONNAIRE' ({state}, formData) {
    // Generate submit timestamp
    formData.submitDate = moment().toISOString()
    formData.status = 'SUBMITTED'

    const options = {
      body: JSON.stringify({
        entities: [
          formData
        ]
      }),
      method: 'PUT'
    }

    const questionnaireId = state.route.params.questionnaireId
    api.post('/api/v2/' + questionnaireId, options).catch(error => {
      console.log('Something went wrong submitting the questionnaire', error)
    })
  }
}

export default actions
