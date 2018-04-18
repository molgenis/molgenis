import ChapterList from 'src/components/ChapterList'
import { createLocalVue, shallow } from '@vue/test-utils'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {
    'questionnaire_chapters': 'chapters'
  }
  return translations[key]
}

describe('ChapterList component', () => {
  let chapterNavigationList
  let chapterProgress
  let getters
  let localVue
  let store

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    chapterNavigationList = [
      {id: 'chapter1', index: 1, label: 'Chapter 1'},
      {id: 'chapter2', index: 2, label: 'Chapter 2'}
    ]

    chapterProgress = {
      'chapter1': 100,
      'chapter2': 50
    }

    getters = {
      getChapterNavigationList: () => chapterNavigationList,
      getChapterProgress: () => chapterProgress
    }

    store = new Vuex.Store({getters})
  })

  const stubs = ['router-link', 'router-view']
  const propsData = {questionnaireId: 'test_quest'}

  it('should return the chapterNavigationList via a getter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, stubs, localVue})
    expect(wrapper.vm.chapterNavigationList).to.deep.equal(chapterNavigationList)
  })

  it('should return the chapterProgress via a getter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, stubs, localVue})
    expect(wrapper.vm.chapterProgress).to.deep.equal(chapterProgress)
  })
})
