import QuestionnaireThankYou from 'src/components/QuestionnaireThankYou'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const localVue = createLocalVue()

localVue.use(Vuex)
localVue.filter('i18n', (key) => {
  const translations = {
    'questionnaire_back_to_questionnaire_list': 'go back'
  }
  return translations[key]
})

describe('QuestionnaireThankYou component', () => {
  const state = {
    submissionText: 'thank you'
  }

  const actions = {
    GET_SUBMISSION_TEXT: td.function()
  }

  const store = new Vuex.Store({
    state,
    actions
  })

  const stubs = ['router-link', 'router-view']

  const propsData = {questionnaireId: 'test_quest'}
  const wrapper = shallow(QuestionnaireThankYou, {propsData, store, localVue, stubs})

  it('should dispatch an action to get the submissionText on created', () => {
    td.verify(actions.GET_SUBMISSION_TEXT(td.matchers.anything(), 'test_quest', undefined))
  })

  it('should render the submissionText from the state correctly', () => {
    expect(wrapper.vm.submissionText).to.equal('thank you')
  })
})
