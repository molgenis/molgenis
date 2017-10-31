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
  import { mapGetters } from 'vuex'

  export default {
    name: 'members-view',
    computed: {
      ...mapGetters({
        members: 'getMembers'
      })
    },
    components: {
      MembersHeader,
      MembersGrid,
      PermissionControls
    }
  }
</script>
