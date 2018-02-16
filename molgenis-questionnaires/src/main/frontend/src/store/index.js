import Vue from 'vue'
import Vuex from 'vuex'

import actions from './actions'
import getters from './getters'
import mutations from './mutations'

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
  questionnaireDescription: '',

  /**
   * The ID of the row holding the questionnaire data for the current user
   */
  questionnaireRowId: '',

  /**
   * Submission text shown after completing a questionnaire
   */
  submissionText: ''
}

Vue.use(Vuex)
export default new Vuex.Store({
  actions,
  getters,
  mutations,
  state,
  strict: true
})
