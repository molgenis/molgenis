<template>
  <div class="row">
    <div class="col">
      <members-header></members-header>
      <members-grid></members-grid>
      <permission-controls></permission-controls>
    </div>
  </div>
</template>

<script>
  import MembersHeader from './MembersHeader'
  import MembersGrid from './MembersGrid'
  import PermissionControls from './PermissionControls'
  import {QUERY_MEMBERS} from '../store/actions'
  import {mapGetters, mapActions} from 'vuex'

  export default {
    name: 'members-view',
    computed: {
      ...mapGetters({
        members: 'getMembers'
      })
    },
    methods: {
      ...mapActions({
        fetchMember: QUERY_MEMBERS
      })
    },
    created () {
      this.fetchMember()
    },
    watch: {
      '$route': 'fetchMember'
    },
    components: {
      MembersHeader,
      MembersGrid,
      PermissionControls
    }
  }
</script>
