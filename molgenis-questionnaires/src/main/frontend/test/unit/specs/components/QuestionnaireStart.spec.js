import QuestionnaireStart from 'src/components/QuestionnaireStart'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const localVue = createLocalVue()

localVue.use(Vuex)
localVue.filter('i18n', (key) => {
  const translations = {
    'questionnaire_loading_text': 'loading',
    'questionnaire_start': 'start',
    'questionnaire_back_to_questionnaire_list': 'back'
  }
  return translations[key]
})

describe('QuestionnaireStart component', () => {
  let actions
  let state
  let store

  beforeEach(() => {
    td.reset()

    state = {
      questionnaireDescription: 'description',
      questionnaireLabel: 'label',
      chapterFields: []
    }

    actions = {
      START_QUESTIONNAIRE: td.function(),
      GET_QUESTIONNAIRE: td.function()
    }

    store = new Vuex.Store({state, actions})
  })

  const stubs = ['router-link', 'router-view']
  const propsData = {questionnaireId: 'test_quest'}

  it('should dispatch action [START_QUESTIONNAIRE] to start a questionnaire when created', () => {
    shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    td.verify(actions.START_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))
  })

  it('should dispatch action [GET_QUESTIONNAIRE] to fetch a questionnaire when created and no chapters are present', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick(() => {
      td.verify(actions.GET_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))

      wrapper.vm.$nextTick(() => {
        expect(wrapper.vm.loading).to.equal(false)
      })
    })
  })

  it('should not dispatch action [GET_QUESTIONNAIRE] to fetch a questionnaire when there are chapters present', () => {
    state.chapterFields = ['chapter1']
    store = new Vuex.Store({state, actions})

    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    td.verify(actions.START_QUESTIONNAIRE(td.matchers.anything(), 'test_quest', undefined))

    wrapper.vm.$nextTick(() => {
      expect(wrapper.vm.loading).to.equal(false)
    })
  })

  it('should retrieve the description from the store', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    expect(wrapper.vm.questionnaireDescription).to.equal('description')
  })

  it('should retrieve the label from the store', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    expect(wrapper.vm.questionnaireLabel).to.equal('label')
  })

  it('should toggle template based on loading value', () => {
    const wrapper = shallow(QuestionnaireStart, {propsData, store, stubs, localVue})
    expect(wrapper.find('.spinner-container').exists()).to.equal(true)

    wrapper.vm.$nextTick(() => {
      wrapper.vm.$nextTick(() => {
        expect(wrapper.find('.spinner-container').exists()).to.equal(false)
      })
    })
  })
})

