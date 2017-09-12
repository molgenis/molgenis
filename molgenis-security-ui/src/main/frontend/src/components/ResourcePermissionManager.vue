<template>
  <div class="container pt-3">
    <ol class="breadcrumb">
      <li class="breadcrumb-item"><a href="/">Home</a></li>
      <li class="breadcrumb-item active">{{'RESOURCE_PERMISSION_MANAGER' | i18n}}</li>
    </ol>
    <div>
      <p>entityType: {{$route.params.entityType}}</p>
      <p>id: {{$route.params.id}}</p>
      <p>
        <router-link :to="'/resource/sys_md_EntityType/sys_md_EntityType#'">click?</router-link>
      </p>
    </div>
  </div>
</template>

<script>
  import { mapActions } from 'vuex'
  import {GET_ACL} from '../store/actions'

  export default {
    name: 'resource-permission-manager',
    methods: {
      ...mapActions({
        getAcl: GET_ACL
      }),
      test () {
        console.log('test')
      }
    },
    computed: {},
    components: {},
    filters: {},
    beforeRouteUpdate (to, from, next) {
      this.test()

      next()
    },
    watch: {
      '$route' (to, from) {
        this.getAcl()
      }
    }
  }
</script>
