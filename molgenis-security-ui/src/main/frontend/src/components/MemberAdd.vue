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
            <select id="userSelect" v-model="userId" class="form-control">
              <option value="">- Please select a user -</option>
              <option v-for="user in sortedUsers" :value="user.id">{{user.username}}</option>
            </select>
          </div>

          <div class="form-group">
            <label for="roleSelect">Role in group</label>
            <select id="roleSelect" v-model="roleName" class="form-control">
              <option value="">- Please select a role -</option>
              <option v-for="role in sortedRoles" :value="role.roleName">{{role.roleLabel}}</option>
            </select>
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
            :disabled="!userId || !roleName">
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
        userId: '',
        roleName: '',
        isAdding: false
      }
    },
    computed: {
      ...mapGetters([
        'groupRoles',
        'users'
      ]),
      sortedRoles () {
        const roles = this.groupRoles[this.groupName] || []
        return [...roles].sort((a, b) => a.roleLabel.localeCompare(b.roleLabel))
      },
      sortedUsers () {
        return [...this.users].sort((a, b) => a.username.localeCompare(b.username))
      }
    },
    methods: {
      onSubmit () {
        this.isAdding = !this.isAdding
        const addMemberCommand = { userId: this.userId, roleName: this.roleName }
        this.$store.dispatch('addMember', {group: this.groupName, addMemberCommand})
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
