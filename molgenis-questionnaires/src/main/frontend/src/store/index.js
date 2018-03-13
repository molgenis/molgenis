import Vue from 'vue'
import Vuex from 'vuex'

import actions from './actions'
import getters from './getters'
import mutations from './mutations'

import type { QuestionnaireState } from '../flow.types'

const state: QuestionnaireState = {

  /**
   * A "raw" questionnaire containing an API v2 EntityType with the filled in data
   */
  questionnaire: {},

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
