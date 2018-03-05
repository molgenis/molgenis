import ChapterList from 'src/components/ChapterList'
import { createLocalVue, shallow } from '@vue/test-utils'
import Vuex from 'vuex'

const localVue = createLocalVue()

localVue.use(Vuex)
localVue.filter('i18n', (key) => {
  const translations = {
    'questionnaire_saving_changes': 'saving',
    'questionnaire_changes_saved': 'saved',
    'questionnaire_chapters': 'chapters'
  }
  return translations[key]
})

describe('ChapterList component', () => {
  let getters
  let store
  let chapterProgress
  let chapterNavigationList

  beforeEach(() => {
    chapterProgress = {
      1: 'complete',
      2: 'incomplete'
    }

    chapterNavigationList = [
      {id: 1, index: 1, label: 'Chapter 1'},
      {id: 2, index: 2, label: 'Chapter 2'}
    ]

    getters = {
      getChapterProgress: () => chapterProgress,
      getChapterNavigationList: () => chapterNavigationList
    }

    store = new Vuex.Store({
      getters
    })
  })

  const propsData = {
    questionnaireId: 'test_quest',
    chapterId: 1,
    changesMade: false,
    saving: false
  }

  it('should return the chapterProgress via a getter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, localVue})
    expect(wrapper.vm.chapterProgress).to.deep.equal(chapterProgress)
  })

  it('should return the chapterNavigationList via a getter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, localVue})
    expect(wrapper.vm.chapterNavigationList).to.deep.equal(chapterNavigationList)
  })

  describe('isChapterCompleted method', () => {
    it('should return [TRUE] when complete', () => {
      const wrapper = shallow(ChapterList, {propsData, store, localVue})
      expect(wrapper.vm.isChapterCompleted(1)).to.equal(true)
    })

    it('should return [FALSE] when incomplete', () => {
      const wrapper = shallow(ChapterList, {propsData, store, localVue})
      expect(wrapper.vm.isChapterCompleted(2)).to.equal(false)
    })
  })
})
