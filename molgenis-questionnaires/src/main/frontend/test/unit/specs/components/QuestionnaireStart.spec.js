import QuestionnaireStart from 'src/pages/QuestionnaireStart'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'
import VueRouter from 'vue-router'

const $t = (key) => {
  const translations = {
    'questionnaire_loading_text': 'loading',
    'questionnaire_start': 'start',
    'questionnaire_back_to_questionnaire_list': 'back',
    'questionnaire_boolean_true': 'Yes',
    'questionnaire_boolean_false': 'No',
    'questionnaire_boolean_null': 'No idea'
  }
  return translations[key]
}

describe('QuestionnaireStart component', () => {
  let actions
  let getters
  let localVue
  let mutations
  let state
  let store

  beforeEach(() => {
    td.reset()

    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    getters = {
      getQuestionnaireLabel: () => 'label',
      getQuestionnaireDescription: () => 'description'
    }

    state = {
      mapperOptions: {},
      error: 'error',
      loading: true
    }

    actions = {
      GET_QUESTIONNAIRE: td.function(),
      START_QUESTIONNAIRE: td.function()
    }

    mutations = {
      SET_MAPPER_OPTIONS: td.function()
    }

    store = new Vuex.Store({state, actions, mutations, getters})
  })

  const stubs = ['router-link', 'router-view']
  const mocks = {$t: $t}
  const propsData = {questionnaireId: 'test_quest'}

  it('should dispatch the [GET_QUESTIONNAIRE] action on created', () => {
    shallow(QuestionnaireStart, {propsData, store, stubs, localVue, mocks})
    td.verify(actions.GET_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))
  })

  it('should dispatch a mutation to set mapperOptions', () => {
    shallow(QuestionnaireStart, {propsData, store, stubs, localVue, mocks})
    td.verify(mutations.SET_MAPPER_OPTIONS(state, td.matchers.anything()))
  })

  it('should not dispatch a mutation to set mapperOptions if already set', () => {
    state.mapperOptions = {booleanLabels: 'labels'}
    store = new Vuex.Store({state, actions, mutations, getters})

    shallow(QuestionnaireStart, {propsData, store, stubs, localVue, mocks})
    td.verify(mutations.SET_MAPPER_OPTIONS(state, td.matchers.anything()), {times: 0})
  })

  it('should retrieve the description from the store', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue, mocks})
    expect(wrapper.vm.questionnaireDescription).to.equal('description')
  })

  it('should retrieve the label from the store', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue, mocks})
    expect(wrapper.vm.questionnaireLabel).to.equal('label')
  })

  it('should return error from the store', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue, mocks})
    expect(wrapper.vm.error).to.equal('error')
  })

  it('should return loading from the store', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue, mocks})
    expect(wrapper.vm.loading).to.equal(true)
  })

  it('should dispatch the [START_QESTIONNAIRE] action when startQuestionnaire method is called', () => {
    const router = new VueRouter()

    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, router, localVue, mocks})
    wrapper.vm.startQuestionnaire()
    td.verify(actions.START_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))
  })
})
