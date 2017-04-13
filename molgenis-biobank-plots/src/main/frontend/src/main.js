// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import store from './store/index'
import BootstrapVue from 'bootstrap-vue/dist/bootstrap-vue.esm'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import './assets/css/biobank-plot-style.css'

if (window.__webpack_public_path__) {
  /* eslint-disable no-undef, camelcase */
  __webpack_public_path__ = window.__webpack_public_path__
  /* eslint-enable */
}

Vue.use(BootstrapVue)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  store,
  template: '<App/>',
  components: { App }
})
