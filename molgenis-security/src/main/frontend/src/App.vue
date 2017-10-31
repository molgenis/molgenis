<template>
  <div class="container">
    <div v-if="loading">
      <i class="fa fa-spinner"></i>
    </div>
    <div id="app" v-else>
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
  import { FETCH_DATA } from './store/actions'
  import MembersBreadcrumb from 'components/MembersBreadcrumb'
  import { mapActions, mapState } from 'vuex'

  export default {
    name: 'molgenis-app',
    computed: {
      ...mapState(['loading'])
    },
    methods: {
      ...mapActions({'fetchData': FETCH_DATA})
    },
    created () {
      this.fetchData()
    },
    components: {
      MembersBreadcrumb
    }
  }
</script>
