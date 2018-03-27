import ChapterNavigation from 'src/components/ChapterNavigation'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'
import VueRouter from 'vue-router'

const $t = (key) => {
  const translations = {
    'questionnaire_next_chapter': 'next',
    'questionnaire_previous_chapter': 'previous',
    'questionnaire_submit': 'submit',
    'questionnaire_forgotten_chapters': 'forgot'
  }
  return translations[key]
}

describe('ChapterNavigation component', () => {
  let actions
  let chapterCompletion
  let getters
  let localVue
  let mutations
  let state
  let store

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    actions = {
      SUBMIT_QUESTIONNAIRE: td.function()
    }

    chapterCompletion = {
      chapter1: true,
      chapter2: false
    }

    getters = {
      getChapterCompletion: () => chapterCompletion,
      getTotalNumberOfChapters: () => 2
    }

    mutations = {
      BLOCK_NAVIGATION: td.function(),
      SET_ERROR: td.function(),
      UPDATE_FORM_STATUS: td.function()
    }

    state = {
      error: '',
      formData: {
        field1: 'value'
      }
    }

    store = new Vuex.Store({actions, getters, mutations, state})
  })

  const stubs = ['router-link', 'router-view']
  const mocks = {$t: $t}
  const propsData = {
    chapterId: 3,
    currentChapter: {id: 'chapter1'},
    formState: {_submit: () => true, _reset: () => true},
    questionnaireId: 'test_quest'
  }

  it('should compute whether all chapters are complete based on chapterCompletion', () => {
    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.allChaptersAreComplete).to.equal(false)
  })

  it('should return chapterCompletion from the state', () => {
    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.chapterCompletion).to.deep.equal(chapterCompletion)
  })

  it('should return error from the state', () => {
    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.error).to.equal(state.error)
  })

  it('should compute the next chapter id using the current chapter id', () => {
    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.nextChapterNumber).to.equal(4)
  })

  it('should compute the previous chapter id using the current chapter id', () => {
    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.previousChapterNumber).to.equal(2)
  })

  it('should compute to not show the next chapter button', () => {
    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.showNextButton).to.equal(false)
  })

  it('should compute to show the next chapter button', () => {
    propsData.chapterId = 1

    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.showNextButton).to.equal(true)
  })

  it('should compute to not show the previous chapter button', () => {
    propsData.chapterId = 1

    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.showPreviousButton).to.equal(false)
  })

  it('should compute to show the previous chapter button', () => {
    propsData.chapterId = 3

    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.showPreviousButton).to.equal(true)
  })

  it('should return the total number of chapters from the state', () => {
    const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, store, stubs})
    expect(wrapper.vm.totalNumberOfChapters).to.equal(2)
  })

  describe('validateBeforeNavigatingToNextChapter', () => {
    const router = new VueRouter()

    it('should commit [UPDATE_FORM_STATUS] with the value "SUBMITTED"', () => {
      const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, router, store, stubs})
      wrapper.vm.validateBeforeNavigatingToNextChapter()
      td.verify(mutations.UPDATE_FORM_STATUS(td.matchers.anything(), 'SUBMITTED'))
    })

    it('should commit [BLOCK_NAVIGATION] with the value "true" if the current chapter is not completed', () => {
      propsData.currentChapter = {id: 'chapter2'}

      const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, router, store, stubs})
      wrapper.vm.validateBeforeNavigatingToNextChapter()
      td.verify(mutations.BLOCK_NAVIGATION(td.matchers.anything(), true))
    })
  })

  describe('validateBeforeSubmit', () => {
    const router = new VueRouter()

    it('should commit [UPDATE_FORM_STATUS] with the value "SUBMITTED"', () => {
      const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, router, store, stubs})
      wrapper.vm.validateBeforeSubmit()
      td.verify(mutations.UPDATE_FORM_STATUS(td.matchers.anything(), 'SUBMITTED'))
    })

    it('should commit [SET_ERROR] if the current chapter is complete but not all chapters are', () => {
      propsData.currentChapter = {id: 'chapter1'}

      const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, router, store, stubs})
      wrapper.vm.validateBeforeSubmit()
      td.verify(mutations.SET_ERROR(td.matchers.anything(), 'forgot: chapter2'))
    })

    it('should commit [BLOCK_NAVIGATION] with the value "true" if the current chapter is not completed', () => {
      propsData.currentChapter = {id: 'chapter2'}

      const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, router, store, stubs})
      wrapper.vm.validateBeforeSubmit()
      td.verify(mutations.BLOCK_NAVIGATION(td.matchers.anything(), true))
    })

    it('should dispatch [SUBMIT_QUESTIONNAIRE] on completion', () => {
      chapterCompletion = {chapter1: true, chapter2: true}
      store = new Vuex.Store({actions, getters, mutations, state})

      const wrapper = shallow(ChapterNavigation, {propsData, localVue, mocks, router, store, stubs})
      wrapper.vm.validateBeforeSubmit()
      td.verify(actions.SUBMIT_QUESTIONNAIRE(td.matchers.anything(), td.matchers.isA(String), undefined))
    })
  })
})
