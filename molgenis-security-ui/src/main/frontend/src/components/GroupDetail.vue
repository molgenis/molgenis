<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <h1>{{ 'security-ui-members-page-title' | i18n }}</h1>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <button id="add-member-btn" @click="addMember" type="button" class="btn btn-primary float-right"><i class="fa fa-plus"></i> Add Member</button>
        <h3 class="mt-2">Members</h3>
      </div>
    </div>

    <div class="row groups-listing mt-1">
      <div class="col">
        <ul class="list-group">
          <li v-for="member in sortedMembers" class="list-group-item flex-column align-items-start">
            <div class="d-flex w-100 justify-content-between">
              <h5 class="text-capitalize">{{member.username}}  <small class="font-weight-light text-uppercase"> ({{member.roleLabel}})</small></h5>
            </div>
          </li>
        </ul>
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
        'groupMembers'
      ]),
      sortedMembers () {
        const groupMember = this.groupMembers[this.name] || []
        return [...groupMember].sort((a, b) => a.username.localeCompare(b.username))
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
    },
    components: {
      Toast
    }
  }
</script>
