<template>
  <div>

    <h1>Hello Security UI</h1>
    <ul v-if="groups.length > 0">
      <li v-for="group in groups">
        <span>{{group.name}}</span>
        <span v-if="group.description">{{group.description}}</span>
      </li>
    </ul>
    <p v-else>No groups available</p>


  </div>
</template>

<script>
  import { mapGetters } from 'vuex'

  export default {
    name: 'GroupOverview',
    computed: {
      ...mapGetters([
        'groups'
      ])
    },
    components: {
    },
    created () {
      this.$store.dispatch('FETCH_GROUPS')
    }
  }
</script>
