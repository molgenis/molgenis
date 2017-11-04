<template>
  <div>
    <div class="row">
      <div class="col-4" v-for="member in members">
        <members-grid-body-card v-bind="member"></members-grid-body-card>
      </div>
    </div>
  </div>
</template>

<script>
  import MembersGridBodyCard from './MembersGridBodyCard'
  import { mapGetters } from 'vuex'

  export default {
    name: 'members-grid-body',
    computed: {
      ...mapGetters(['members'])
    },
    components: {
      MembersGridBodyCard
    }
  }
</script>
