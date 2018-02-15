import Vue from 'vue'
import Vuex from 'vuex'

import api from '@molgenis/molgenis-api-client'
import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'

const getters = {
  getChapterByIndex: (state) => (index) => {
    return [state.chapterFields[(index - 1)]]
  },

  getTotalNumberOfChapters: state => {
    return state.chapterFields.length
  }
}

const state = {

  /**
   * List of questionnaires available to the current user
   */
  questionnaireList: [],

  /**
   * All the compound fields of the questionnaire metadata as chapters
   */
  chapterFields: [],

  /**
   * The data for the entire form
   */
  formData: {},

  /**
   * The label of the questionnaire
   */
  questionnaireLabel: '',

  /**
   * The description of the questionnaire
   */
  questionnaireDescription: ''
}

const mutations = {
  'SET_QUESTIONNAIRE_LIST' (state, questionnaireList) {
    state.questionnaireList = questionnaireList
  },

  'SET_CHAPTER_FIELDS' (state, chapterFields) {
    state.chapterFields = chapterFields
  },

  'SET_FORM_DATA' (state, formData) {
    state.formData = formData
  },

  'SET_QUESTIONNAIRE_LABEL' (state, questionnaireLabel) {
    state.questionnaireLabel = questionnaireLabel
  },

  'SET_QUESTIONNAIRE_DESCRIPTION' (state, questionnaireDescription) {
    state.questionnaireDescription = questionnaireDescription
  }
}

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

      const chapters = response.meta.attributes.filter(attribute => attribute.fieldType === 'COMPOUND')
      const data = response.items.length > 0 ? response.items[0] : {}
      const form = EntityToFormMapper.generateForm({attributes: chapters}, data)

      commit('SET_CHAPTER_FIELDS', form.formFields)
      commit('SET_FORM_DATA', form.formData)
    })
  },

  'AUTO_SAVE_QUESTIONNAIRE' ({commit, state}, formData) {
    const options = {
      body: JSON.stringify({
        entities: [formData]
      }),
      method: 'PUT'
    }

    const questionnaireId = state.route.params.questionnaireId
    api.post('/api/v2/' + questionnaireId, options).then(() => {
      commit('SET_FORM_DATA', formData)
    }).catch(error => {
      console.log(error)
    })
  },

  'SUBMIT_QUESTIONNAIRE' ({state}, formData) {
    console.log('submit', formData)
    // Trigger submit
//        this.formData.status = 'SUBMITTED'
//        this.formState.$submitted = true
//        this.formState._validate()
//
//        // Check if form is valid
//        if (this.formState.$valid) {
//          // Generate submit timestamp
//          this.formData.submitDate = moment().toISOString()
//          const options = {
//            body: JSON.stringify(this.formData)
//          }
//
//          // Submit to server and redirect to thank you page
//          api.post('/api/v1/' + this.questionnaire.name + '/' + this.questionnaireId + '?_method=PUT', options).then(() => {
//            this.$router.push({path: '/' + this.questionnaireId + '/thanks'})
//          }).catch(error => {
//            console.log('Something went wrong submitting the questionnaire', error)
//          })
//        } else {
//          this.formData.status = 'OPEN'
//          this.formState.$submitted = false
//        }
  }
}

Vue.use(Vuex)
export default new Vuex.Store({
  state,
  mutations,
  getters,
  actions,
  strict: true
})
