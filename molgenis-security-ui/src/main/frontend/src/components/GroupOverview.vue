<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <h1>{{ 'security-ui-groups-page-title' | i18n }}</h1>
      </div>
    </div>

    <div class="row">
      <div class="col" v-if="getLoginUser.isSuperUser">
        <button id="add-group-btn" @click="addGroup" type="button" class="btn btn-primary float-right"><i
          class="fa fa-plus"></i> {{'security-ui-add-group' | i18n}}
        </button>
        <h3 class="mt-2">{{'security-ui-groups-header' | i18n}}</h3>
      </div>
    </div>

    <div class="row groups-listing mt-1">
      <div class="col">

        <div v-if="groups.length > 0" class="list-group">
          <h5 class="text-capitalize">
            <router-link
              v-for="group in sortedGroups"
              :key="group.name"
              :to="{ name: 'groupDetail', params: { name: group.name } }"
              class="list-group-item list-group-item-action">
              {{group.label}}
            </router-link>
          </h5>
        </div>

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
        'getLoginUser'
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
