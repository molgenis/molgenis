import Vue from 'vue'
import App from './App.vue'

Vue.config.productionTip = false

new Vue({
  render: createElement => createElement(App)
}).$mount('#molgenis-site-menu')
