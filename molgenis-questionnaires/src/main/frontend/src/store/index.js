import Vue from 'vue'
import Vuex from 'vuex'

import api from '@molgenis/molgenis-api-client'
import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'

const state = {
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

const getters = {
  getChapterByIndex: (state) => (index) => {
    return [state.chapterFields[(index - 1)]]
  },
  getTotalNumberOfChapters: state => {
    return state.chapterFields.length
  }
}

const actions = {
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
