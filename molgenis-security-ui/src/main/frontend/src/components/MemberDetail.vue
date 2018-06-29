<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb">
            <li class="breadcrumb-item">
              <router-link :to="{ name: 'groupOverView' }" class="text-capitalize">Groups</router-link>
            </li>
            <li class="breadcrumb-item">
              <router-link :to="{ name: 'groupDetail', params: { name: groupName } }" class="text-capitalize">{{groupName}}</router-link>
            </li>
            <li class="breadcrumb-item active text-capitalize" aria-current="page">{{memberName}}</li>
          </ol>
        </nav>
      </div>
    </div>

    <div class="row mb-3  ">
      <div class="col">
        <h1>Member details</h1>
      </div>
    </div>

    <div class="row">
      <div class="col-md-6">
        <h5 class="font-weight-light">Username</h5>
        <h5 v-if="member" class="pl-3">{{memberName}}</h5>
      </div>
    </div>

    <hr/>

    <div class="row">
      <div class="col-md-3">
        <h5 class="font-weight-light">Role</h5>
        <h5 v-if="member" class="pl-3">{{member.roleLabel}}</h5>
      </div>
      <div class="col-md-9 ">
        <button
          id="edit-role-btn"
          class="btn btn-sm btn-outline-secondary"
          type="button"
          @click.prevent="onUpdateMember">
          <i class="fa fa-edit"></i>
          Edit
        </button>
      </div>
    </div>

    <hr/>

    <button
      v-if="!isRemoving"
      id="remove-btn"
      class="btn btn-danger"
      type="button"
      @click.prevent="onRemoveMember">
      Remove from group
    </button>

    <button
      v-else
      id="remove-btn-removing"
      class="btn btn-danger"
      type="button"
      disabled="disabled">
      Removing <i class="fa fa-spinner fa-spin "></i>
    </button>

  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import Toast from './Toast'

  export default {
    name: 'MemberDetail',
    props: {
      groupName: {
        type: String,
        required: true
      },
      memberName: {
        type: String,
        required: true
      }
    },
    data () {
      return {
        isRemoving: false,
        isUpdating: false
      }
    },
    computed: {
      ...mapGetters([
        'groupMembers',
        'getLoginUser'
      ]),
      member () {
        const members = this.groupMembers[this.groupName] || []
        return members.find(m => m.username === this.memberName)
      }
    },
    methods: {
      onRemoveMember () {
        this.isRemoving = !this.isRemoving
        this.$store.dispatch('removeMember', {groupName: this.groupName, memberName: this.memberName})
          .then(() => {
            this.$router.push({ name: 'groupDetail', params: { name: this.groupName } })
          }, () => {
            this.isRemoving = !this.isRemoving
          })
      },
      onUpdateMember () {
        this.isUpdating = !this.isUpdating
        const updateMemberCommand = { roleName: this.roleName }
        this.$store.dispatch('updateMember', {groupName: this.groupName, memberName: this.memberName, updateMemberCommand})
          .then(() => {
            this.$router.push({ name: 'groupDetail', params: { name: this.groupName } })
          }, () => {
            this.isUpdating = !this.isUpdating
          })
      }
    },
    created () {
      if (!this.groupMembers.length) {
        this.$store.dispatch('fetchGroupMembers', this.groupName)
      }
    },
    components: {
      Toast
    }
  }
</script>
