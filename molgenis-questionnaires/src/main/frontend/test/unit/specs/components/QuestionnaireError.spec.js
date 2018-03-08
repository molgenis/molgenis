import QuestionnaireError from 'src/components/QuestionnaireError'
import { createLocalVue, shallow } from '@vue/test-utils'

const $t = (key) => {
  const translations = {
    'questionnaire_back_to_questionnaire_list': 'go back'
  }
  return translations[key]
}

describe('QuestionnaireError component', () => {
  let localVue

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.filter('i18n', $t)
  })

  const stubs = ['router-link', 'router-view']
  const propsData = {error: 'error test'}

  it('should render an error in pre tags', () => {
    const wrapper = shallow(QuestionnaireError, {propsData, stubs, localVue})
    expect(wrapper.contains('pre')).to.equal(true)

    const pre = wrapper.find('pre')
    expect(pre.text()).to.equal('error test')
  })
})
