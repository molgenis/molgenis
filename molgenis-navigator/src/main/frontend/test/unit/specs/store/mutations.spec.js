import mutations from '@/store/mutations'

describe('mutations', () => {
  describe('ADD_ALERTS', () => {
    it('should set add the alerts to existing alerts in the state', () => {
      const state = {
        alerts: [{type: 'error', message: 'error message', code: 'error code'}]
      }

      const alerts = [
        [{type: 'info', message: 'info message'}]
      ]

      mutations.__ADD_ALERTS__(state, alerts)
      expect(state.alerts[1]).to.eql(alerts[0])
    })
  })
  describe('REMOVE_ALERT', () => {
    it('should remove an alert by index in the state', () => {
      const state = {
        alerts: [
          {type: 'error', message: 'error message', code: 'error code'},
          {type: 'info', message: 'info message'}
        ]
      }

      const expectedAlerts = [
        {type: 'info', message: 'info message'}
      ]
      mutations.__REMOVE_ALERT__(state, 0)
      expect(state.alerts).to.eql(expectedAlerts)
    })
  })
  describe('SET_JOBS', () => {
    it('should set the jobs in the state', () => {
      const state = {
        jobs: []
      }

      const jobs = [
        {type: 'download', id: '0', status: 'running'},
        {type: 'copy', id: '1', status: 'finished'}
      ]

      mutations.__SET_JOBS__(state, jobs)
      expect(state.jobs).to.deep.equal(jobs)
    })
  })
  describe('SET_PACKAGE', () => {
    it('should set the package', () => {
      const state = {
        package: null
      }

      const _package = [
        {id: '0', label: 'grandchild', parent: {id: '1', label: 'child'}}
      ]

      mutations.__SET_PACKAGE__(state, _package)
      expect(state.path).to.deep.equal(_package)
    })
  })
  describe('SET_ITEMS', () => {
    it('should set the items in the state and clear the selected items', () => {
      const state = {
        items: [{type: 'package', 'id': '0', 'label': 'label0'}],
        selectedItems: [{type: 'package', 'id': '0', 'label': 'label0'}]
      }

      const items = [
        {type: 'package', 'id': '1', 'label': 'label1'},
        {type: 'entityType', 'id': '2', 'label': 'label2'}
      ]

      mutations.__SET_ITEMS__(state, items)
      expect(state.items).to.deep.equal(items)
      expect(state.selectedItems).to.deep.equal([])
    })
  })
  describe('SET_SELECTED_ITEMS', () => {
    it('should set the selected items in the state', () => {
      const state = {
        selectedItems: []
      }

      const selectedItems = [
        {type: 'package', 'id': '1', 'label': 'label1'},
        {type: 'entityType', 'id': '2', 'label': 'label2'}
      ]

      mutations.__SET_SELECTED_ITEMS__(state, selectedItems)
      expect(state.selectedItems).to.deep.equal(selectedItems)
    })
  })
  describe('SET_CLIPBOARD', () => {
    it('should set the clipboard in the state', () => {
      const state = {
        clipboard: null
      }

      const clipboard = {
        mode: 'cut',
        items: [
          {type: 'package', 'id': '1', 'label': 'label1'},
          {type: 'entityType', 'id': '2', 'label': 'label2'}
        ]
      }

      mutations.__SET_CLIPBOARD__(state, clipboard)
      expect(state.clipboard).to.deep.equal(clipboard)
    })
  })
  describe('RESET_CLIPBOARD', () => {
    it('should set the clipboard to null in the state', () => {
      const state = {
        clipboard: {
          mode: 'cut',
          items: [
            {type: 'package', 'id': '1', 'label': 'label1'},
            {type: 'entityType', 'id': '2', 'label': 'label2'}
          ]
        }
      }

      mutations.__RESET_CLIPBOARD__(state)
      expect(state.clipboard).to.deep.equal(null)
    })
  })
})
