export default {
  'SET_ERROR' (state, error) {
    state.error = error
  },

  'SET_LOADING' (state, loading) {
    state.loading = loading
  },

  'UPDATE_APPS' (state, apps) {
    state.apps = apps
  }
}