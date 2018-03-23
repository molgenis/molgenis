import QuestionnaireChapter from 'src/pages/QuestionnaireChapter'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {
    'questionnaire_overview_loading_text': 'loading',
    'questionnaire_back_to_start': 'back to start',
    'questionnaire_chapter_incomplete_message': 'you forgot something',
    'questionnaire_boolean_true': 'Yes',
    'questionnaire_boolean_false': 'No',
    'questionnaire_boolean_null': 'No idea'
  }
  return translations[key]
}

describe('QuestionnaireChapter component', () => {
  let actions
  let getters
  let localVue
  let mutations
  let store
  let state

  beforeEach(() => {
    td.reset()

    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    actions = {
      GET_QUESTIONNAIRE: td.function()
    }

    getters = {
      getChapterByIndex: () => () => ({id: 'chapter1'}),
      getTotalNumberOfChapters: () => 1
    }

    mutations = {
      UPDATE_FORM_STATUS: td.function(),
      SET_FORM_DATA: td.function(),
      SET_MAPPER_OPTIONS: td.function()
    }

    state = {
      chapterFields: [],
      error: '',
      formData: {field1: 'value'},
      loading: false,
      mapperOptions: {},
      navigationBlocked: false,
      questionnaireLabel: 'label'
    }

    store = new Vuex.Store({actions, getters, mutations, state})
  })

  const stubs = ['router-link', 'router-view']
  const mocks = {$t: $t}
  const propsData = {chapterId: 1, questionnaireId: 'test_quest'}

  it('should dispatch the [GET_QUESTIONNAIRE] when created', () => {
    shallow(QuestionnaireChapter, {propsData, store, mocks, stubs, localVue})
    td.verify(actions.GET_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))
  })

  it('should commit the [SET_MAPPER_OPTIONS] mutation when no boolean labels are present in the state', () => {
    shallow(QuestionnaireChapter, {propsData, store, mocks, stubs, localVue})
    const mapperOptions = {
      booleanLabels: {
        trueLabel: 'Yes',
        falseLabel: 'No',
        nillLabel: 'No idea'
      }
    }

    td.verify(mutations.SET_MAPPER_OPTIONS(state, mapperOptions))
  })

  it('should get the current chapter from the state by index', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, mocks, stubs, localVue})
    expect(wrapper.vm.currentChapter).to.deep.equal({id: 'chapter1'})
  })

  it('should compute navigation is not blocked', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, mocks, stubs, localVue})
    expect(wrapper.vm.navigationBlocked).to.equal(false)
  })

  it('should compute that navigation is blocked', () => {
    state.navigationBlocked = true
    store = new Vuex.Store({actions, getters, mutations, state})

    const wrapper = shallow(QuestionnaireChapter, {propsData, store, mocks, stubs, localVue})
    wrapper.setData({formState: {$error: {'key': 'value'}}})

    expect(wrapper.vm.navigationBlocked).to.equal(true)
  })

  it('should fetch loading status from the state', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, mocks, stubs, localVue})
    expect(wrapper.vm.loading).to.equal(false)
  })

  it('should fetch error message from the state', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, mocks, stubs, localVue})
    expect(wrapper.vm.error).to.equal('')
  })
})
