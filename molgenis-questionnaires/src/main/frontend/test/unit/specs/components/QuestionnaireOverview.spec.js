import QuestionnaireOverview from 'src/pages/QuestionnaireOverview'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {
    'questionnaire_overview_loading_text': 'loading overview',
    'questionnaires_overview_title': 'overview'
  }
  return translations[key]
}

describe('QuestionnaireOverview component', () => {
  let actions
  let localVue
  let store
  let state

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

    actions = {
      GET_QUESTIONNAIRE_OVERVIEW: td.function()
    }

    state = {
      questionnaire: {
        meta: {
          attributes: [
            {
              name: 'id',
              fieldType: 'STRING'
            },
            {
              name: 'compound',
              fieldType: 'COMPOUND',
              attributes: [
                {
                  name: 'field1'
                },
                {
                  name: 'field2'
                }
              ]
            }
          ]
        },
        items: [{
          id: 'id',
          field1: 'value',
          field2: 'other value'
        }]
      },
      error: 'error',
      loading: true
    }

    store = new Vuex.Store({actions, state})
  })

  const stubs = ['router-link', 'router-view']
  const mocks = {$t: $t}
  const propsData = {questionnaireId: 'test_quest'}

  it('should return the questionnaire fields from the state', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue, mocks})
    const expected = [{name: 'compound', fieldType: 'COMPOUND', attributes: [{name: 'field1'}, {name: 'field2'}]}]
    expect(wrapper.vm.getQuestionnaireFields()).to.deep.equal(expected)
  })

  it('should return the questionnaire data from the state', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue, mocks})
    const expected = {id: 'id', field1: 'value', field2: 'other value'}
    expect(wrapper.vm.getQuestionnaireData()).to.deep.equal(expected)
  })

  it('should return error from the store', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue, mocks})
    expect(wrapper.vm.error).to.equal('error')
  })

  it('should return loading from the store', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue, mocks})
    expect(wrapper.vm.loading).to.equal(true)
  })

  it('should dispatch the [GET_QUESTIONNAIRE_OVERVIEW] action on create', () => {
    shallow(QuestionnaireOverview, {propsData, store, stubs, localVue, mocks})
    td.verify(actions.GET_QUESTIONNAIRE_OVERVIEW(td.matchers.anything(), 'test_quest', undefined))
  })
})
