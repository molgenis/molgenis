<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <h1>{{ 'security-ui-groups-page-title' | i18n }}</h1>
      </div>
    </div>

    <div class="row">
      <div class="col" v-if="isSuperUser">
          <button @click="addGroup" type="button" class="btn btn-primary float-right"><i class="fa fa-plus"></i> Add Group</button>
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
  import { mapGetters, mapMutations } from 'vuex'
  import Toast from './Toast'

  export default {
    name: 'GroupOverview',
    computed: {
      ...mapGetters([
        'groups',
        'isSuperUser'
      ]),
      sortedGroups () {
        return [...this.groups].sort((a, b) => a.label.localeCompare(b.label))
      }
    },
    methods: {
      ...mapMutations([
        'clearToast'
      ]),
      addGroup () {
        this.clearToast()
        this.$router.push({name: 'createGroup'})
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
