import api from '@molgenis/molgenis-api-client'

export default {
  'ACTIVATE_APP' ({commit, dispatch}, appId) {
    api.get('/plugin/appmanager/activate/' + appId).then(() => {
      dispatch('FETCH_APPS')
    }, error => {
      commit('SET_ERROR', error)
    })
  },

  'DEACTIVATE_APP' ({commit, dispatch}, appId) {
    api.get('/plugin/appmanager/deactivate/' + appId).then(() => {
      dispatch('FETCH_APPS')
    }, error => {
      commit('SET_ERROR', error)
    })
  },

  'DELETE_APP' ({commit, dispatch}, appId) {
    api.get('/plugin/appmanager/delete/' + appId).then(() => {
      dispatch('FETCH_APPS')
    }, error => {
      commit('SET_ERROR', error)
    })
  },

  'FETCH_APPS' ({commit}) {
    api.get('/plugin/appmanager/apps').then(apps => {
      commit('UPDATE_APPS', apps)
      commit('SET_LOADING', false)
    }, error => {
      // could be 'no permission to see apps, please login before continuing'
      commit('SET_ERROR', error)
    })
  },

  'UPLOAD_APP' ({commit, dispatch}, file) {
    api.postFile('/plugin/appmanager/upload', file).then(() => {
      dispatch('FETCH_APPS')
    }, error => {
      commit('SET_ERROR', error)
    })
  }
}