// @flow
import api from '@molgenis/molgenis-api-client'
import type { VuexContext } from '../flow.types.js'
// $FlowFixMe
import Vue from 'vue'
import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'

const handleError = (commit: Function, error: Error) => {
  commit('SET_ERROR', error)
  commit('SET_LOADING', false)
}

const cleanScreen = (commit: Function) => {
  commit('SET_ERROR', '')
  commit('SET_LOADING', true)
}

const actions = {
  'GET_QUESTIONNAIRE_LIST' ({commit}: VuexContext) {
    cleanScreen(commit)
    return api.get('/menu/plugins/questionnaires/list').then(response => {
      commit('SET_QUESTIONNAIRE_LIST', response)
      commit('SET_LOADING', false)
    }, error => {
      handleError(commit, error)
    })
  },

  'START_QUESTIONNAIRE' ({commit}: VuexContext, questionnaireId: string) : Promise<any> {
    return new Promise((resolve, reject) => {
      cleanScreen(commit)
      api.get(`/menu/plugins/questionnaires/start/${questionnaireId}`).then(response => {
        commit('SET_QUESTIONNAIRE_ROW_ID', response.id)
        commit('SET_LOADING', false)
        resolve(response.id)
      }, error => {
        handleError(commit, error)
        reject(error)
      })
    })
  },

  'GET_QUESTIONNAIRE' ({state, getters, commit}: VuexContext, questionnaireId: string) {
    cleanScreen(commit)

    const currentQuestionnaireId = getters.getQuestionnaireId
    if (currentQuestionnaireId !== questionnaireId) {
      return api.get(`/api/v2/${questionnaireId}?includeCategories=true`).then(response => {
        commit('SET_QUESTIONNAIRE', response)

        const data = response.items.length > 0 ? response.items[0] : {}
        commit('SET_QUESTIONNAIRE_ROW_ID', data[response.meta.idAttribute])

        const form = EntityToFormMapper.generateForm(response.meta, data, state.mapperOptions)
        commit('SET_FORM_DATA', form.formData)

        const chapters = form.formFields.filter(field => field.type === 'field-group')
        commit('SET_CHAPTER_FIELDS', chapters)
        // Set state to submitted to have the form validate required fields
        commit('UPDATE_FORM_STATUS', 'SUBMITTED')

        commit('SET_LOADING', false)
      }, error => {
        handleError(commit, error)
      })
    }
    commit('SET_LOADING', false)
  },

  'GET_QUESTIONNAIRE_OVERVIEW' ({commit}: VuexContext, questionnaireId: string) {
    cleanScreen(commit)
    return api.get(`/api/v2/${questionnaireId}`).then(response => {
      commit('SET_QUESTIONNAIRE', response)
      commit('SET_LOADING', false)
    }, error => {
      handleError(commit, error)
    })
  },

  'GET_SUBMISSION_TEXT' ({commit}: VuexContext, questionnaireId: string) {
    cleanScreen(commit)
    return api.get('/menu/plugins/questionnaires/submission-text/' + encodeURIComponent(questionnaireId)).then(response => {
      commit('SET_SUBMISSION_TEXT', response)
      commit('SET_LOADING', false)
    }, error => {
      handleError(commit, error)
    })
  },

  'VALIDATE_FIELD' ({commit, state, dispatch}: VuexContext, payload: Object) {
    const {formData, formState} = payload

    const updatedAttribute = Object.keys(formData).find(key => formData[key] !== state.formData[key]) || ''
    commit('SET_FORM_DATA', formData)
    // Set state to open allow required fields to be empty on auto-save
    commit('UPDATE_FORM_STATUS', 'OPEN')

    Vue.nextTick(() => {
      const fieldState = formState[updatedAttribute]
      const updatedValue = formData[updatedAttribute]
      if (fieldState && fieldState.$valid) {
        // Set state to submitted to have the form validate required fields
        commit('UPDATE_FORM_STATUS', 'SUBMITTED')
        dispatch('AUTO_SAVE_QUESTIONNAIRE', {updatedAttribute, updatedValue})
      }
    })
  },

  'AUTO_SAVE_QUESTIONNAIRE' ({commit, state}: VuexContext, payload: Object) {
    const options = {
      body: JSON.stringify(payload.updatedValue),
      method: 'PUT'
    }

    commit('INCREMENT_SAVING_QUEUE')

    const encodedTableId = encodeURIComponent(state.questionnaire.meta.name)
    const encodedRowId = encodeURIComponent(state.questionnaireRowId)
    const encodedColumnId = encodeURIComponent(payload.updatedAttribute)
    return api.post(`/api/v1/${encodedTableId}/${encodedRowId}/${encodedColumnId}`, options).catch((error) => {
      handleError(commit, error)
    }).then(() => {
      commit('DECREMENT_SAVING_QUEUE')
    })
  },

  'SUBMIT_QUESTIONNAIRE' ({commit, state}: VuexContext, submitDate: string) {
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

    return api.post('/api/v2/' + encodeURIComponent(state.questionnaire.meta.name), options)
      .then()
      .catch(error => handleError(commit, error))
  }
}

export default actions
