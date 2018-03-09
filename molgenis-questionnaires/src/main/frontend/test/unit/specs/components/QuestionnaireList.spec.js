import QuestionnaireList from 'src/pages/QuestionnaireList'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {
    'questionnaire_loading_list': 'loading questionnaires'
  }
  return translations[key]
}

describe('QuestionnaireList component', function () {
  let actions
  let localVue
  let state
  let store

  beforeEach(() => {
    td.reset()

    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    actions = {
      GET_QUESTIONNAIRE_LIST: td.function()
    }

    state = {error: 'error', loading: true}
    store = new Vuex.Store({state, actions})
  })

  const stubs = ['router-link', 'router-view']
  const mocks = {$t: $t}

  it('should dispatch action [GET_QUESTIONNAIRE_LIST] to get a list of questionnaires at creation time', () => {
    shallow(QuestionnaireList, {store, localVue, stubs, mocks})
    td.verify(actions.GET_QUESTIONNAIRE_LIST(td.matchers.anything(), undefined, undefined))
  })

  it('should return error from the store', () => {
    const wrapper = shallow(QuestionnaireList, {store, localVue, stubs, mocks})
    expect(wrapper.vm.error).to.equal('error')
  })

  it('should return loading from the store', () => {
    const wrapper = shallow(QuestionnaireList, {store, localVue, stubs, mocks})
    expect(wrapper.vm.loading).to.equal(true)
  })
})
