// @flow
import api from '@molgenis/molgenis-api-client'
import type { VuexContext, QuestionnaireType } from '../flow.types.js'
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
    return api.get('/menu/plugins/questionnaires/list').then((response:QuestionnaireType) => {
      commit('SET_QUESTIONNAIRE_LIST', response)
      commit('SET_LOADING', false)
    }, error => {
      handleError(commit, error)
    })
  },

  'START_QUESTIONNAIRE' ({commit, dispatch}: VuexContext, questionnaireId: string) : Promise<any> {
    return new Promise((resolve, reject) => {
      cleanScreen(commit)
      api.get(`/menu/plugins/questionnaires/start/${questionnaireId}`).then(response => {
        commit('SET_QUESTIONNAIRE_ROW_ID', response.id)
        dispatch('GET_QUESTIONNAIRE', questionnaireId)
        resolve(response.id)
      }, error => {
        handleError(commit, error)
        reject(error)
      })
    })
  },

  'GET_QUESTIONNAIRE' ({state, getters, commit}: VuexContext, questionnaireId: string): any {
    cleanScreen(commit)
    return new Promise((resolve, reject) => {
      const currentQuestionnaireId = getters.getQuestionnaireId
      const encodedQuestionnaireId = encodeURIComponent(questionnaireId)
      const encodedUserName = encodeURIComponent(state.username)

      if (currentQuestionnaireId !== questionnaireId) {
        const getUrl = `/api/v2/${encodedQuestionnaireId}?includeCategories=true&q=owner==${encodedUserName}`
        return api.get(getUrl).then(response => {
          commit('SET_QUESTIONNAIRE', response)
          const data = response.items[0]
          const form = EntityToFormMapper.generateForm(response.meta, data, state.mapperOptions)
          commit('SET_FORM_DATA', form.formData)
          const chapters = form.formFields.filter(field => field.type === 'field-group')
          commit('SET_CHAPTER_FIELDS', chapters)
          // Set state to submitted to have the form validate required fields
          commit('UPDATE_FORM_STATUS', 'SUBMITTED')
          commit('SET_LOADING', false)
          resolve()
        }, error => {
          handleError(commit, error)
          reject(error)
        })
      }

      commit('SET_LOADING', false)
    })
  },

  'GET_QUESTIONNAIRE_OVERVIEW' ({commit}: VuexContext, questionnaireId: string) {
    cleanScreen(commit)
    const lng = this._vm.$lng || this._vm.fallbackLng
    return api.get(`/api/v2/${questionnaireId}`).then(response => {
      commit('SET_QUESTIONNAIRE', response)

      if (response.items[0].report_header) {
        const reportDataEndPoint = response.items[0].report_header._href

        api.get(reportDataEndPoint).then(reportDataResponse => {
          const reportData = {
            logoDataUrl: reportDataResponse.logo,
            introText: reportDataResponse['intro-' + lng]
          }
          commit('SET_QUESTIONNAIRE_REPORT_HEADER', reportData)
          commit('SET_LOADING', false)
        }, error => {
          handleError(commit, error)
        })
      } else {
        commit('SET_LOADING', false)
      }
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

  'AUTO_SAVE_QUESTIONNAIRE' ({commit, state, dispatch}: VuexContext, payload: Object) {
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
        const options = {
          body: JSON.stringify(updatedValue),
          method: 'PUT'
        }
        commit('INCREMENT_SAVING_QUEUE')
        const encodedTableId = encodeURIComponent(state.questionnaire.meta.name)
        const encodedRowId = encodeURIComponent(state.questionnaireRowId)
        const encodedColumnId = encodeURIComponent(updatedAttribute)
        api.post(`/api/v1/${encodedTableId}/${encodedRowId}/${encodedColumnId}`, options).catch((error) => {
          handleError(commit, error)
        }).then(() => {
          commit('DECREMENT_SAVING_QUEUE')
        })
      } else {
        commit('UPDATE_FORM_STATUS', 'SUBMITTED')
      }
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
