import Vue from 'vue'
import App from './App.vue'

Vue.config.productionTip = false

const root= new Vue({
  render: createElement => createElement(App)
}).$mount('#app')
