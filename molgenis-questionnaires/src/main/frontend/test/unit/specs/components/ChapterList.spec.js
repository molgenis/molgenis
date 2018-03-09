import ChapterList from 'src/components/ChapterList'
import { createLocalVue, shallow } from '@vue/test-utils'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {
    'questionnaire_saving_changes': 'saving',
    'questionnaire_changes_saved': 'saved',
    'questionnaire_chapters': 'chapters'
  }
  return translations[key]
}

describe('ChapterList component', () => {
  let chapterNavigationList
  let allVisibleFieldIdsInChapters
  let getters
  let localVue
  let state
  let store

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    allVisibleFieldIdsInChapters = {
      'chapter1': ['field1', 'field2'],
      'chapter2': ['field3', 'field4', 'field5', 'field6']
    }

    chapterNavigationList = [
      {id: 'chapter1', index: 1, label: 'Chapter 1'},
      {id: 'chapter2', index: 2, label: 'Chapter 2'}
    ]

    state = {
      formData: {
        'field1': 'value',
        'field2': 'value',
        'field3': ['value'],
        'field4': undefined,
        'field5': [],
        'field6': 'value'
      }
    }

    getters = {
      getChapterNavigationList: () => chapterNavigationList,
      getVisibleFieldIdsForAllChapters: () => allVisibleFieldIdsInChapters
    }

    store = new Vuex.Store({state, getters})
  })

  const stubs = ['router-link', 'router-view']
  const propsData = {
    questionnaireId: 'test_quest',
    chapterId: 1,
    changesMade: false,
    saving: false
  }

  it('should return the chapterNavigationList via a getter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, stubs, localVue})
    expect(wrapper.vm.chapterNavigationList).to.deep.equal(chapterNavigationList)
  })

  it('should return the allVisibleFieldIdsInChapters via a getter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, stubs, localVue})
    expect(wrapper.vm.allVisibleFieldIdsInChapters).to.deep.equal(allVisibleFieldIdsInChapters)
  })

  it('should use the allVisibleFieldIdsInChapters to compute the number of visible fields per chapter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, stubs, localVue})
    const expected = {'chapter1': 2, 'chapter2': 4}

    expect(wrapper.vm.numberOfVisibleFieldsPerChapter).to.deep.equal(expected)
  })

  it('should use the allVisibleFieldIdsInChapters to compute the number of filled in field per chapter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, stubs, localVue})
    const expected = {'chapter1': 2, 'chapter2': 2}

    expect(wrapper.vm.numberOfFilledInFieldsPerChapter).to.deep.equal(expected)
  })

  it('should use the numbe of filled in fields and total visible fields to compute the total progress per chapter', () => {
    const wrapper = shallow(ChapterList, {propsData, store, stubs, localVue})
    const expected = {'chapter1': 100, 'chapter2': 50}
    expect(wrapper.vm.progressPerChapter).to.deep.equal(expected)
  })
})
