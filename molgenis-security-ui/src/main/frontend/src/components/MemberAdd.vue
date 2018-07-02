<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <h1>Add member</h1>
      </div>
    </div>

    <div class="row">
      <div class="col-md-6">
        <form>

          <div class="form-group">
            <label for="userSelect">User</label>
            <select id="userSelect" v-model="username" class="form-control" :disabled="nonMemberUsers.length === 0">
              <option v-if="nonMemberUsers.length > 0" value="">- Please select a user -</option>
              <option v-else="nonMemberUsers.length > 0" value="" >No available users</option>
              <option v-for="user in nonMemberUsers" :value="user.username">{{user.username}}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Role in group</label>
            <div v-for="role in sortedRoles" class="form-check" >
              <input class="form-check-input" type="radio" name="roleRadio"
                     :id="role.roleName" :value="role.roleName" v-model="roleName" >
              <label class="form-check-label" :for="role.roleName">
                {{role.roleLabel}}
              </label>
            </div>

          </div>

          <router-link :to="{name: 'groupDetail', params: { name: groupName }}">
            <a href="#" class="btn btn-secondary" role="button">Cancel</a>
          </router-link>

          <button
            v-if="!isAdding"
            id="create-btn"
            class="btn btn-success"
            type="submit"
            @click.prevent="onSubmit"
            :disabled="!username || !roleName">
            Add Member
          </button>

          <button
            v-else
            id="save-btn-saving"
            class="btn btn-primary"
            type="button"
            disabled="disabled">
            Create <i class="fa fa-spinner fa-spin "></i>
          </button>

        </form>
      </div>
    </div>


  </div>
</template>

<script>
  import Toast from './Toast'
  import { mapGetters } from 'vuex'

  export default {
    name: 'MemberAdd',
    props: {
      groupName: {
        type: String,
        required: false
      }
    },
    data () {
      return {
        username: '',
        roleName: '',
        isAdding: false
      }
    },
    computed: {
      ...mapGetters([
        'groupRoles',
        'users',
        'groupMembers'
      ]),
      sortedRoles () {
        const roles = this.groupRoles[this.groupName] || []
        return [...roles].sort((a, b) => a.roleLabel.localeCompare(b.roleLabel))
      },
      nonMemberUsers () {
        const currentMembers = this.groupMembers[this.groupName] || []
        const currentMemberNames = currentMembers.map((cm) => cm.username)
        const nonMemberUsers = this.users.filter((u) => !currentMemberNames.includes(u.username))
        return [...nonMemberUsers].sort((a, b) => a.username.localeCompare(b.username))
      }
    },
    methods: {
      onSubmit () {
        this.isAdding = !this.isAdding
        const addMemberCommand = { username: this.username, roleName: this.roleName }
        this.$store.dispatch('addMember', {groupName: this.groupName, addMemberCommand})
          .then(() => {
            this.$router.push({ name: 'groupDetail', params: { name: this.groupName } })
          }, () => {
            this.isAdding = !this.isAdding
          })
      }
    },
    created () {
      this.$store.dispatch('tempFetchUsers')
      this.$store.dispatch('fetchGroupRoles', this.groupName)
    },
    components: {
      Toast
    }
  }
</script>
