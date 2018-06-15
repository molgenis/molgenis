<template>
  <div>

    <toast></toast>

    <h1>{{ 'security-ui-group-title' | i18n }}</h1>
    <ul v-if="groups.length > 0">
      <li v-for="group in groups">
        <span v-if="group.label">{{group.label}}</span>
      </li>
    </ul>
    <p v-else>{{ 'security-ui-no-groups' | i18n }}</p>

  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import Toast from './Toast'

  export default {
    name: 'GroupOverview',
    computed: {
      ...mapGetters([
        'groups'
      ])
    },
    components: {
      Toast
    },
    created () {
      this.$store.dispatch('fetchGroups')
    }
  }
</script>
