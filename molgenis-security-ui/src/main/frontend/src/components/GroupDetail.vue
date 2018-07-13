<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb">
            <li class="breadcrumb-item">
              <router-link :to="{ name: 'groupOverView' }">{{ 'security-ui-breadcrumb-groups' | i18n }}</router-link>
            </li>
            <li class="breadcrumb-item active text-capitalize" aria-current="page">{{name}}</li>
          </ol>
        </nav>
      </div>
    </div>

    <div class="row mb-3  ">
      <div class="col">
        <h1>{{ 'security-ui-members-page-title' | i18n }}</h1>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <button id="add-member-btn" v-if="canAddMember" @click="addMember" type="button"
                class="btn btn-primary float-right"><i class="fa fa-plus"></i> {{'security-ui-add-member' | i18n}}
        </button>
        <h3 class="mt-2">{{'security-ui-members-header' | i18n}}</h3>
      </div>
    </div>

    <div class="row groups-listing mt-1">
      <div class="col">
        <router-link
          v-for="member in sortedMembers"
          :key="member.username"
          :to="{ name: 'memberDetail', params: { groupName: name, memberName: member.username } }"
          class="list-group-item list-group-item-action">
          <div>
            <h5 class="text-capitalize">{{member.username}}  <small class="font-weight-light text-uppercase"> ({{member.roleLabel}})</small></h5>
          </div>
        </router-link>
      </div>
    </div>

  </div>
</template>

<script>
  import Toast from './Toast'
  import { mapGetters, mapMutations } from 'vuex'

  export default {
    name: 'GroupDetail',
    props: {
      name: {
        type: String,
        required: false
      }
    },
    computed: {
      ...mapGetters([
        'groupMembers',
        'groupPermissions'
      ]),
      sortedMembers () {
        const members = this.groupMembers[this.name] || []
        return [...members].sort((a, b) => a.username.localeCompare(b.username))
      },
      canAddMember () {
        const permissions = this.groupPermissions[this.name] || []
        return permissions.includes('ADD_MEMBERSHIP')
      }
    },
    methods: {
      ...mapMutations([
        'clearToast'
      ]),
      addMember () {
        this.clearToast()
        this.$router.push({name: 'addMember', params: { groupName: this.name }})
      }
    },
    created () {
      this.$store.dispatch('fetchGroupMembers', this.name)
      this.$store.dispatch('fetchGroupPermissions', this.name)
    },
    components: {
      Toast
    }
  }
</script>
