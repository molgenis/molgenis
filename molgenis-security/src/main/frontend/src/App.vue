<template>
  <div class="container">
    <spinner v-if="loading"></spinner>
    <div id="app" v-else>
      <members-breadcrumb></members-breadcrumb>
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
  import { FETCH_DATA } from './store/actions'
  import MembersBreadcrumb from 'components/MembersBreadcrumb'
  import { mapActions, mapState } from 'vuex'
  import Spinner from 'components/Spinner'

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
      Spinner,
      MembersBreadcrumb
    }
  }
</script>
