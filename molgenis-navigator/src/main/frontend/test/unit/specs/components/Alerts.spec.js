import { shallow, createLocalVue } from 'vue-test-utils'
import Vuex from 'vuex'
import td from 'testdouble'
import Alerts from '@/components/Alerts'

const localVue = createLocalVue()
localVue.use(Vuex)

describe('Alerts.vue', () => {
  describe('removeAlert', () => {
    it('should remove the error alert', () => {
      const state = {
        alerts: [{type: 'ERROR', message: 'error message', code: 'error code'},
          {type: 'INFO', message: 'info message'},
          {type: 'SUCCESS', message: 'success message', code: 'error code'},
          {type: 'WARNING', message: 'warning message', code: 'error code'}]
      }
      const removeAlert = td.function('__REMOVE_ALERT__')
      let store = new Vuex.Store({
        state: state,
        mutations: {
          __REMOVE_ALERT__: removeAlert
        }
      })

      const wrapper = shallow(Alerts, {store, localVue})
      wrapper.find('b-alert').trigger('dismissed')
      td.verify(removeAlert(state, 0))
    })
  })
})
