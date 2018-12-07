import actions from '@/store/actions'
import * as api from '@/utils/api'
import td from 'testdouble'
import utils from '@molgenis/molgenis-vue-test-utils'

describe('actions', () => {
  beforeEach(() => td.reset())

  describe('FETCH_RESOURCES', () => {
    it('should dispatch FETCH_RESOURCES_BY_QUERY action for the current query', done => {
      const options = {
        state: {
          query: 'MyQuery'
        },
        expectedActions: [
          {type: '__FETCH_RESOURCES_BY_QUERY__', payload: 'MyQuery'}
        ]
      }
      utils.testAction(actions.__FETCH_RESOURCES__, options, done)
    })
    it('should dispatch FETCH_RESOURCES_BY_FOLDER action for the current folder', done => {
      const options = {
        state: {
          route: {
            params: {
              folderId: 'folderId'
            }
          }
        },
        expectedActions: [
          {type: '__FETCH_RESOURCES_BY_FOLDER__', payload: 'folderId'}
        ]
      }
      utils.testAction(actions.__FETCH_RESOURCES__, options, done)
    })
  })
  describe('FETCH_RESOURCES_BY_QUERY', () => {
    it('should set resources in the state for the given query', done => {
      const resources = [{type: 'PACKAGE', id: 'id', label: 'label', readonly: false}]

      const getResourcesByQuery = td.function('api.getResourcesByQuery')
      td.when(getResourcesByQuery('myQuery')).thenResolve(Promise.resolve(resources))
      td.replace(api, 'getResourcesByQuery', getResourcesByQuery)

      const options = {
        payload: 'myQuery',
        expectedMutations: [
          {type: '__SET_RESOURCES__', payload: resources}
        ]
      }
      utils.testAction(actions.__FETCH_RESOURCES_BY_QUERY__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const getResourcesByQuery = td.function('api.getResourcesByQuery')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(getResourcesByQuery('myQuery')).thenResolve(Promise.reject(new Error()))
      td.replace(api, 'getResourcesByQuery', getResourcesByQuery)

      const options = {
        payload: 'myQuery',
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__FETCH_RESOURCES_BY_QUERY__, options, done)
    })
  })
  describe('FETCH_RESOURCES_BY_FOLDER', () => {
    it('should set folder and folder resources in the state for the given folder id', done => {
      const folderState = {
        folder: {id: 'id', label: 'label', readonly: false},
        resources: [{
          type: 'PACKAGE',
          id: 'id',
          label: 'label',
          readonly: false
        }]
      }

      const getResourcesByFolderId = td.function('api.getResourcesByFolderId')
      td.when(getResourcesByFolderId('folderId')).thenResolve(Promise.resolve(folderState))
      td.replace(api, 'getResourcesByFolderId', getResourcesByFolderId)

      const options = {
        payload: 'folderId',
        expectedMutations: [
          {type: '__SET_FOLDER__', payload: folderState.folder},
          {type: '__SET_RESOURCES__', payload: folderState.resources}
        ]
      }
      utils.testAction(actions.__FETCH_RESOURCES_BY_FOLDER__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const getResourcesByFolderId = td.function('api.getResourcesByFolderId')
      td.when(getResourcesByFolderId('folderId')).thenResolve(Promise.reject(new Error()))
      td.replace(api, 'getResourcesByFolderId', getResourcesByFolderId)

      const options = {
        payload: 'folderId',
        expectedMutations: [
          {type: '__ADD_ALERTS__'}
        ]
      }
      utils.testAction(actions.__FETCH_RESOURCES_BY_FOLDER__, options, done)
    })
  })
  describe('SELECT_ALL_RESOURCES', () => {
    it('should update selected resources in the state to be resources', done => {
      const resources = [{type: 'PACKAGE', id: 'id', label: 'label', readonly: false}]

      const options = {
        state: {
          resources: resources
        },
        expectedMutations: [
          {type: '__SET_SELECTED_RESOURCES__', payload: resources}
        ]
      }
      utils.testAction(actions.__SELECT_ALL_RESOURCES__, options, done)
    })
  })
  describe('DESELECT_ALL_RESOURCES', () => {
    it('should update selected resources in the state to be empty', done => {
      const options = {
        state: {
          selectedResources: [{type: 'PACKAGE', id: 'id', label: 'label', readonly: false}]
        },
        expectedMutations: [
          {type: '__SET_SELECTED_RESOURCES__', payload: []}
        ]
      }
      utils.testAction(actions.__DESELECT_ALL_RESOURCES__, options, done)
    })
  })
  describe('SELECT_RESOURCE', () => {
    it('should add given resource to the selected resources in the state', done => {
      const resource0 = {type: 'PACKAGE', id: 'id0', label: 'label0', readonly: false}
      const resource1 = {type: 'PACKAGE', id: 'id1', label: 'label1', readonly: false}
      const options = {
        state: {
          selectedResources: [resource0]
        },
        payload: resource1,
        expectedMutations: [
          {
            type: '__SET_SELECTED_RESOURCES__',
            payload: [resource0, resource1]
          }
        ]
      }
      utils.testAction(actions.__SELECT_RESOURCE__, options, done)
    })
  })
  describe('DESELECT_RESOURCE', () => {
    it('should remove given resource from the selected resources in the state', done => {
      const resource0 = {type: 'PACKAGE', id: '0', label: 'label0', readonly: false}
      const resource1 = {type: 'ENTITY_TYPE', id: '0', label: 'label0', readonly: false}
      const options = {
        state: {
          selectedResources: [resource0, resource1]
        },
        payload: resource0,
        expectedMutations: [
          {
            type: '__SET_SELECTED_RESOURCES__',
            payload: [resource1]
          }
        ]
      }
      utils.testAction(actions.__DESELECT_RESOURCE__, options, done)
    })
  })
  describe('DELETE_SELECTED_RESOURCES', () => {
    const resources = [{type: 'PACKAGE', id: 'id', label: 'label', readonly: false}]
    const deleteResources = td.function('api.deleteResources')

    it('should delete selected resources', done => {
      const job = {type: 'DELETE', id: 'id', status: 'RUNNING'}

      td.when(deleteResources(resources)).thenResolve(Promise.resolve(job))
      td.replace(api, 'deleteResources', deleteResources)

      const options = {
        state: {
          selectedResources: resources
        },
        expectedActions: [
          {type: '__POLL_JOB__', payload: job}
        ],
        expectedMutations: [
          {type: '__SET_SELECTED_RESOURCES__', payload: []},
          {type: '__ADD_JOB__', payload: job}
        ]
      }
      utils.testAction(actions.__DELETE_SELECTED_RESOURCES__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]

      td.when(deleteResources(resources)).thenResolve(Promise.reject(error))
      td.replace(api, 'deleteResources', deleteResources)

      const options = {
        state: {
          selectedResources: resources
        },
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__DELETE_SELECTED_RESOURCES__, options, done)
    })
  })
  describe('CREATE_RESOURCE', () => {
    const resource = {type: 'PACKAGE', id: 'id', label: 'label', readonly: false}
    const folder = {id: 'id', label: 'label', readonly: false}
    const createResource = td.function('api.createResource')

    it('should create the given resource and refresh resources in the state', done => {
      td.when(createResource(resource, folder)).thenResolve(Promise.resolve())
      td.replace(api, 'createResource', createResource)

      const options = {
        state: {
          folder: folder
        },
        payload: resource,
        expectedActions: [
          {
            type: '__FETCH_RESOURCES__'
          }
        ]
      }
      utils.testAction(actions.__CREATE_RESOURCE__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(createResource(resource, folder)).thenResolve(Promise.reject(error))
      td.replace(api, 'createResource', createResource)

      const options = {
        state: {
          folder: folder
        },
        payload: resource
      }
      utils.testAction(actions.__CREATE_RESOURCE__, options, done)
    })
  })
  describe('UPDATE_RESOURCE', () => {
    it('should update the given resource and refresh resources in the state', done => {
      const resource = {type: 'PACKAGE', id: 'id', label: 'label', readonly: false}
      const updatedResource = {type: 'PACKAGE', id: 'id', label: 'labelNew', readonly: false}

      const updateResource = td.function('api.updateResource')
      td.when(updateResource(resource, updatedResource)).thenResolve(Promise.resolve())
      td.replace(api, 'updateResource', updateResource)

      const options = {
        state: {
          resources: [resource]
        },
        payload: updatedResource,
        expectedActions: [
          {
            type: '__FETCH_RESOURCES__'
          }
        ]
      }
      utils.testAction(actions.__UPDATE_RESOURCE__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const resource = {type: 'PACKAGE', id: 'id', label: 'label', readonly: false}
      const updatedResource = {type: 'PACKAGE', id: 'id', label: 'labelNew', readonly: false}

      const updateResource = td.function('api.createResource')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(updateResource(resource, updatedResource)).thenResolve(Promise.reject(error))
      td.replace(api, 'updateResource', updateResource)

      const options = {
        state: {
          resources: [resource]
        },
        payload: updatedResource
      }
      utils.testAction(actions.__UPDATE_RESOURCE__, options, done)
    })
  })
  describe('MOVE_CLIPBOARD_RESOURCES', () => {
    it('should move clipboard resources to given target folder', done => {
      const resources = [{type: 'PACKAGE', id: 'id', label: 'label', readonly: false}]
      const folder = {id: 'id', label: 'label', readonly: false}

      const moveResources = td.function('api.moveResources')
      td.when(moveResources(resources, folder)).thenResolve(Promise.resolve())
      td.replace(api, 'moveResources', moveResources)

      const options = {
        payload: folder,
        state: {
          clipboard: {
            mode: 'CUT',
            resources: resources
          }
        },
        expectedActions: [
          {type: '__FETCH_RESOURCES__'}
        ],
        expectedMutations: [
          {type: '__RESET_CLIPBOARD__'}
        ]
      }
      utils.testAction(actions.__MOVE_CLIPBOARD_RESOURCES__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const resources = [{type: 'PACKAGE', id: 'id', label: 'label', readonly: false}]
      const folder = {id: 'id', label: 'label', readonly: false}

      const moveResources = td.function('api.moveResources')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(moveResources(resources, folder)).thenResolve(Promise.reject(error))
      td.replace(api, 'moveResources', moveResources)

      const options = {
        payload: folder,
        state: {
          clipboard: {
            mode: 'CUT',
            resources: resources
          }
        },
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__MOVE_CLIPBOARD_RESOURCES__, options, done)
    })
  })
  describe('COPY_CLIPBOARD_RESOURCES', () => {
    const resources = [{type: 'PACKAGE', id: 'id', label: 'label', readonly: false}]
    const folder = {id: 'id', label: 'label', readonly: false}

    it('should copy clipboard resources to given target folder', done => {
      const job = {type: 'COPY', id: 'id', status: 'RUNNING'}
      const copyResources = td.function('api.copyResources')
      td.when(copyResources(resources, folder)).thenResolve(Promise.resolve(job))
      td.replace(api, 'copyResources', copyResources)

      const options = {
        payload: folder,
        state: {
          clipboard: {
            mode: 'COPY',
            resources: resources
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
      utils.testAction(actions.__COPY_CLIPBOARD_RESOURCES__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const copyResources = td.function('api.copyResources')
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]
      td.when(copyResources(resources, folder)).thenResolve(Promise.reject(error))
      td.replace(api, 'copyResources', copyResources)

      const options = {
        payload: folder,
        state: {
          clipboard: {
            mode: 'CUT',
            resources: resources
          }
        },
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__COPY_CLIPBOARD_RESOURCES__, options, done)
    })
  })
  describe('POLL_JOB', () => {
    it('should update the job on progress change', done => {
      const job = {
        type: 'COPY',
        id: 'jobId',
        status: 'RUNNING',
        progress: 3,
        progressMax: 100
      }
      const updatedJob = {
        type: 'COPY',
        id: 'jobId',
        status: 'RUNNING',
        progress: 30,
        progressMax: 100
      }

      const fetchJob = td.function('api.fetchJob')
      td.when(fetchJob(job)).thenResolve(Promise.resolve(updatedJob))
      td.replace(api, 'fetchJob', fetchJob)

      const options = {
        payload: job,
        state: {
          jobs: [job]
        },
        expectedMutations: [
          {type: '__UPDATE_JOB__', payload: updatedJob}
        ]
      }
      utils.testAction(actions.__POLL_JOB__, options, done)
    })
    it('should fetch resources on copy job success', done => {
      const job = {
        type: 'COPY',
        id: 'jobId',
        status: 'RUNNING',
        progress: 3,
        progressMax: 100
      }
      const updatedJob = {
        type: 'COPY',
        id: 'jobId',
        status: 'SUCCESS',
        progress: 100,
        progressMax: 100,
        resultUrl: '/files/myfile.zip'
      }

      const fetchJob = td.function('api.fetchJob')
      td.when(fetchJob(job)).thenResolve(Promise.resolve(updatedJob))
      td.replace(api, 'fetchJob', fetchJob)

      const options = {
        payload: job,
        state: {
          jobs: [job]
        },
        expectedMutations: [
          {type: '__UPDATE_JOB__', payload: updatedJob}
        ],
        expectedActions: [
          {type: '__FETCH_RESOURCES__'}
        ]
      }
      utils.testAction(actions.__POLL_JOB__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]

      const job = {
        type: 'COPY',
        id: 'jobId',
        status: 'RUNNING',
        progress: 3,
        progressMax: 100
      }

      const fetchJob = td.function('api.fetchJob')
      td.when(fetchJob(job)).thenResolve(Promise.reject(error))
      td.replace(api, 'fetchJob', fetchJob)

      const options = {
        payload: job,
        state: {
          jobs: [job]
        },
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }

      utils.testAction(actions.__POLL_JOB__, options, done)
    })
  })
  describe('DOWNLOAD_SELECTED_RESOURCES', () => {
    const resources = [{type: 'PACKAGE', id: 'id', label: 'label', readonly: false}]
    const downloadResources = td.function('api.downloadResources')

    it('should download selected resources', done => {
      const job = {type: 'DOWNLOAD', id: 'id', status: 'RUNNING'}

      td.when(downloadResources(resources)).thenResolve(Promise.resolve(job))
      td.replace(api, 'downloadResources', downloadResources)

      const options = {
        state: {
          selectedResources: resources
        },
        expectedActions: [
          {type: '__POLL_JOB__', payload: job}
        ],
        expectedMutations: [
          {type: '__SET_SELECTED_RESOURCES__', payload: []},
          {type: '__ADD_JOB__', payload: job}
        ]
      }
      utils.testAction(actions.__DOWNLOAD_SELECTED_RESOURCES__, options, done)
    })
    it('should set alerts in the state in case of errors', done => {
      const error = new Error()
      error.alerts = [{type: 'ERROR', message: 'message'}]

      td.when(downloadResources(resources)).thenResolve(Promise.reject(error))
      td.replace(api, 'downloadResources', downloadResources)

      const options = {
        state: {
          selectedResources: resources
        },
        expectedMutations: [
          {type: '__ADD_ALERTS__', payload: error.alerts}
        ]
      }
      utils.testAction(actions.__DOWNLOAD_SELECTED_RESOURCES__, options, done)
    })
  })
})
