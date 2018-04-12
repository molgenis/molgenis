import Vue from 'vue'
import AppManager from './AppManager.vue'

Vue.config.productionTip = false

new Vue({
  el: '#app-manager',
  template: '<AppManager/>',
  components: {AppManager}
})
