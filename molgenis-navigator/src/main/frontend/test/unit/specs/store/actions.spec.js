import actions from '@/store/actions'
import * as api from '@/utils/api'
import td from 'testdouble'
import utils from '@molgenis/molgenis-vue-test-utils'

describe('actions', () => {
  beforeEach(() => td.reset())

  describe('FETCH_ITEMS', () => {
    it('should dispatch FETCH_ITEMS_BY_QUERY action for the current query', done => {
      const options = {
        state: {
          query: 'MyQuery'
        },
        expectedActions: [
          {type: '__FETCH_ITEMS_BY_QUERY__', payload: 'MyQuery'}
        ]
      }
      utils.testAction(actions.__FETCH_ITEMS__, options, done)
    })
    it('should dispatch FETCH_ITEMS_BY_FOLDER action for the current folder', done => {
      const options = {
        state: {
          route: {
            params: {
              folderId: 'folderId'
            }
          }
        },
        expectedActions: [
          {type: '__FETCH_ITEMS_BY_FOLDER__', payload: 'folderId'}
        ]
      }
      utils.testAction(actions.__FETCH_ITEMS__, options, done)
    })
  })
  describe('FETCH_ITEMS_BY_QUERY', () => {
    it('should set items in the state for the given query', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]

      const getItemsByQuery = td.function('api.getItemsByQuery')
      td.when(getItemsByQuery('myQuery')).thenResolve(Promise.resolve(items))
      td.replace(api, 'getItemsByQuery', getItemsByQuery)

      const options = {
        payload: 'myQuery',
        expectedMutations: [
          {type: '__SET_ITEMS__', payload: items}
        ]
      }
      utils.testAction(actions.__FETCH_ITEMS_BY_QUERY__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const getItemsByQuery = td.function('api.getItemsByQuery')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(getItemsByQuery('myQuery')).thenResolve(Promise.reject(new Error()))
      td.replace(api, 'getItemsByQuery', getItemsByQuery)

      const options = {
        payload: 'myQuery',
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__FETCH_ITEMS_BY_QUERY__, options, done)
    })
  })
  describe('FETCH_ITEMS_BY_FOLDER', () => {
    it('should set folder and folder items in the state for the given folder id', done => {
      const folder = {id: 'id', label: 'label', readonly: false}
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]

      const getFolder = td.function('api.getFolder')
      td.when(getFolder('folderId')).thenResolve(Promise.resolve(folder))
      td.replace(api, 'getFolder', getFolder)

      const getItemsByFolderId = td.function('api.getItemsByFolderId')
      td.when(getItemsByFolderId('folderId')).thenResolve(Promise.resolve(items))
      td.replace(api, 'getItemsByFolderId', getItemsByFolderId)

      const options = {
        payload: 'folderId',
        expectedMutations: [
          {type: '__SET_FOLDER__', payload: folder},
          {type: '__SET_ITEMS__', payload: items}
        ]
      }
      utils.testAction(actions.__FETCH_ITEMS_BY_FOLDER__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]

      const getFolder = td.function('api.getFolder')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(getFolder('folderId')).thenResolve(Promise.reject(error))
      td.replace(api, 'getFolder', getFolder)

      const getItemsByFolderId = td.function('api.getItemsByFolderId')
      td.when(getItemsByFolderId('folderId')).thenResolve(Promise.resolve(items))
      td.replace(api, 'getItemsByFolderId', getItemsByFolderId)

      const options = {
        payload: 'folderId',
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__FETCH_ITEMS_BY_FOLDER__, options, done)
    })
  })
  describe('SELECT_ALL_ITEMS', () => {
    it('should update selected items in the state to be items', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]

      const options = {
        state: {
          items: items
        },
        expectedMutations: [
          {type: '__SET_SELECTED_ITEMS__', payload: items}
        ]
      }
      utils.testAction(actions.__SELECT_ALL_ITEMS__, options, done)
    })
  })
  describe('DESELECT_ALL_ITEMS', () => {
    it('should update selected items in the state to be empty', done => {
      const options = {
        state: {
          selectedItems: [{type: 'package', id: 'id', label: 'label', readonly: false}]
        },
        expectedMutations: [
          {type: '__SET_SELECTED_ITEMS__', payload: []}
        ]
      }
      utils.testAction(actions.__DESELECT_ALL_ITEMS__, options, done)
    })
  })
  describe('SELECT_ITEM', () => {
    it('should add given item to the selected items in the state', done => {
      const item0 = {type: 'package', id: 'id0', label: 'label0', readonly: false}
      const item1 = {type: 'package', id: 'id1', label: 'label1', readonly: false}
      const options = {
        state: {
          selectedItems: [item0]
        },
        payload: item1,
        expectedMutations: [
          {
            type: '__SET_SELECTED_ITEMS__',
            payload: [item0, item1]
          }
        ]
      }
      utils.testAction(actions.__SELECT_ITEM__, options, done)
    })
  })
  describe('DESELECT_ITEM', () => {
    it('should remove given item from the selected items in the state', done => {
      const item0 = {type: 'package', id: '0', label: 'label0', readonly: false}
      const item1 = {type: 'entityType', id: '0', label: 'label0', readonly: false}
      const options = {
        state: {
          selectedItems: [item0, item1]
        },
        payload: item0,
        expectedMutations: [
          {
            type: '__SET_SELECTED_ITEMS__',
            payload: [item1]
          }
        ]
      }
      utils.testAction(actions.__DESELECT_ITEM__, options, done)
    })
  })
  describe('DELETE_SELECTED_ITEMS', () => {
    it('should delete the selected items', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]

      const deleteItems = td.function('api.deleteItems')
      td.when(deleteItems(items)).thenResolve(Promise.resolve())
      td.replace(api, 'deleteItems', deleteItems)

      const options = {
        state: {
          selectedItems: items
        },
        expectedActions: [
          {type: '__FETCH_ITEMS__'}
        ]
      }
      utils.testAction(actions.__DELETE_SELECTED_ITEMS__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]

      const deleteItems = td.function('api.deleteItems')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(deleteItems(items)).thenResolve(Promise.reject(error))
      td.replace(api, 'deleteItems', deleteItems)

      const options = {
        state: {
          selectedItems: items
        },
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__DELETE_SELECTED_ITEMS__, options, done)
    })
  })
  describe('CREATE_ITEM', () => {
    it('should create the given item and refresh items in the state', done => {
      const item = {type: 'package', id: 'id', label: 'label', readonly: false}

      const createItem = td.function('api.createItem')
      td.when(createItem(item)).thenResolve(Promise.resolve())
      td.replace(api, 'createItem', createItem)

      const options = {
        payload: item,
        expectedActions: [
          {
            type: '__FETCH_ITEMS__'
          }
        ]
      }
      utils.testAction(actions.__CREATE_ITEM__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const item = {type: 'package', id: 'id', label: 'label', readonly: false}

      const createItem = td.function('api.createItem')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(createItem(item)).thenResolve(Promise.reject(error))
      td.replace(api, 'createItem', createItem)

      const options = {
        payload: item
      }
      utils.testAction(actions.__CREATE_ITEM__, options, done)
    })
  })
  describe('UPDATE_ITEM', () => {
    it('should update the given item and refresh items in the state', done => {
      const item = {type: 'package', id: 'id', label: 'label', readonly: false}
      const updatedItem = {type: 'package', id: 'id', label: 'labelNew', readonly: false}

      const updateItem = td.function('api.updateItem')
      td.when(updateItem(item, updatedItem)).thenResolve(Promise.resolve())
      td.replace(api, 'updateItem', updateItem)

      const options = {
        state: {
          items: [item]
        },
        payload: updatedItem,
        expectedActions: [
          {
            type: '__FETCH_ITEMS__'
          }
        ]
      }
      utils.testAction(actions.__UPDATE_ITEM__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const item = {type: 'package', id: 'id', label: 'label', readonly: false}
      const updatedItem = {type: 'package', id: 'id', label: 'labelNew', readonly: false}

      const updateItem = td.function('api.createItem')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(updateItem(item, updatedItem)).thenResolve(Promise.reject(error))
      td.replace(api, 'updateItem', updateItem)

      const options = {
        state: {
          items: [item]
        },
        payload: updatedItem
      }
      utils.testAction(actions.__UPDATE_ITEM__, options, done)
    })
  })
  describe('MOVE_CLIPBOARD_ITEMS', () => {
    it('should move clipboard items to given target folder', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]
      const folder = {id: 'id', label: 'label', readonly: false}

      const moveItems = td.function('api.moveItems')
      td.when(moveItems(items, folder)).thenResolve(Promise.resolve())
      td.replace(api, 'moveItems', moveItems)

      const options = {
        payload: folder,
        state: {
          clipboard: {
            mode: 'cut',
            items: items
          }
        },
        expectedActions: [
          {type: '__FETCH_ITEMS__'}
        ],
        expectedMutations: [
          {type: '__RESET_CLIPBOARD__'}
        ]
      }
      utils.testAction(actions.__MOVE_CLIPBOARD_ITEMS__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]
      const folder = {id: 'id', label: 'label', readonly: false}

      const moveItems = td.function('api.moveItems')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(moveItems(items, folder)).thenResolve(Promise.reject(error))
      td.replace(api, 'moveItems', moveItems)

      const options = {
        payload: folder,
        state: {
          clipboard: {
            mode: 'cut',
            items: items
          }
        },
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__MOVE_CLIPBOARD_ITEMS__, options, done)
    })
  })
  describe('COPY_CLIPBOARD_ITEMS', () => {
    it('should copy clipboard items to given target folder', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]
      const folder = {id: 'id', label: 'label', readonly: false}
      const job = {type: 'copy', id: 'id', status: 'running'}
      const copyItems = td.function('api.copyItems')
      td.when(copyItems(items, folder)).thenResolve(Promise.resolve(job))
      td.replace(api, 'copyItems', copyItems)

      const options = {
        payload: folder,
        state: {
          clipboard: {
            mode: 'copy',
            items: items
          }
        },
        expectedActions: [
          {type: '__POLL_JOB__', payload: job}
        ],
        expectedMutations: [
          {type: '__RESET_CLIPBOARD__'},
          {type: '__ADD_JOB__', payload: job}
        ]
      }
      utils.testAction(actions.__COPY_CLIPBOARD_ITEMS__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const items = [{type: 'package', id: 'id', label: 'label', readonly: false}]
      const folder = {id: 'id', label: 'label', readonly: false}

      const copyItems = td.function('api.copyItems')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(copyItems(items, folder)).thenResolve(Promise.reject(error))
      td.replace(api, 'copyItems', copyItems)

      const options = {
        payload: folder,
        state: {
          clipboard: {
            mode: 'cut',
            items: items
          }
        },
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__COPY_CLIPBOARD_ITEMS__, options, done)
    })
  })
})
