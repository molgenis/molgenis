import Vue from 'vue'
import Vuex from 'vuex'

import actions from './actions'
import getters from './getters'
import mutations from './mutations'

const state = {

  /**
   * A "raw" questionnaire containing an API v2 EntityType with the filled in data
   */
  questionnaireOverview: {},

  /**
   * List of questionnaires available to the current user
   */
  questionnaireList: [],

  /**
   * The ID of the currently selected questionnaire
   * This is also recorded in the URL, but if we store it in the state we can use it to
   * check if we should clear the current state because a new questionnaire is selected
   */
  questionnaireId: '',

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
  submissionText: '',

  /**
   * Option object to be used in the EntityToFormMapper
   */
  mapperOptions: {},

  /**
   * Loading boolean used to spinner activity fetch state across the questionnaire
   */
  loading: true,

  /**
   * Error string filled when something goes wrong
   */
  error: ''
}

Vue.use(Vuex)
export default new Vuex.Store({
  actions,
  getters,
  mutations,
  state,
  strict: true
})
