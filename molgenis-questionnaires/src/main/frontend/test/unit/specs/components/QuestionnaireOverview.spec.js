import QuestionnaireOverview from 'src/components/QuestionnaireOverview'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const localVue = createLocalVue()

localVue.use(Vuex)
localVue.filter('i18n', (key) => {
  const translations = {
    'questionnaire_overview_loading_text': 'loading overview',
    'questionnaires_overview_title': 'overview'
  }
  return translations[key]
})

describe.only('QuestionnaireOverview component', () => {
  let actions
  let store
  let questionnaire

  beforeEach(() => {
    actions = {
      GET_QUESTIONNAIRE_OVERVIEW: td.function()
    }

    questionnaire = {
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
    }

    td.when(actions.GET_QUESTIONNAIRE_OVERVIEW(td.matchers.anything(), 'test_quest', undefined)).thenResolve(questionnaire)
    store = new Vuex.Store({actions})
  })

  const stubs = ['router-link', 'router-view']
  const propsData = {questionnaireId: 'test_quest'}

  it('should set a local questionnaire object when created', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick(() => {
      expect(wrapper.vm.questionnaire).to.deep.equal(questionnaire)
    })
  })

  it('should set loading to false when done setting the local questionnaire', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick(() => {
      expect(wrapper.vm.loading).to.equal(false)
    })
  })

  it('should have computed data after being created', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick(() => {
      expect(wrapper.vm.data).to.deep.equal(questionnaire.items[0])
    })
  })

  it('should have computed attributes after being created', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick(() => {
      expect(wrapper.vm.attributes).to.deep.equal([questionnaire.meta.attributes[1]])
    })
  })

  it('should toggle template based on loading value', () => {
    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    expect(wrapper.find('.spinner-container').exists()).to.equal(true)

    wrapper.vm.$nextTick(() => {
      wrapper.vm.$nextTick(() => {
        expect(wrapper.find('.spinner-container').exists()).to.equal(false)
      })
    })
  })
})
