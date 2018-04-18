import QuestionnaireTable from 'src/components/QuestionnaireTable'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

const $t = (key) => {
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
}

describe('QuestionnaireTable component', () => {
  let actions
  let localVue
  let state
  let store

  beforeEach(() => {
    td.reset()

    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)

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

    store = new Vuex.Store({state})
  })

  const stubs = ['router-link', 'router-view']

  it('should render a table of questionnaires', () => {
    const wrapper = shallow(QuestionnaireTable, {store, localVue, stubs})
    const rows = wrapper.findAll('tbody > tr')
    expect(rows.length).to.equal(3)

    expect(rows.at(0).contains('Questionnaire not started'))
    expect(rows.at(1).contains('Questionnaire open'))
    expect(rows.at(2).contains('Questionnaire submitted'))
  })

  it('should say it does not have questionnaires if list is empty', () => {
    state.questionnaireList = []
    store = new Vuex.Store({state, actions})

    const wrapper = shallow(QuestionnaireTable, {store, localVue, stubs})
    expect(wrapper.find('h3').text()).to.equal('no questionnaires')
  })
})
