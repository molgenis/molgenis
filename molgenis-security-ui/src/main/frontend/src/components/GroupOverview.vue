<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <h1>{{ 'security-ui-groups-page-title' | i18n }}</h1>
      </div>
    </div>

    <div class="row">
      <div class="col ">
        <router-link to="/group/create">
          <a href="#" class="btn btn-primary float-right" role="button"><i class="fa fa-plus"></i> Add Group</a>
        </router-link>
        <h5 class="mt-2">Groups</h5>
      </div>
    </div>

    <div class="row groups-listing mt-1">
      <div class="col">
        <ul v-if="groups.length > 0" class="list-group">
          <li v-for="group in sortedGroups" class="list-group-item">
            <span v-if="group.label" class="font-weight-bold">{{group.label}}</span>
          </li>
        </ul>
        <ul v-else class="list-group">
          <li class="list-group-item">
            <span>{{ 'security-ui-no-groups-found' | i18n }}</span>
          </li>

        </ul>
      </div>
    </div>

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
      ]),
      sortedGroups () {
        return [...this.groups].sort((a, b) => a.label.localeCompare(b.label))
      }
    },
    components: {
      Toast
    },
    created () {
      this.$store.dispatch('fetchGroups')
    }
  }
</script>
