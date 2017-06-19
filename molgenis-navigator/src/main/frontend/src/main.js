import "es6-promise/auto";
import Vue from "vue";
import App from "./App";
import {sync} from "vuex-router-sync";
import store from "./store";
import router from "./router";
// You can use both the components found @https://bootstrap-vue.github.io/
import BootstrapVue from "bootstrap-vue/dist/bootstrap-vue.esm";
// Or use manual bootstrap 4 from @https://v4-alpha.getbootstrap.com/getting-started/introduction/
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";

import "font-awesome/css/font-awesome.min.css";

// Keeps the router and the store in sync @https://github.com/vuejs/vuex-router-sync
sync(store, router)

Vue.use(BootstrapVue)
/* eslint-disable no-new */
new Vue({
  el: '#app',
  store,
  router,
  template: '<App />',
  components: {App}
})
