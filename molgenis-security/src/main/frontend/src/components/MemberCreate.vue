<template>
  <div class="row">
    <div class="col">
      <form @submit.prevent="onSubmit">
        <div class="form-group">
          <label for="memberSelect">User</label>
          <p v-if="edit">{{label}}</p>
          <select v-model="id" class="form-control" id="memberSelect" required v-else>
            <option v-for="user in unassignedUsers" :value="user.id">{{ user.label }}</option>
          </select>

        </div>
        <div class="form-group">
          <label for="roleSelect">Role</label>
          <select v-model="role" class="form-control" id="roleSelect" required>
            <option v-for="role in context.children" :value="role.id">{{ role.label }}</option>
          </select>
        </div>
        <div class="form-group">
          <label for="fromDate">From</label>
          <input v-model="from" type="date" class="form-control" id="fromDate" required>
        </div>
        <div class="form-group" :class="{'has-danger': untilDateBeforeFromDateError}">
          <label for="untilDate">Until</label>
          <input v-model="until" type="date" class="form-control" id="untilDate">
          <div v-if="untilDateBeforeFromDateError" class="form-control-feedback">
            Until date must be after the from date
          </div>
        </div>
        <button type="submit" class="btn btn-success">Save</button>
        <button v-if="edit" type="button" class="btn btn-danger" @click="deleteMember">Remove</button>
      </form>
    </div>
  </div>
</template>

<script>
  import { CREATE_MEMBER, DELETE_MEMBER } from '../store/actions'
  import { mapGetters, mapState, mapActions } from 'vuex'
  import moment from 'moment'

  export default {
    name: 'member-create',
    created () {
      if (this.member) {
        this.edit = true
        this.type = this.member.type
        this.id = this.member.id
        this.label = this.member.label
        this.role = this.member.role
//        this.from = this.member.from TODO: update if in future
        if (this.member.until) {
          this.until = this.member.until.substring(0, 10)
        }
      }
    },
    data: function () {
      return {
        id: null,
        role: null,
        from: moment().format('YYYY-MM-DD'),
        until: null,
        untilDateBeforeFromDateError: false,
        edit: false
      }
    },
    computed: {
      ...mapState(['users']),
      ...mapGetters(['members', 'unassignedUsers', 'context', 'member'])
    },
    methods: {
      ...mapActions({createMember: CREATE_MEMBER, deleteMember: DELETE_MEMBER}),
      onSubmit: function () {
        if (this.until && moment(this.until, 'YYYY-MM-DD').isBefore(moment(this.from, 'YYYY-MM-DD'))) {
          this.untilDateBeforeFromDateError = true
        } else {
          this.createMember({
            id: this.id,
            role: this.role,
            until: this.until,
            from: this.from
          }).then(() => this.$router.go(-1))
        }
      }
    }
  }
</script>

<style scoped>
  button {
    cursor: pointer;
  }
</style>
