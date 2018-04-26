import Vue from 'vue'
import Router from 'vue-router'
import MetadataManagerContainer from 'components/MetadataManagerContainer'
import { INITIAL_STATE } from '../store/state'

Vue.use(Router)
export default new Router({
  mode: 'history',
  // Set base URL which is provided by the server
  // e.g. metadata-manager base URL is /menu/main/metadata-manager
  base: INITIAL_STATE.baseUrl,
  routes: [
    {
      path: '/',
      component: MetadataManagerContainer
    },
    {
      path: '/:entityTypeId',
      component: MetadataManagerContainer
    },
    {
      path: '/:entityTypeId/:attributeId',
      component: MetadataManagerContainer
    }
  ]
})
