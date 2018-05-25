// @flow
import type { AppManagerState, App } from '../flow.types'

export default {
  'SET_ERROR' (state: AppManagerState, error: string) {
    state.error = error
  },

  'SET_LOADING' (state: AppManagerState, loading: boolean) {
    state.loading = loading
  },

  'UPDATE_APPS' (state: AppManagerState, apps: Array<App>) {
    state.apps = apps
  }
}
