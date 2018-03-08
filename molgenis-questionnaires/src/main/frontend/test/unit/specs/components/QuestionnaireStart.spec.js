import QuestionnaireStart from 'src/pages/QuestionnaireStart'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'
import { generateError } from '../../utils'

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

describe('QuestionnaireStart component', function () {
  const spec = this.title

  let actions
  let localVue
  let mutations
  let state
  let store

  beforeEach(() => {
    td.reset()

    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    state = {
      questionnaireDescription: 'description',
      questionnaireLabel: 'label',
      chapterFields: [],
      mapperOptions: {booleanLabels: 'labels'}
    }

    actions = {
      START_QUESTIONNAIRE: td.function(),
      GET_QUESTIONNAIRE: td.function()
    }

    mutations = {
      SET_MAPPER_OPTIONS: td.function()
    }

    store = new Vuex.Store({state, actions, mutations})
  })

  const stubs = ['router-link', 'router-view']
  const mocks = {$t: $t}
  const propsData = {questionnaireId: 'test_quest'}

  it('should dispatch action [START_QUESTIONNAIRE] to start a questionnaire when created', () => {
    shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    td.verify(actions.START_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))
  })

  it('should dispatch a mutation to set mapperOptions', function (done) {
    const test = this.test.title

    state.mapperOptions = {}
    store = new Vuex.Store({state, actions, mutations})

    const wrapper = shallow(QuestionnaireStart, {propsData, store, mocks, stubs, localVue})
    wrapper.vm.$nextTick().then(() => {
      td.verify(mutations.SET_MAPPER_OPTIONS(state, td.matchers.anything()))
      done()
    }).catch(error => done(generateError(error, spec, test)))
  })

  it('should not dispatch a mutation to set mapperOptions if already set', function (done) {
    const test = this.test.title

    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick().then(() => {
      td.verify(mutations.SET_MAPPER_OPTIONS(state, td.matchers.anything()), {times: 0})
      done()
    }).catch(error => done(generateError(error, spec, test)))
  })

  it('should dispatch action [GET_QUESTIONNAIRE] to fetch a questionnaire when created and no chapters are present', function (done) {
    const test = this.test.title

    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick().then(() => {
      wrapper.vm.$nextTick().then(() => {
        td.verify(actions.GET_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))
        done()
      }).catch(error => done(generateError(error, spec, test)))
    })
  })

  it('should not dispatch action [GET_QUESTIONNAIRE] to fetch a questionnaire when there are chapters present', () => {
    state.chapterFields = ['chapter1']
    store = new Vuex.Store({state, actions})

    shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    td.verify(actions.GET_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined), {times: 0})
  })

  it('should retrieve the description from the store', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    expect(wrapper.vm.questionnaireDescription).to.equal('description')
  })

  it('should retrieve the label from the store', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    expect(wrapper.vm.questionnaireLabel).to.equal('label')
  })
})

