<template>
  <div class="row">
    <div class="col">
      <form @submit.prevent="onSubmit">
        <div class="form-group">
          <label for="memberSelect">User or group</label>
          <select v-model="member.id" class="form-control" id="memberSelect" required>
            <option v-for="userGroup in unassignedUsersGroups" :value="userGroup.id">{{ userGroup.label }}</option>
          </select>
        </div>
        <div class="form-group">
          <label for="roleSelect">Role</label>
          <select v-model="member.role" class="form-control" id="roleSelect" required>
            <option v-for="role in roles" :value="role.id">{{ role.label }}</option>
          </select>
        </div>
        <div class="form-group">
          <label for="fromDate">From</label>
          <input v-model="member.from" type="datetime-local" class="form-control" id="fromDate" required>
        </div>
        <div :class="['form-group', untilDateBeforeFromDateError ? 'has-danger' : '']">
          <label for="untilDate">Until</label>
          <input v-model="member.until" type="datetime-local" class="form-control" id="untilDate">
          <div v-if="untilDateBeforeFromDateError" class="form-control-feedback">
            Until date must be after the from date
          </div>
        </div>
        <button type="submit" class="btn btn-success">Save</button>
      </form>
    </div>
  </div>
</template>

<script>
  import {CREATE_MEMBER, GET_ROLES, GET_USERS_GROUPS, QUERY_MEMBERS} from '../store/actions'
  import {mapGetters} from 'vuex'
  import moment from 'moment'

  export default {
    name: 'member-edit',
    data: function () {
      return {
        member: {
          type: 'user',
          id: null,
          role: null,
          from: moment().format('YYYY-MM-DD[T]HH:mm'),
          until: null
        },
        untilDateBeforeFromDateError: false
      }
    },
    computed: {
      ...mapGetters({
        roles: 'getRoles',
        usersGroups: 'getUsersGroups',
        members: 'getMembers'
      }),
      unassignedUsersGroups: function () {
        return this.usersGroups.filter(userGroup => !this.members.find(member => member.id === userGroup.id))
      }
    },
    methods: {
      fetchData: function () {
        this.$store.dispatch(GET_ROLES)
        this.$store.dispatch(GET_USERS_GROUPS)
        this.$store.dispatch(QUERY_MEMBERS, {})
      },
      onSubmit: function () {
        if (this.member.until && moment(this.member.until, 'YYYY-MM-DD[T]HH:mm').isBefore(moment(this.member.from, 'YYYY-MM-DD[T]HH:mm'))) {
          this.untilDateBeforeFromDateError = true
        } else {
          this.$store.dispatch(CREATE_MEMBER, this.member).then(() => this.$router.go(-1))
        }
      }
    },
    created () {
      this.fetchData()
    },
    watch: {
      '$route': 'fetchData'
    }
  }
</script>

<style scoped>
  button {
    cursor: pointer;
  }
</style>
