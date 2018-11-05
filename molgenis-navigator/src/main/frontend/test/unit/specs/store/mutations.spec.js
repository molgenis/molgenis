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
  describe('ADD_JOB', () => {
    it('should add job to jobs in the state', () => {
      const job0 = {type: 'download', id: '0', status: 'running'}
      const job1 = {type: 'copy', id: '1', status: 'finished'}

      const state = {
        jobs: [job0]
      }

      mutations.__ADD_JOB__(state, job1)
      expect(state.jobs).to.deep.equal([job0, job1])
    })
  })
  describe('UPDATE_JOB', () => {
    it('should update existing job in the state', () => {
      const job0 = {type: 'download', id: '0', status: 'running'}
      const job1 = {type: 'copy', id: '0', status: 'running'}

      const state = {
        jobs: [job0, job1]
      }

      const updatedJob1 = {type: 'copy', id: '0', status: 'success'}
      mutations.__UPDATE_JOB__(state, updatedJob1)
      expect(state.jobs).to.deep.equal([job0, updatedJob1])
    })
  })
  describe('SET_FOLDER', () => {
    it('should set the folder', () => {
      const state = {
        folder: null
      }

      const folder = [
        {id: '0', label: 'grandchild', parent: {id: '1', label: 'child'}}
      ]

      mutations.__SET_FOLDER__(state, folder)
      expect(state.folder).to.deep.equal(folder)
    })
  })
  describe('SET_ITEMS', () => {
    it('should set the items in the state and clear the selected items', () => {
      const state = {
        items: [{type: 'PACKAGE', 'id': '0', 'label': 'label0'}],
        selectedItems: [{type: 'PACKAGE', 'id': '0', 'label': 'label0'}]
      }

      const items = [
        {type: 'PACKAGE', 'id': '1', 'label': 'label1'},
        {type: 'ENTITY_TYPE', 'id': '2', 'label': 'label2'}
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
        {type: 'PACKAGE', 'id': '1', 'label': 'label1'},
        {type: 'ENTITY_TYPE', 'id': '2', 'label': 'label2'}
      ]

      mutations.__SET_SELECTED_ITEMS__(state, selectedItems)
      expect(state.selectedItems).to.deep.equal(selectedItems)
    })
  })
  describe('SET_CLIPBOARD', () => {
    it('should set the clipboard in the state and clear the selected items', () => {
      const state = {
        clipboard: null
      }

      const clipboard = {
        mode: 'cut',
        items: [
          {type: 'PACKAGE', 'id': '1', 'label': 'label1'},
          {type: 'ENTITY_TYPE', 'id': '2', 'label': 'label2'}
        ]
      }

      mutations.__SET_CLIPBOARD__(state, clipboard)
      expect(state.selectedItems).to.deep.equal([])
      expect(state.clipboard).to.deep.equal(clipboard)
    })
  })
  describe('RESET_CLIPBOARD', () => {
    it('should set the clipboard to null in the state', () => {
      const state = {
        clipboard: {
          mode: 'cut',
          items: [
            {type: 'PACKAGE', 'id': '1', 'label': 'label1'},
            {type: 'ENTITY_TYPE', 'id': '2', 'label': 'label2'}
          ]
        }
      }

      mutations.__RESET_CLIPBOARD__(state)
      expect(state.clipboard).to.deep.equal(null)
    })
  })
})
