import QuestionnaireList from 'src/components/QuestionnaireList'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const localVue = createLocalVue()

localVue.use(Vuex)
localVue.filter('i18n', (key) => {
  const translations = {
    'questionnaires_title': 'questionnaires',
    'questionnaires_description': 'list of questionnaires',
    'questionnaires_no_questionnaires_found_message': 'no questionnaires',
    'questionnaires_table_questionnaire_header': 'name',
    'questionnaires_table_status_header': 'status',
    'questionnaires_table_status_not_started': 'not started',
    'questionnaires_table_status_open': 'open',
    'questionnaires_table_status_submitted': 'submitted',
    'questionnaires_view_questionnaire': 'view'
  }
  return translations[key]
})

describe('QuestionnaireList component', () => {
  let actions
  let state
  let store

  beforeEach(() => {
    td.reset()

    state = {
      questionnaireList: [
        {
          label: 'Questionnaire not started',
          status: 'NOT_STARTED'
        },
        {
          label: 'Questionnaire open',
          status: 'OPEN'
        },
        {
          label: 'Questionnaire submitted',
          status: 'SUBMITTED'
        }
      ]
    }

    actions = {
      GET_QUESTIONNAIRE_LIST: td.function()
    }

    store = new Vuex.Store({state, actions})
  })

  const stubs = ['router-link', 'router-view']

  it('should dispatch action [GET_QUESTIONNAIRE_LIST] to get a list of questionnaires at creation time', () => {
    shallow(QuestionnaireList, {store, localVue, stubs})
    td.verify(actions.GET_QUESTIONNAIRE_LIST(td.matchers.anything(), undefined, undefined))
  })

  it('should set loading to false when action is done in created function', () => {
    const wrapper = shallow(QuestionnaireList, {store, localVue, stubs})
    wrapper.vm.$nextTick(() => {
      expect(wrapper.vm.loading).to.equal(false)
    })
  })

  it('should render the list of questionnaires from the state correctly', () => {
    const wrapper = shallow(QuestionnaireList, {store, localVue, stubs})
    expect(wrapper.vm.questionnaireList).to.deep.equal(state.questionnaireList)
  })

  it('should render a table of questionnaires', () => {
    const wrapper = shallow(QuestionnaireList, {store, localVue, stubs})

    wrapper.vm.$nextTick(() => {
      wrapper.vm.$nextTick(() => {
        const rows = wrapper.findAll('tbody > tr')
        expect(rows.length).to.equal(3)

        expect(rows.at(0).contains('Questionnaire not started'))
        expect(rows.at(1).contains('Questionnaire open'))
        expect(rows.at(2).contains('Questionnaire submitted'))
      })
    })
  })

  it('should say it does not have questionnaires if list is empty', () => {
    state.questionnaireList = []
    store = new Vuex.Store({state, actions})
    const wrapper = shallow(QuestionnaireList, {store, localVue, stubs})

    wrapper.vm.$nextTick(() => {
      wrapper.vm.$nextTick(() => {
        expect(wrapper.find('h3').text()).to.equal('no questionnaires')
      })
    })
  })
})
