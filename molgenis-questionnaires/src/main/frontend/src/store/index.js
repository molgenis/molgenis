import Vue from 'vue'
import Vuex from 'vuex'

import actions from './actions'
import getters from './getters'
import mutations from './mutations'

import type { QuestionnaireState } from '../flow.types'

const state: QuestionnaireState = {

  /**
   * All the compound fields of the questionnaire metadata as chapters
   */
  chapterFields: [],

  /**
   * Error string filled when something goes wrong
   */
  error: '',

  /**
   * The data for the entire form
   */
  formData: {},

  /**
   * Loading boolean used to spinner activity fetch state across the questionnaire
   */
  loading: true,

  /**
   * Option object to be used in the EntityToFormMapper
   */
  mapperOptions: {
    showNillableBooleanOption: false
  },

  /**
   * Boolean to block navigation and show reminders
   */
  navigationBlocked: false,
  /**
   * A "raw" questionnaire containing an API v2 EntityType with the filled in data
   */
  questionnaire: {},

  /**
   * List of questionnaires available to the current user
   */
  questionnaireList: [],

  /**
   * The ID of the row holding the questionnaire data for the current user
   */
  questionnaireRowId: '',

  /**
   * Auto save outstading calls counter
   */
  numberOfOutstandingCalls: 0,

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
