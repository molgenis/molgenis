import QuestionnaireChapter from 'src/components/QuestionnaireChapter'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'
import VueRouter from 'vue-router'
import { generateError } from '../../utils'

const $t = (key) => {
  const translations = {
    'questionnaire_loading_chapter_text': 'loading',
    'questionnaire_previous_chapter': 'previous',
    'questionnaire_back_to_start': 'back to start',
    'questionnaire_next_chapter': 'next',
    'questionnaire_submit': 'submit',
    'chapter_incomplete_message': 'you forgot something',
    'questionnaire_boolean_true': 'Yes',
    'questionnaire_boolean_false': 'No',
    'questionnaire_boolean_null': 'No idea'
  }
  return translations[key]
}

describe('QuestionnaireChapter component', function () {
  const spec = this.title

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
      AUTO_SAVE_QUESTIONNAIRE: td.function(),
      GET_QUESTIONNAIRE: td.function(),
      SUBMIT_QUESTIONNAIRE: td.function()
    }

    const chapterFields = [{
      id: 'chapter1'
    }]

    getters = {
      getChapterByIndex: () => () => chapterFields,
      getTotalNumberOfChapters: () => 1
    }

    mutations = {
      UPDATE_FORM_STATUS: td.function(),
      SET_FORM_DATA: td.function(),
      SET_MAPPER_OPTIONS: td.function()
    }

    state = {
      questionnaireLabel: 'label',
      chapterFields: chapterFields,
      formData: {field1: 'value'},
      mapperOptions: {booleanLabels: 'labels'}
    }

    store = new Vuex.Store({actions, getters, mutations, state})
  })

  const stubs = ['router-link', 'router-view']
  const mocks = {$t: $t}
  const propsData = {chapterId: 1, questionnaireId: 'test_quest'}

  it('should not dispatch action [GET_QUESTIONNAIRE] when chapters are present', () => {
    shallow(QuestionnaireChapter, {propsData, mocks, store, stubs, localVue})
    td.verify(actions.GET_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined), {times: 0})
  })

  it('should dispatch a mutation to set mapperOptions', () => {
    state.mapperOptions = {}
    store = new Vuex.Store({actions, getters, mutations, state})

    shallow(QuestionnaireChapter, {propsData, mocks, store, stubs, localVue})
    td.verify(mutations.SET_MAPPER_OPTIONS(state, td.matchers.anything()))
  })

  it('should not dispatch a mutation to set mapperOptions if already set', function (done) {
    const test = this.test.title

    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick().then(() => {
      td.verify(mutations.SET_MAPPER_OPTIONS(state, td.matchers.anything()), {times: 0})
      done()
    }).catch(error => done(generateError(error, spec, test)))
  })

  it('should dispatch action [GET_QUESTIONNAIRE] when no chapters are present', () => {
    state.chapterFields = []
    store = new Vuex.Store({actions, getters, mutations, state})

    shallow(QuestionnaireChapter, {propsData, mocks, store, stubs, localVue})
    td.verify(actions.GET_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))
  })

  it('should set loading to false when action is done in created function', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.loading).to.equal(false)
  })

  it('should load formData from the state', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.formData).to.deep.equal(state.formData)
  })

  it('should use the current chapterId to calculate the next id properly', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.nextChapterNumber).to.equal(2)
  })

  it('should use the current chapterId to calculate the previous id properly', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.previousChapterNumber).to.equal(0)
  })

  it('should should not show next if the chapter id is equal to the total number of chapters', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.showNextButton).to.equal(false)
  })

  it('should show the next button if the chapter id is lower than the total number of chapters', () => {
    getters.getTotalNumberOfChapters = () => 2
    store = new Vuex.Store({actions, getters, mutations, state})

    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.showNextButton).to.equal(true)
  })

  it('should not show the previous button if the current chapter is the first chapter', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.showPreviousButton).to.equal(false)
  })

  it('should show the previous button if the current chapter is no longer the first', () => {
    propsData.chapterId = 2

    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.showPreviousButton).to.equal(true)
  })

  it('should return the total number of chapters from the state', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.totalNumberOfChapters).to.equal(1)
  })

  it('should compute the progress based on current chapter id and total number of chapters', () => {
    getters.getTotalNumberOfChapters = () => 10
    store = new Vuex.Store({actions, getters, mutations, state})
    propsData.chapterId = 2

    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.progressPercentage).to.equal(20)
  })

  it('should not show the progress bar if there is only one chapter', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.showProgressBar).to.equal(false)
  })

  it('should show the progress bar if there is more than one chapter', () => {
    getters.getTotalNumberOfChapters = () => 2
    store = new Vuex.Store({actions, getters, mutations, state})

    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.showProgressBar).to.equal(true)
  })

  it('should get the current chapter from the state by index', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.currentChapter).to.deep.equal([{id: 'chapter1'}])
  })

  it('should get the questionnaire label from the state', () => {
    const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
    expect(wrapper.vm.questionnaireLabel).to.equal('label')
  })

  describe('onValueChanged', () => {
    it('should commit the [SET_FORM_DATA] mutation when called', () => {
      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
      wrapper.vm.onValueChanged({id: '1'})
      td.verify(mutations.SET_FORM_DATA(state, {id: '1'}))
    })
  })

  describe('navigateToNextChapter', () => {
    const router = new VueRouter()

    it('should commit [UPDATE_FORM_STATUS] with value "SUBMITTED"', () => {
      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
      wrapper.vm.navigateToNextChapter()
      td.verify(mutations.UPDATE_FORM_STATUS(state, 'SUBMITTED'))
    })

    it('should set navigationBlocked to true if form is not valid', function (done) {
      const test = this.test.title

      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
      wrapper.setData({formState: {$valid: false}})
      wrapper.vm.navigateToNextChapter()

      wrapper.vm.$nextTick().then(() => {
        expect(wrapper.vm.navigationBlocked).to.equal(true)
        done()
      }).catch(error => done(generateError(error, spec, test)))
    })

    it('should commit [UPDATE_FORM_STATUS] with value "OPEN" if form is valid', () => {
      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, router, localVue})
      wrapper.setData({formState: {$valid: true}})
      wrapper.vm.navigateToNextChapter()
      td.verify(mutations.UPDATE_FORM_STATUS(state, td.matchers.isA(String)))
    })
  })

  describe('navigateToPreviousChapter', () => {
    const router = new VueRouter()

    it('should commit [UPDATE_FORM_STATUS] with value "OPEN" if navigation is blocked', () => {
      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, router, localVue})
      wrapper.setData({navigationBlocked: true})
      wrapper.vm.navigateToPreviousChapter()
      td.verify(mutations.UPDATE_FORM_STATUS(state, td.matchers.isA(String)))
    })

    it('should not commit [UPDATE_FORM_STATUS] when navigation is not blocked', () => {
      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, router, localVue})
      wrapper.setData({navigationBlocked: false})
      wrapper.vm.navigateToPreviousChapter()
      td.verify(mutations.UPDATE_FORM_STATUS(state, 'OPEN'), {times: 0})
    })
  })

  describe('submitQuestionnaire', () => {
    const router = new VueRouter()

    it('should commit [UPDATE_FORM_STATUS] with value "SUBMITTED"', () => {
      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
      wrapper.vm.submitQuestionnaire()
      td.verify(mutations.UPDATE_FORM_STATUS(state, td.matchers.isA(String)))
    })

    it('should set navigationBlocked to true if form is not valid', done => {
      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, localVue})
      wrapper.setData({formState: {$valid: false}})
      wrapper.vm.submitQuestionnaire()

      wrapper.vm.$nextTick(() => {
        expect(wrapper.vm.navigationBlocked).to.equal(true)
        done()
      })
    })

    it('should dispatch action [SUBMIT_QUESTIONNAIRE] when form is valid', function (done) {
      const test = this.test.title

      const wrapper = shallow(QuestionnaireChapter, {propsData, store, stubs, router, localVue})
      wrapper.setData({formState: {$valid: true}})
      wrapper.vm.submitQuestionnaire()

      wrapper.vm.$nextTick().then(() => {
        td.verify(actions.SUBMIT_QUESTIONNAIRE(td.matchers.anything(), td.matchers.isA(String), undefined))
        done()
      }).catch(error => done(generateError(error, spec, test)))
    })
  })
})
