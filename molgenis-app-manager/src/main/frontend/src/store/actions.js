// @flow
import api from '@molgenis/molgenis-api-client'

import type { VuexContext } from '../flow.types'

export default {
  'ACTIVATE_APP' ({commit, dispatch}: VuexContext, appId: string) {
    api.post('/plugin/appmanager/activate/' + appId).then(() => {
      dispatch('FETCH_APPS')
    }, error => {
      commit('SET_ERROR', error)
    })
  },

  'DEACTIVATE_APP' ({commit, dispatch}: VuexContext, appId: string) {
    api.post('/plugin/appmanager/deactivate/' + appId).then(() => {
      dispatch('FETCH_APPS')
    }, error => {
      console.log('error')
      commit('SET_ERROR', error)
    })
  },

  'DELETE_APP' ({commit, dispatch}: VuexContext, appId: string) {
    api.delete_('/plugin/appmanager/delete/' + appId).then(() => {
      dispatch('FETCH_APPS')
    }, error => {
      commit('SET_ERROR', error)
    })
  },

  'FETCH_APPS' ({commit}: VuexContext) {
    api.get('/plugin/appmanager/apps').then(apps => {
      commit('UPDATE_APPS', apps)
      commit('SET_LOADING', false)
    }, error => {
      // could be 'no permission to see apps, please login before continuing'
      commit('SET_ERROR', error)
    })
  },

  'UPLOAD_APP' ({commit, dispatch}: VuexContext, file: File) {
    api.postFile('/plugin/appmanager/upload', file).then(() => {
      dispatch('FETCH_APPS')
    }, error => {
      commit('SET_ERROR', error)
    })
  }
}
