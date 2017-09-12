<template>
  <div class="container pt-3">
    <ol class="breadcrumb">
      <li class="breadcrumb-item"><a href="/">Home</a></li>
      <li class="breadcrumb-item active">{{'RESOURCE_PERMISSION_MANAGER' | i18n}}</li>
    </ol>
    <div>
      <p>entityType: {{$route.params.entityType}}</p>
      <p>id: {{$route.params.id}}</p>
      <code v-if="acl">{{acl | json}}</code>
      <p>
        <router-link :to="'/resource/sys_md_Package/sys'">click?</router-link>
      </p>
    </div>
  </div>
</template>

<script>
  import { mapActions, mapState } from 'vuex'
  import {GET_ACL} from '../store/actions'

  export default {
    name: 'resource-permission-manager',
    methods: {
      ...mapActions({
        getAcl: GET_ACL
      })
    },
    computed: {
      ...mapState(['acl'])
    },
    components: {},
    mounted () {
      this.getAcl()
    },
    watch: {
      '$route' (to, from) {
        this.getAcl()
      }
    },
    filters: {
      json (object) {
        return JSON.stringify(object, null, 2)
      }
    }
  }
</script>
