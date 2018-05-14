import ChapterForm from 'src/components/ChapterForm'
import { createLocalVue, shallow } from '@vue/test-utils'
import td from 'testdouble'
import Vuex from 'vuex'

describe('ChapterForm component', () => {
  let actions
  let localVue
  let state
  let store

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)

    actions = {
      VALIDATE_FIELD: td.function()
    }

    state = {
      formData: {
        field1: 'value'
      }
    }

    store = new Vuex.Store({state, actions})
  })

  const propsData = {
    currentChapter: {},
    formState: {},
    questionnaireId: 'test_quest'
  }

  it('should retrieve formData from the state', () => {
    const wrapper = shallow(ChapterForm, {propsData, localVue, store})
    expect(wrapper.vm.formData).to.deep.equal(state.formData)
  })

  it('should dispatch [VALIDATE_FIELD] action when onValueChanged method is called', () => {
    const wrapper = shallow(ChapterForm, {propsData, localVue, store})
    const updatedFormData = {field1: 'new value'}

    wrapper.vm.onValueChanged(updatedFormData)
    td.verify(actions.VALIDATE_FIELD(td.matchers.anything(),
      {
        formData: {field1: 'new value'},
        formState: {}
      },
      undefined))
  })
})
