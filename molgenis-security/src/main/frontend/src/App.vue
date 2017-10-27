<template>
  <div class="container">
    <div id="app">
      <members-breadcrumb></members-breadcrumb>
      <router-view></router-view>
    </div>
  </div>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";
</style>

<script>
  import {GET_ROLES, GET_USERS_GROUPS} from './store/actions'
  import MembersBreadcrumb from 'components/MembersBreadcrumb'

  export default {
    name: 'molgenis-app',
    methods: {
      fetchRoles: function () {
        this.$store.dispatch(GET_ROLES)
        this.$store.dispatch(GET_USERS_GROUPS)
      }
    },
    created() {
      this.fetchRoles()
    },
    watch: {
      '$route': 'fetchRoles'
    },
    components: {
      MembersBreadcrumb
    }
  }
</script>
