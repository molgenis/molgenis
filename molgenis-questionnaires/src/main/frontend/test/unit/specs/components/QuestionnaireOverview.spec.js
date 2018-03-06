import QuestionnaireOverview from 'src/components/QuestionnaireOverview'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'
import { generateError } from '../../utils'

const $t = (key) => {
  const translations = {
    'questionnaire_overview_loading_text': 'loading overview',
    'questionnaires_overview_title': 'overview'
  }
  return translations[key]
}

describe('QuestionnaireOverview component', function () {
  const spec = this.title

  let actions
  let localVue
  let store
  let questionnaire

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

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

    td.when(actions.GET_QUESTIONNAIRE_OVERVIEW(td.matchers.anything(), td.matchers.anything(), td.matchers.anything())).thenResolve(questionnaire)
    store = new Vuex.Store({actions})
  })

  const stubs = ['router-link', 'router-view']
  const propsData = {questionnaireId: 'test_quest'}

  it('should set a local questionnaire object when created', function (done) {
    const test = this.test.title

    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick().then(() => {
      expect(wrapper.vm.questionnaire).to.deep.equal(questionnaire)
      done()
    }).catch(error => done(generateError(error, spec, test)))
  })

  it('should set loading to false when done setting the local questionnaire', function (done) {
    const test = this.test.title

    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick().then(() => {
      expect(wrapper.vm.loading).to.equal(false)
      done()
    }).catch(error => done(generateError(error, spec, test)))
  })

  it('should have computed data after being created', function (done) {
    const test = this.test.title

    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick().then(() => {
      expect(wrapper.vm.data).to.deep.equal(questionnaire.items[0])
      done()
    }).catch(error => done(generateError(error, spec, test)))
  })

  it('should have computed attributes after being created', function (done) {
    const test = this.test.title

    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    wrapper.vm.$nextTick().then(() => {
      expect(wrapper.vm.attributes).to.deep.equal([questionnaire.meta.attributes[1]])
      done()
    }).catch(error => done(generateError(error, spec, test)))
  })

  it('should toggle template based on loading value', function (done) {
    const test = this.test.title

    const wrapper = shallow(QuestionnaireOverview, {propsData, store, stubs, localVue})
    expect(wrapper.find('.spinner-container').exists()).to.equal(true)

    wrapper.vm.$nextTick().then(() => {
      expect(wrapper.find('.spinner-container').exists()).to.equal(false)
      done()
    }).catch(error => done(generateError(error, spec, test)))
  })
})
